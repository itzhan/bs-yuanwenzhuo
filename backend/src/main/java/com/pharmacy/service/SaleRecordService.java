package com.pharmacy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pharmacy.common.PageResult;
import com.pharmacy.dto.SaleRecordDTO;
import com.pharmacy.entity.SaleRecord;
import com.pharmacy.vo.SaleRecordVO;

public interface SaleRecordService extends IService<SaleRecord> {

    PageResult<SaleRecordVO> listSales(int page, int size, String keyword, Long drugId);

    void createSale(SaleRecordDTO dto, Long operatorId);

    SaleRecordVO getSaleDetail(Long id);

    void deleteSale(Long id);
}
