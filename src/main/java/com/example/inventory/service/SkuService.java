package com.example.inventory.service;

import com.example.inventory.dto.sku.InventoryAdjustDTO;
import com.example.inventory.dto.sku.SkuCreateDTO;
import com.example.inventory.dto.sku.SkuDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SkuService {
    SkuDTO create(SkuCreateDTO dto);
    SkuDTO getById(Long id);
    Page<SkuDTO> list(Long productId, Pageable pageable);
    SkuDTO update(Long id, SkuCreateDTO dto);
    void delete(Long id);
    SkuDTO adjustInventory(Long id, InventoryAdjustDTO dto);
}

