package com.pharmacy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pharmacy.common.PageResult;
import com.pharmacy.common.exception.BusinessException;
import com.pharmacy.dto.PrescriptionDTO;
import com.pharmacy.dto.PrescriptionItemDTO;
import com.pharmacy.entity.Drug;
import com.pharmacy.entity.Prescription;
import com.pharmacy.entity.PrescriptionItem;
import com.pharmacy.entity.User;
import com.pharmacy.mapper.DrugMapper;
import com.pharmacy.mapper.PrescriptionItemMapper;
import com.pharmacy.mapper.PrescriptionMapper;
import com.pharmacy.mapper.UserMapper;
import com.pharmacy.service.PrescriptionService;
import com.pharmacy.vo.PrescriptionItemVO;
import com.pharmacy.vo.PrescriptionVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PrescriptionServiceImpl extends ServiceImpl<PrescriptionMapper, Prescription>
        implements PrescriptionService {

    @Autowired
    private PrescriptionItemMapper prescriptionItemMapper;

    @Autowired
    private DrugMapper drugMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public PageResult<PrescriptionVO> listPrescriptions(int page, int size, String keyword, Integer status, Long doctorId) {
        Page<Prescription> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Prescription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(doctorId != null, Prescription::getDoctorId, doctorId);
        wrapper.eq(status != null, Prescription::getStatus, status);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Prescription::getPrescriptionNo, keyword)
                    .or().like(Prescription::getPatientName, keyword)
                    .or().like(Prescription::getPatientPhone, keyword)
                    .or().like(Prescription::getDiagnosis, keyword));
        }
        wrapper.orderByDesc(Prescription::getCreateTime);

        Page<Prescription> result = page(pageParam, wrapper);
        List<Prescription> records = result.getRecords();

        // 批量获取医师姓名
        Set<Long> doctorIds = records.stream().map(Prescription::getDoctorId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> doctorNameMap = Map.of();
        if (!doctorIds.isEmpty()) {
            doctorNameMap = userMapper.selectBatchIds(doctorIds).stream()
                    .collect(Collectors.toMap(User::getId,
                            u -> u.getRealName() != null ? u.getRealName() : u.getUsername()));
        }

        // 批量获取每个处方的明细数量
        List<Long> ids = records.stream().map(Prescription::getId).collect(Collectors.toList());
        Map<Long, Integer> itemCountMap = new HashMap<>();
        if (!ids.isEmpty()) {
            List<PrescriptionItem> allItems = prescriptionItemMapper.selectList(
                    new LambdaQueryWrapper<PrescriptionItem>().in(PrescriptionItem::getPrescriptionId, ids));
            for (PrescriptionItem it : allItems) {
                itemCountMap.merge(it.getPrescriptionId(), 1, Integer::sum);
            }
        }

        Map<Long, String> finalDoctorMap = doctorNameMap;
        List<PrescriptionVO> voList = records.stream().map(p -> {
            PrescriptionVO vo = new PrescriptionVO();
            BeanUtils.copyProperties(p, vo);
            vo.setDoctorName(finalDoctorMap.get(p.getDoctorId()));
            vo.setItemCount(itemCountMap.getOrDefault(p.getId(), 0));
            return vo;
        }).collect(Collectors.toList());

        return PageResult.of(voList, result.getTotal(), page, size);
    }

    @Override
    public PrescriptionVO getPrescriptionDetail(Long id) {
        Prescription prescription = getById(id);
        if (prescription == null) {
            throw new BusinessException("处方不存在");
        }
        return buildVO(prescription);
    }

    @Override
    @Transactional
    public void createPrescription(PrescriptionDTO dto, Long doctorId) {
        if (doctorId == null) {
            throw new BusinessException("无法获取当前用户");
        }
        Prescription prescription = new Prescription();
        BeanUtils.copyProperties(dto, prescription);
        prescription.setDoctorId(doctorId);
        prescription.setStatus(1);
        prescription.setPrescriptionNo(generatePrescriptionNo());
        save(prescription);

        saveItems(prescription.getId(), dto.getItems());
    }

    @Override
    @Transactional
    public void updatePrescription(Long id, PrescriptionDTO dto, Long doctorId) {
        Prescription existing = getById(id);
        if (existing == null) {
            throw new BusinessException("处方不存在");
        }
        if (!Objects.equals(existing.getDoctorId(), doctorId)) {
            throw new BusinessException("只能修改自己开具的处方");
        }
        if (existing.getStatus() == null || existing.getStatus() != 1) {
            throw new BusinessException("只能修改有效状态的处方");
        }

        existing.setPatientName(dto.getPatientName());
        existing.setPatientGender(dto.getPatientGender());
        existing.setPatientAge(dto.getPatientAge());
        existing.setPatientPhone(dto.getPatientPhone());
        existing.setDiagnosis(dto.getDiagnosis());
        existing.setRemark(dto.getRemark());
        updateById(existing);

        // 先删旧明细，再插入新明细
        prescriptionItemMapper.delete(new LambdaQueryWrapper<PrescriptionItem>()
                .eq(PrescriptionItem::getPrescriptionId, id));
        saveItems(id, dto.getItems());
    }

    @Override
    public void voidPrescription(Long id, Long doctorId) {
        Prescription existing = getById(id);
        if (existing == null) {
            throw new BusinessException("处方不存在");
        }
        if (!Objects.equals(existing.getDoctorId(), doctorId)) {
            throw new BusinessException("只能作废自己开具的处方");
        }
        if (existing.getStatus() != null && existing.getStatus() == 0) {
            throw new BusinessException("处方已作废");
        }
        existing.setStatus(0);
        updateById(existing);
    }

    @Override
    public List<PrescriptionVO> getPatientMedication(String patientName, String patientPhone) {
        if (!StringUtils.hasText(patientName) && !StringUtils.hasText(patientPhone)) {
            throw new BusinessException("请至少输入患者姓名或电话");
        }
        LambdaQueryWrapper<Prescription> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(patientName)) {
            wrapper.eq(Prescription::getPatientName, patientName);
        }
        if (StringUtils.hasText(patientPhone)) {
            wrapper.eq(Prescription::getPatientPhone, patientPhone);
        }
        wrapper.orderByDesc(Prescription::getCreateTime);

        List<Prescription> list = list(wrapper);
        return list.stream().map(this::buildVO).collect(Collectors.toList());
    }

    // ----------------- helpers -----------------

    private void saveItems(Long prescriptionId, List<PrescriptionItemDTO> itemDTOs) {
        if (itemDTOs == null) return;
        for (PrescriptionItemDTO itemDTO : itemDTOs) {
            PrescriptionItem item = new PrescriptionItem();
            item.setPrescriptionId(prescriptionId);
            item.setDrugId(itemDTO.getDrugId());
            item.setQuantity(itemDTO.getQuantity());
            item.setDosage(itemDTO.getDosage());
            item.setUsageNote(itemDTO.getUsageNote());
            prescriptionItemMapper.insert(item);
        }
    }

    private PrescriptionVO buildVO(Prescription prescription) {
        PrescriptionVO vo = new PrescriptionVO();
        BeanUtils.copyProperties(prescription, vo);

        if (prescription.getDoctorId() != null) {
            User doctor = userMapper.selectById(prescription.getDoctorId());
            if (doctor != null) {
                vo.setDoctorName(doctor.getRealName() != null ? doctor.getRealName() : doctor.getUsername());
            }
        }

        List<PrescriptionItem> items = prescriptionItemMapper.selectList(
                new LambdaQueryWrapper<PrescriptionItem>()
                        .eq(PrescriptionItem::getPrescriptionId, prescription.getId()));

        Set<Long> drugIds = items.stream().map(PrescriptionItem::getDrugId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, Drug> drugMap = Map.of();
        if (!drugIds.isEmpty()) {
            drugMap = drugMapper.selectBatchIds(drugIds).stream()
                    .collect(Collectors.toMap(Drug::getId, d -> d));
        }

        Map<Long, Drug> finalDrugMap = drugMap;
        List<PrescriptionItemVO> itemVOs = items.stream().map(it -> {
            PrescriptionItemVO iv = new PrescriptionItemVO();
            BeanUtils.copyProperties(it, iv);
            Drug drug = finalDrugMap.get(it.getDrugId());
            if (drug != null) {
                iv.setDrugName(drug.getGenericName());
                iv.setSpecification(drug.getSpecification());
                iv.setUnit(drug.getUnit());
                iv.setManufacturer(drug.getManufacturer());
            }
            return iv;
        }).collect(Collectors.toList());
        vo.setItems(itemVOs);
        vo.setItemCount(itemVOs.size());
        return vo;
    }

    private String generatePrescriptionNo() {
        return "RX" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", new Random().nextInt(10000));
    }
}
