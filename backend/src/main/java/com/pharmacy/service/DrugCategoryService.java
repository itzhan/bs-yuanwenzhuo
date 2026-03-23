package com.pharmacy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pharmacy.dto.DrugCategoryDTO;
import com.pharmacy.entity.DrugCategory;

import java.util.List;
import java.util.Map;

public interface DrugCategoryService extends IService<DrugCategory> {

    List<DrugCategory> listAll();

    List<Map<String, Object>> listTree();

    void addCategory(DrugCategoryDTO dto);

    void updateCategory(Long id, DrugCategoryDTO dto);

    void deleteCategory(Long id);
}
