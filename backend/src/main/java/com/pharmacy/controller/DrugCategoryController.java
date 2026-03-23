package com.pharmacy.controller;

import com.pharmacy.common.Result;
import com.pharmacy.dto.DrugCategoryDTO;
import com.pharmacy.entity.DrugCategory;
import com.pharmacy.service.DrugCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class DrugCategoryController {

    private final DrugCategoryService drugCategoryService;

    @GetMapping
    public Result<List<DrugCategory>> list() {
        return Result.success(drugCategoryService.listAll());
    }

    @GetMapping("/tree")
    @SuppressWarnings("unchecked")
    public Result<List<Map<String, Object>>> tree() {
        return Result.success(drugCategoryService.listTree());
    }

    @PostMapping
    public Result<Void> add(@RequestBody @Valid DrugCategoryDTO dto) {
        drugCategoryService.addCategory(dto);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid DrugCategoryDTO dto) {
        drugCategoryService.updateCategory(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        drugCategoryService.deleteCategory(id);
        return Result.success();
    }
}
