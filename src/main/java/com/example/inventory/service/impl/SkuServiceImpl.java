package com.example.inventory.service.impl;

import com.example.inventory.dto.sku.InventoryAdjustDTO;
import com.example.inventory.dto.sku.SkuCreateDTO;
import com.example.inventory.dto.sku.SkuDTO;
import com.example.inventory.entity.InventoryAdjustment;
import com.example.inventory.entity.Product;
import com.example.inventory.entity.Sku;
import com.example.inventory.exception.NotFoundException;
import com.example.inventory.repository.InventoryAdjustmentRepository;
import com.example.inventory.repository.ProductRepository;
import com.example.inventory.repository.SkuRepository;
import com.example.inventory.service.SkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.OptimisticLockException;

@Service
public class SkuServiceImpl implements SkuService {

    private final SkuRepository skuRepository;
    private final ProductRepository productRepository;
    private final InventoryAdjustmentRepository adjustmentRepository;

    @Autowired
    public SkuServiceImpl(SkuRepository skuRepository, ProductRepository productRepository, InventoryAdjustmentRepository adjustmentRepository) {
        this.skuRepository = skuRepository;
        this.productRepository = productRepository;
        this.adjustmentRepository = adjustmentRepository;
    }

    @Override
    @Transactional
    public SkuDTO create(SkuCreateDTO dto) {
        Product p = productRepository.findById(dto.getProductId()).orElseThrow(() -> new NotFoundException("Product not found"));
        Sku s = new Sku();
        s.setCode(dto.getCode());
        s.setBarcode(dto.getBarcode());
        s.setPrice(dto.getPrice());
        s.setInventoryCount(dto.getInventoryCount());
        s.setActive(dto.getActive());
        s.setProduct(p);
        Sku saved = skuRepository.save(s);
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SkuDTO getById(Long id) {
        Sku s = skuRepository.findById(id).orElseThrow(() -> new NotFoundException("Sku not found"));
        return toDto(s);
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<SkuDTO> list(Long productId, org.springframework.data.domain.Pageable pageable) {
        if (productId != null) {
            return skuRepository.findByProductId(productId, pageable).map(this::toDto);
        }
        return skuRepository.findAll(pageable).map(this::toDto);
    }

    @Override
    @Transactional
    public SkuDTO update(Long id, SkuCreateDTO dto) {
        Sku s = skuRepository.findById(id).orElseThrow(() -> new NotFoundException("Sku not found"));
        s.setBarcode(dto.getBarcode());
        s.setPrice(dto.getPrice());
        s.setActive(dto.getActive());
        Sku saved = skuRepository.save(s);
        return toDto(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Sku s = skuRepository.findById(id).orElseThrow(() -> new NotFoundException("Sku not found"));
        skuRepository.delete(s);
    }

    @Override
    @Transactional
    public SkuDTO adjustInventory(Long id, InventoryAdjustDTO dto) {
        // idempotency check
        if (dto.getIdempotencyKey() != null) {
            var existing = adjustmentRepository.findBySkuIdAndIdempotencyKey(id, dto.getIdempotencyKey());
            if (existing.isPresent()) {
                // return current sku state
                Sku s = skuRepository.findById(id).orElseThrow(() -> new NotFoundException("Sku not found"));
                return toDto(s);
            }
        }

        final int maxAttempts = 3;
        int attempt = 0;
        while (true) {
            attempt++;
            try {
                int rows = skuRepository.updateInventoryIfNonNegative(id, dto.getDelta());
                if (rows == 1) {
                    Sku s = skuRepository.findById(id).orElseThrow(() -> new NotFoundException("Sku not found"));
                    // record adjustment
                    InventoryAdjustment adj = new InventoryAdjustment();
                    adj.setSku(s);
                    adj.setDelta(dto.getDelta());
                    adj.setReason(dto.getReason());
                    adj.setIdempotencyKey(dto.getIdempotencyKey());
                    try {
                        adjustmentRepository.save(adj);
                    } catch (DataIntegrityViolationException dive) {
                        // likely a concurrent insert of same idempotency key -> treat as idempotent
                        Sku s2 = skuRepository.findById(id).orElseThrow(() -> new NotFoundException("Sku not found"));
                        return toDto(s2);
                    }
                    return toDto(s);
                }
                // rows == 0 means either insufficient inventory to apply delta (would go negative)
                // or concurrent update; if delta would make inventory negative, throw IllegalArgument
                Sku s = skuRepository.findById(id).orElseThrow(() -> new NotFoundException("Sku not found"));
                int attempted = s.getInventoryCount() + dto.getDelta();
                if (attempted < 0) {
                    throw new IllegalArgumentException("Inventory cannot be negative");
                }
                // otherwise concurrent update; retry
                if (attempt >= maxAttempts) {
                    throw new OptimisticLockingFailureException("Could not update inventory after retries");
                }
                try {
                    Thread.sleep(50L * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during retry", ie);
                }
            } catch (OptimisticLockingFailureException | OptimisticLockException e) {
                if (attempt >= maxAttempts) {
                    throw e;
                }
                try {
                    Thread.sleep(50L * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during optimistic lock retry", ie);
                }
            }
        }
    }

    private SkuDTO toDto(Sku s) {
        SkuDTO dto = new SkuDTO();
        dto.setId(s.getId());
        dto.setCode(s.getCode());
        dto.setBarcode(s.getBarcode());
        dto.setPrice(s.getPrice());
        dto.setInventoryCount(s.getInventoryCount());
        dto.setActive(s.getActive());
        dto.setProductId(s.getProduct() != null ? s.getProduct().getId() : null);
        return dto;
    }
}
