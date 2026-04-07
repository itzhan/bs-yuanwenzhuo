package com.pharmacy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pharmacy.common.PageResult;
import com.pharmacy.dto.PrescriptionDTO;
import com.pharmacy.entity.Prescription;
import com.pharmacy.vo.PrescriptionVO;

import java.util.List;

public interface PrescriptionService extends IService<Prescription> {

    PageResult<PrescriptionVO> listPrescriptions(int page, int size, String keyword, Integer status, Long doctorId);

    PrescriptionVO getPrescriptionDetail(Long id);

    void createPrescription(PrescriptionDTO dto, Long doctorId);

    void updatePrescription(Long id, PrescriptionDTO dto, Long doctorId);

    void voidPrescription(Long id, Long doctorId);

    List<PrescriptionVO> getPatientMedication(String patientName, String patientPhone);
}
