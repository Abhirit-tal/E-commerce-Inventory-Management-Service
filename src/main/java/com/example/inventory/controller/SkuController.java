package com.example.inventory.controller;

import com.example.inventory.dto.sku.InventoryAdjustDTO;
import com.example.inventory.dto.sku.SkuCreateDTO;
import com.example.inventory.dto.sku.SkuDTO;
import com.example.inventory.service.SkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/skus")
public class SkuController {

    private final SkuService skuService;

    @Autowired
    public SkuController(SkuService skuService) {
        this.skuService = skuService;
    }

    @PostMapping
    public ResponseEntity<SkuDTO> create(@Valid @RequestBody SkuCreateDTO dto) {
        SkuDTO created = skuService.create(dto);
        return ResponseEntity.status(201).body(created);
    }

    @GetMapping
    public ResponseEntity<Page<SkuDTO>> list(@RequestParam(required = false) Long productId, Pageable pageable) {
        Page<SkuDTO> page = skuService.list(productId, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SkuDTO> get(@PathVariable Long id) {
        return ResponseEntity.ok(skuService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SkuDTO> update(@PathVariable Long id, @Valid @RequestBody SkuCreateDTO dto) {
        return ResponseEntity.ok(skuService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        skuService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/inventory/adjust")
    public ResponseEntity<SkuDTO> adjustInventory(@PathVariable Long id, @Valid @RequestBody InventoryAdjustDTO dto) {
        SkuDTO updated = skuService.adjustInventory(id, dto);
        return ResponseEntity.ok(updated);
    }
}

