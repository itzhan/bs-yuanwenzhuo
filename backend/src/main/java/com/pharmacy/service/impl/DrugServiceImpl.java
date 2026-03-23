package com.pharmacy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pharmacy.common.PageResult;
import com.pharmacy.common.exception.BusinessException;
import com.pharmacy.dto.DrugDTO;
import com.pharmacy.entity.Drug;
import com.pharmacy.entity.DrugCategory;
import com.pharmacy.mapper.DrugCategoryMapper;
import com.pharmacy.mapper.DrugMapper;
import com.pharmacy.service.DrugService;
import com.pharmacy.vo.DrugVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DrugServiceImpl extends ServiceImpl<DrugMapper, Drug> implements DrugService {

    @Autowired
    private DrugCategoryMapper drugCategoryMapper;

    @Override
    public PageResult<DrugVO> listDrugs(int page, int size, String keyword,
                                        Long categoryId, Integer status) {
        Page<Drug> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Drug> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(Drug::getGenericName, keyword)
                    .or().like(Drug::getTradeName, keyword)
                    .or().like(Drug::getDrugCode, keyword));
        }
        wrapper.eq(categoryId != null, Drug::getCategoryId, categoryId);
        wrapper.eq(status != null, Drug::getStatus, status);
        wrapper.orderByDesc(Drug::getCreateTime);

        Page<Drug> result = page(pageParam, wrapper);

        Set<Long> categoryIds = result.getRecords().stream()
                .map(Drug::getCategoryId)
                .filter(cid -> cid != null)
                .collect(Collectors.toSet());
        Map<Long, String> categoryNameMap = Map.of();
        if (!categoryIds.isEmpty()) {
            List<DrugCategory> categories = drugCategoryMapper.selectBatchIds(categoryIds);
            categoryNameMap = categories.stream()
                    .collect(Collectors.toMap(DrugCategory::getId, DrugCategory::getName));
        }

        Map<Long, String> finalMap = categoryNameMap;
        List<DrugVO> voList = result.getRecords().stream().map(drug -> {
            DrugVO vo = new DrugVO();
            BeanUtils.copyProperties(drug, vo);
            if (drug.getCategoryId() != null) {
                vo.setCategoryName(finalMap.get(drug.getCategoryId()));
            }
            return vo;
        }).collect(Collectors.toList());

        return PageResult.of(voList, result.getTotal(), page, size);
    }

    @Override
    public DrugVO getDrugDetail(Long id) {
        Drug drug = getById(id);
        if (drug == null) {
            throw new BusinessException("药品不存在");
        }
        DrugVO vo = new DrugVO();
        BeanUtils.copyProperties(drug, vo);
        if (drug.getCategoryId() != null) {
            DrugCategory category = drugCategoryMapper.selectById(drug.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
        }
        return vo;
    }

    @Override
    public void addDrug(DrugDTO dto) {
        Drug drug = new Drug();
        BeanUtils.copyProperties(dto, drug);
        if (!StringUtils.hasText(drug.getDrugCode())) {
            drug.setDrugCode("DRG" + System.currentTimeMillis());
        }
        save(drug);
    }

    @Override
    public void updateDrug(Long id, DrugDTO dto) {
        Drug drug = getById(id);
        if (drug == null) {
            throw new BusinessException("药品不存在");
        }
        BeanUtils.copyProperties(dto, drug);
        drug.setId(id);
        updateById(drug);
    }

    @Override
    public void deleteDrug(Long id) {
        removeById(id);
    }

    @Override
    public List<Drug> getAllDrugs() {
        return list(new LambdaQueryWrapper<Drug>()
                .eq(Drug::getStatus, 1)
                .orderByAsc(Drug::getGenericName));
    }
}
