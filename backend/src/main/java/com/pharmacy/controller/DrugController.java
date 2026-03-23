package com.pharmacy.controller;

import com.pharmacy.common.PageResult;
import com.pharmacy.common.Result;
import com.pharmacy.dto.DrugDTO;
import com.pharmacy.entity.Drug;
import com.pharmacy.service.DrugService;
import com.pharmacy.vo.DrugVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drugs")
@RequiredArgsConstructor
public class DrugController {

    private final DrugService drugService;

    @GetMapping
    public Result<PageResult<DrugVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status) {
        return Result.success(drugService.listDrugs(page, size, keyword, categoryId, status));
    }

    @GetMapping("/all")
    public Result<List<Drug>> all() {
        return Result.success(drugService.getAllDrugs());
    }

    @GetMapping("/{id}")
    public Result<DrugVO> getById(@PathVariable Long id) {
        return Result.success(drugService.getDrugDetail(id));
    }

    @PostMapping
    public Result<Void> add(@RequestBody @Valid DrugDTO drugDTO) {
        drugService.addDrug(drugDTO);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid DrugDTO drugDTO) {
        drugService.updateDrug(id, drugDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        drugService.deleteDrug(id);
        return Result.success();
    }
}
