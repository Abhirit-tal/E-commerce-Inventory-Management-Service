package com.example.inventory.service.impl;

import com.example.inventory.entity.Sku;
import com.example.inventory.exception.NotFoundException;
import com.example.inventory.repository.SkuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.OptimisticLockException;

@Service
public class SkuInventoryService {

    private final SkuRepository skuRepository;

    @Autowired
    public SkuInventoryService(SkuRepository skuRepository) {
        this.skuRepository = skuRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Sku adjustInventoryOnce(Long id, int delta) {
        Sku s = skuRepository.findById(id).orElseThrow(() -> new NotFoundException("Sku not found"));
        int newCount = s.getInventoryCount() + delta;
        if (newCount < 0) {
            throw new IllegalArgumentException("Inventory cannot be negative");
        }
        s.setInventoryCount(newCount);
        Sku saved = skuRepository.save(s);
        return saved;
    }
}

