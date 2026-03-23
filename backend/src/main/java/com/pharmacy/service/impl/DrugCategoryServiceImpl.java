package com.pharmacy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pharmacy.common.exception.BusinessException;
import com.pharmacy.dto.DrugCategoryDTO;
import com.pharmacy.entity.Drug;
import com.pharmacy.entity.DrugCategory;
import com.pharmacy.mapper.DrugCategoryMapper;
import com.pharmacy.mapper.DrugMapper;
import com.pharmacy.service.DrugCategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DrugCategoryServiceImpl extends ServiceImpl<DrugCategoryMapper, DrugCategory>
        implements DrugCategoryService {

    @Autowired
    private DrugMapper drugMapper;

    @Override
    public List<DrugCategory> listAll() {
        return list(new LambdaQueryWrapper<DrugCategory>()
                .orderByAsc(DrugCategory::getSort));
    }

    @Override
    public List<Map<String, Object>> listTree() {
        List<DrugCategory> all = listAll();
        Map<Long, List<DrugCategory>> parentMap = all.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getParentId() == null ? 0L : c.getParentId()));
        return buildTree(parentMap, 0L);
    }

    private List<Map<String, Object>> buildTree(Map<Long, List<DrugCategory>> parentMap, Long parentId) {
        List<DrugCategory> children = parentMap.getOrDefault(parentId, Collections.emptyList());
        List<Map<String, Object>> result = new ArrayList<>();
        for (DrugCategory cat : children) {
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("id", cat.getId());
            node.put("name", cat.getName());
            node.put("parentId", cat.getParentId());
            node.put("sort", cat.getSort());
            List<Map<String, Object>> childNodes = buildTree(parentMap, cat.getId());
            if (!childNodes.isEmpty()) {
                node.put("children", childNodes);
            }
            result.add(node);
        }
        return result;
    }

    @Override
    public void addCategory(DrugCategoryDTO dto) {
        DrugCategory category = new DrugCategory();
        BeanUtils.copyProperties(dto, category);
        save(category);
    }

    @Override
    public void updateCategory(Long id, DrugCategoryDTO dto) {
        DrugCategory category = getById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        BeanUtils.copyProperties(dto, category);
        category.setId(id);
        updateById(category);
    }

    @Override
    public void deleteCategory(Long id) {
        long drugCount = drugMapper.selectCount(new LambdaQueryWrapper<Drug>()
                .eq(Drug::getCategoryId, id));
        if (drugCount > 0) {
            throw new BusinessException("该分类下存在药品，无法删除");
        }
        long childCount = count(new LambdaQueryWrapper<DrugCategory>()
                .eq(DrugCategory::getParentId, id));
        if (childCount > 0) {
            throw new BusinessException("该分类下存在子分类，无法删除");
        }
        removeById(id);
    }
}
