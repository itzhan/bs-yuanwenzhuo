package com.pharmacy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pharmacy.common.PageResult;
import com.pharmacy.dto.SupplierDTO;
import com.pharmacy.entity.Supplier;

import java.util.List;

public interface SupplierService extends IService<Supplier> {

    PageResult<Supplier> listSuppliers(int page, int size, String keyword);

    void addSupplier(SupplierDTO dto);

    void updateSupplier(Long id, SupplierDTO dto);

    void deleteSupplier(Long id);

    List<Supplier> getAllSuppliers();
}
