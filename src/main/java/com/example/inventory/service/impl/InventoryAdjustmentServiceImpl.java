package com.example.inventory.service.impl;

import com.example.inventory.dto.inventory.InventoryAdjustmentDTO;
import com.example.inventory.entity.InventoryAdjustment;
import com.example.inventory.service.InventoryAdjustmentService;
import com.example.inventory.repository.InventoryAdjustmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class InventoryAdjustmentServiceImpl implements InventoryAdjustmentService {

    private final InventoryAdjustmentRepository repository;

    @Autowired
    public InventoryAdjustmentServiceImpl(InventoryAdjustmentRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<InventoryAdjustmentDTO> listBySku(Long skuId, Pageable pageable) {
        return repository.findBySkuId(skuId, pageable).map(this::toDto);
    }

    private InventoryAdjustmentDTO toDto(InventoryAdjustment a) {
        InventoryAdjustmentDTO dto = new InventoryAdjustmentDTO();
        dto.setId(a.getId());
        dto.setSkuId(a.getSku() != null ? a.getSku().getId() : null);
        dto.setDelta(a.getDelta());
        dto.setReason(a.getReason());
        dto.setCreatedAt(a.getCreatedAt());
        return dto;
    }
}
