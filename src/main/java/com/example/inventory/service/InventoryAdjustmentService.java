package com.example.inventory.service;

import com.example.inventory.dto.inventory.InventoryAdjustmentDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InventoryAdjustmentService {
    Page<InventoryAdjustmentDTO> listBySku(Long skuId, Pageable pageable);
}

