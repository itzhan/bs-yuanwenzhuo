package com.pharmacy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pharmacy.common.PageResult;
import com.pharmacy.dto.DrugDTO;
import com.pharmacy.entity.Drug;
import com.pharmacy.vo.DrugVO;

import java.util.List;

public interface DrugService extends IService<Drug> {

    PageResult<DrugVO> listDrugs(int page, int size, String keyword, Long categoryId, Integer status);

    DrugVO getDrugDetail(Long id);

    void addDrug(DrugDTO dto);

    void updateDrug(Long id, DrugDTO dto);

    void deleteDrug(Long id);

    List<Drug> getAllDrugs();
}
