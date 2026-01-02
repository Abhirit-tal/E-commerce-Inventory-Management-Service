package com.example.inventory.controller;

import com.example.inventory.dto.inventory.InventoryAdjustmentCreateDTO;
import com.example.inventory.dto.inventory.InventoryAdjustmentDTO;
import com.example.inventory.dto.sku.InventoryAdjustDTO;
import com.example.inventory.dto.sku.SkuDTO;
import com.example.inventory.service.InventoryAdjustmentService;
import com.example.inventory.service.SkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/inventory-adjustments")
public class InventoryAdjustmentController {

    private final InventoryAdjustmentService service;
    private final SkuService skuService;

    @Autowired
    public InventoryAdjustmentController(InventoryAdjustmentService service, SkuService skuService) {
        this.service = service;
        this.skuService = skuService;
    }

    @GetMapping("/sku/{skuId}")
    public ResponseEntity<Page<InventoryAdjustmentDTO>> listBySku(@PathVariable Long skuId, Pageable pageable) {
        Page<InventoryAdjustmentDTO> page = service.listBySku(skuId, pageable);
        return ResponseEntity.ok(page);
    }

    @PostMapping
    public ResponseEntity<SkuDTO> create(@Valid @RequestBody InventoryAdjustmentCreateDTO dto) {
        InventoryAdjustDTO adj = new InventoryAdjustDTO();
        adj.setDelta(dto.getDelta());
        adj.setReason(dto.getReason());
        adj.setIdempotencyKey(dto.getIdempotencyKey());
        SkuDTO updated = skuService.adjustInventory(dto.getSkuId(), adj);
        return ResponseEntity.ok(updated);
    }
}
