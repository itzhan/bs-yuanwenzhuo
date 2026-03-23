package com.pharmacy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pharmacy.common.PageResult;
import com.pharmacy.common.exception.BusinessException;
import com.pharmacy.dto.PurchaseOrderDTO;
import com.pharmacy.dto.PurchaseOrderItemDTO;
import com.pharmacy.entity.*;
import com.pharmacy.mapper.*;
import com.pharmacy.service.PurchaseOrderService;
import com.pharmacy.vo.PurchaseOrderItemVO;
import com.pharmacy.vo.PurchaseOrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PurchaseOrderServiceImpl extends ServiceImpl<PurchaseOrderMapper, PurchaseOrder>
        implements PurchaseOrderService {

    @Autowired
    private PurchaseOrderItemMapper purchaseOrderItemMapper;

    @Autowired
    private SupplierMapper supplierMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DrugMapper drugMapper;

    @Autowired
    private InventoryMapper inventoryMapper;

    @Autowired
    private InventoryLogMapper inventoryLogMapper;

    @Override
    public PageResult<PurchaseOrderVO> listOrders(int page, int size, Integer status, String keyword) {
        Page<PurchaseOrder> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, PurchaseOrder::getStatus, status);
        if (StringUtils.hasText(keyword)) {
            wrapper.like(PurchaseOrder::getOrderNo, keyword);
        }
        wrapper.orderByDesc(PurchaseOrder::getCreateTime);

        Page<PurchaseOrder> result = page(pageParam, wrapper);

        Set<Long> supplierIds = result.getRecords().stream()
                .map(PurchaseOrder::getSupplierId).filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> userIds = new HashSet<>();
        result.getRecords().forEach(o -> {
            if (o.getCreatorId() != null) userIds.add(o.getCreatorId());
            if (o.getApproverId() != null) userIds.add(o.getApproverId());
        });

        Map<Long, String> supplierNameMap = Map.of();
        if (!supplierIds.isEmpty()) {
            supplierNameMap = supplierMapper.selectBatchIds(supplierIds).stream()
                    .collect(Collectors.toMap(Supplier::getId, Supplier::getName));
        }

        Map<Long, String> userNameMap = Map.of();
        if (!userIds.isEmpty()) {
            userNameMap = userMapper.selectBatchIds(userIds).stream()
                    .collect(Collectors.toMap(User::getId, User::getRealName));
        }

        Map<Long, String> finalSupplierMap = supplierNameMap;
        Map<Long, String> finalUserMap = userNameMap;
        List<PurchaseOrderVO> voList = result.getRecords().stream().map(order -> {
            PurchaseOrderVO vo = new PurchaseOrderVO();
            BeanUtils.copyProperties(order, vo);
            vo.setSupplierName(finalSupplierMap.get(order.getSupplierId()));
            vo.setCreatorName(order.getCreatorId() != null ? finalUserMap.get(order.getCreatorId()) : null);
            vo.setApproverName(order.getApproverId() != null ? finalUserMap.get(order.getApproverId()) : null);
            return vo;
        }).collect(Collectors.toList());

        return PageResult.of(voList, result.getTotal(), page, size);
    }

    @Override
    public PurchaseOrderVO getOrderDetail(Long id) {
        PurchaseOrder order = getById(id);
        if (order == null) {
            throw new BusinessException("采购单不存在");
        }

        PurchaseOrderVO vo = new PurchaseOrderVO();
        BeanUtils.copyProperties(order, vo);

        if (order.getSupplierId() != null) {
            Supplier supplier = supplierMapper.selectById(order.getSupplierId());
            if (supplier != null) vo.setSupplierName(supplier.getName());
        }
        if (order.getCreatorId() != null) {
            User creator = userMapper.selectById(order.getCreatorId());
            if (creator != null) vo.setCreatorName(creator.getRealName());
        }
        if (order.getApproverId() != null) {
            User approver = userMapper.selectById(order.getApproverId());
            if (approver != null) vo.setApproverName(approver.getRealName());
        }

        List<PurchaseOrderItem> items = purchaseOrderItemMapper.selectList(
                new LambdaQueryWrapper<PurchaseOrderItem>()
                        .eq(PurchaseOrderItem::getPurchaseOrderId, id));

        Set<Long> drugIds = items.stream()
                .map(PurchaseOrderItem::getDrugId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, Drug> drugMap = Map.of();
        if (!drugIds.isEmpty()) {
            drugMap = drugMapper.selectBatchIds(drugIds).stream()
                    .collect(Collectors.toMap(Drug::getId, d -> d));
        }

        Map<Long, Drug> finalDrugMap = drugMap;
        List<PurchaseOrderItemVO> itemVOs = items.stream().map(item -> {
            PurchaseOrderItemVO itemVO = new PurchaseOrderItemVO();
            itemVO.setId(item.getId());
            itemVO.setOrderId(item.getPurchaseOrderId());
            itemVO.setDrugId(item.getDrugId());
            itemVO.setQuantity(item.getQuantity());
            itemVO.setUnitPrice(item.getUnitPrice());
            itemVO.setTotalPrice(item.getAmount());
            Drug drug = finalDrugMap.get(item.getDrugId());
            if (drug != null) {
                itemVO.setDrugName(drug.getGenericName());
                itemVO.setSpecification(drug.getSpecification());
                itemVO.setUnit(drug.getUnit());
            }
            return itemVO;
        }).collect(Collectors.toList());
        vo.setItems(itemVOs);

        return vo;
    }

    @Override
    @Transactional
    public void createOrder(PurchaseOrderDTO dto, Long creatorId) {
        PurchaseOrder order = new PurchaseOrder();
        order.setSupplierId(dto.getSupplierId());
        order.setRemark(dto.getRemark());
        order.setCreatorId(creatorId);
        order.setStatus(0);

        String orderNo = "PO" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", new Random().nextInt(10000));
        order.setOrderNo(orderNo);

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<PurchaseOrderItem> items = new ArrayList<>();
        for (PurchaseOrderItemDTO itemDTO : dto.getItems()) {
            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setDrugId(itemDTO.getDrugId());
            item.setQuantity(itemDTO.getQuantity());
            item.setUnitPrice(itemDTO.getUnitPrice());
            BigDecimal amount = itemDTO.getUnitPrice()
                    .multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            item.setAmount(amount);
            totalAmount = totalAmount.add(amount);
            items.add(item);
        }
        order.setTotalAmount(totalAmount);
        save(order);

        for (PurchaseOrderItem item : items) {
            item.setPurchaseOrderId(order.getId());
            purchaseOrderItemMapper.insert(item);
        }
    }

    @Override
    public void approveOrder(Long id, Long approverId) {
        PurchaseOrder order = getById(id);
        if (order == null) {
            throw new BusinessException("采购单不存在");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("只能审批待审批状态的采购单");
        }
        order.setStatus(1);
        order.setApproverId(approverId);
        updateById(order);
    }

    @Override
    @Transactional
    public void receiveOrder(Long id, Long operatorId) {
        PurchaseOrder order = getById(id);
        if (order == null) {
            throw new BusinessException("采购单不存在");
        }
        if (order.getStatus() != 1) {
            throw new BusinessException("只能收货已审批的采购单");
        }

        order.setStatus(2);
        updateById(order);

        List<PurchaseOrderItem> items = purchaseOrderItemMapper.selectList(
                new LambdaQueryWrapper<PurchaseOrderItem>()
                        .eq(PurchaseOrderItem::getPurchaseOrderId, id));

        for (PurchaseOrderItem item : items) {
            Inventory inventory = inventoryMapper.selectOne(
                    new LambdaQueryWrapper<Inventory>()
                            .eq(Inventory::getDrugId, item.getDrugId())
                            .last("LIMIT 1"));

            if (inventory != null) {
                int beforeQty = inventory.getQuantity();
                int afterQty = beforeQty + item.getQuantity();
                inventory.setQuantity(afterQty);
                inventoryMapper.updateById(inventory);

                InventoryLog log = new InventoryLog();
                log.setDrugId(item.getDrugId());
                log.setInventoryId(inventory.getId());
                log.setType(0);
                log.setQuantity(item.getQuantity());
                log.setBeforeQuantity(beforeQty);
                log.setAfterQuantity(afterQty);
                log.setReason("采购入库 - 单号：" + order.getOrderNo());
                log.setOperatorId(operatorId);
                inventoryLogMapper.insert(log);
            } else {
                Inventory newInv = new Inventory();
                newInv.setDrugId(item.getDrugId());
                newInv.setQuantity(item.getQuantity());
                newInv.setWarningThreshold(10);
                inventoryMapper.insert(newInv);

                InventoryLog log = new InventoryLog();
                log.setDrugId(item.getDrugId());
                log.setInventoryId(newInv.getId());
                log.setType(0);
                log.setQuantity(item.getQuantity());
                log.setBeforeQuantity(0);
                log.setAfterQuantity(item.getQuantity());
                log.setReason("采购入库 - 单号：" + order.getOrderNo());
                log.setOperatorId(operatorId);
                inventoryLogMapper.insert(log);
            }
        }
    }

    @Override
    public void cancelOrder(Long id) {
        PurchaseOrder order = getById(id);
        if (order == null) {
            throw new BusinessException("采购单不存在");
        }
        if (order.getStatus() != 0 && order.getStatus() != 1) {
            throw new BusinessException("只能取消待审批或已审批的采购单");
        }
        order.setStatus(3);
        updateById(order);
    }

    @Override
    public void deleteOrder(Long id) {
        removeById(id);
    }
}
