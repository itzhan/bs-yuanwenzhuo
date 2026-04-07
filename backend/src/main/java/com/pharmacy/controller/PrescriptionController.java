package com.pharmacy.controller;

import com.pharmacy.common.PageResult;
import com.pharmacy.common.Result;
import com.pharmacy.dto.PrescriptionDTO;
import com.pharmacy.service.PrescriptionService;
import com.pharmacy.vo.PrescriptionVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @GetMapping
    public Result<PageResult<PrescriptionVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "true") boolean mineOnly) {
        Long doctorId = mineOnly ? getCurrentUserId() : null;
        return Result.success(prescriptionService.listPrescriptions(page, size, keyword, status, doctorId));
    }

    @GetMapping("/patient")
    public Result<List<PrescriptionVO>> patientMedication(
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) String patientPhone) {
        return Result.success(prescriptionService.getPatientMedication(patientName, patientPhone));
    }

    @GetMapping("/{id}")
    public Result<PrescriptionVO> getById(@PathVariable Long id) {
        return Result.success(prescriptionService.getPrescriptionDetail(id));
    }

    @PostMapping
    public Result<Void> create(@RequestBody @Valid PrescriptionDTO dto) {
        prescriptionService.createPrescription(dto, getCurrentUserId());
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid PrescriptionDTO dto) {
        prescriptionService.updatePrescription(id, dto, getCurrentUserId());
        return Result.success();
    }

    @PutMapping("/{id}/void")
    public Result<Void> voidPrescription(@PathVariable Long id) {
        prescriptionService.voidPrescription(id, getCurrentUserId());
        return Result.success();
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getDetails() instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> details = (Map<String, Object>) auth.getDetails();
            Object uid = details.get("userId");
            if (uid instanceof Long) return (Long) uid;
            if (uid instanceof Integer) return ((Integer) uid).longValue();
        }
        return null;
    }
}
