package com.pharmacy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pharmacy.common.PageResult;
import com.pharmacy.common.exception.BusinessException;
import com.pharmacy.dto.SupplierDTO;
import com.pharmacy.entity.Supplier;
import com.pharmacy.mapper.SupplierMapper;
import com.pharmacy.service.SupplierService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class SupplierServiceImpl extends ServiceImpl<SupplierMapper, Supplier> implements SupplierService {

    @Override
    public PageResult<Supplier> listSuppliers(int page, int size, String keyword) {
        Page<Supplier> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(Supplier::getName, keyword)
                    .or().like(Supplier::getContactPerson, keyword)
                    .or().like(Supplier::getPhone, keyword));
        }
        wrapper.orderByDesc(Supplier::getCreateTime);

        Page<Supplier> result = page(pageParam, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    @Override
    public void addSupplier(SupplierDTO dto) {
        Supplier supplier = new Supplier();
        BeanUtils.copyProperties(dto, supplier);
        save(supplier);
    }

    @Override
    public void updateSupplier(Long id, SupplierDTO dto) {
        Supplier supplier = getById(id);
        if (supplier == null) {
            throw new BusinessException("供应商不存在");
        }
        BeanUtils.copyProperties(dto, supplier);
        supplier.setId(id);
        updateById(supplier);
    }

    @Override
    public void deleteSupplier(Long id) {
        removeById(id);
    }

    @Override
    public List<Supplier> getAllSuppliers() {
        return list(new LambdaQueryWrapper<Supplier>()
                .eq(Supplier::getStatus, 1)
                .orderByAsc(Supplier::getName));
    }
}
