package com.example.inventory.service;

import com.example.inventory.dto.sku.InventoryAdjustDTO;
import com.example.inventory.dto.sku.SkuDTO;
import com.example.inventory.entity.InventoryAdjustment;
import com.example.inventory.entity.Sku;
import com.example.inventory.repository.InventoryAdjustmentRepository;
import com.example.inventory.repository.ProductRepository;
import com.example.inventory.repository.SkuRepository;
import com.example.inventory.service.impl.SkuServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class SkuServiceOptimisticRetryTest {

    private SkuRepository skuRepository;
    private ProductRepository productRepository;
    private InventoryAdjustmentRepository adjustmentRepository;
    private SkuServiceImpl skuService;

    @BeforeEach
    public void setUp() {
        skuRepository = mock(SkuRepository.class);
        productRepository = mock(ProductRepository.class);
        adjustmentRepository = mock(InventoryAdjustmentRepository.class);
        skuService = new SkuServiceImpl(skuRepository, productRepository, adjustmentRepository);
    }

    @Test
    public void adjustInventory_retriesOnOptimisticLock_thenSucceeds_andRecordsAdjustment() {
        // Prepare Sku states: before update inventory = 5, after update inventory = 8
        Sku before = new Sku();
        before.setId(10L);
        before.setInventoryCount(5);

        Sku after = new Sku();
        after.setId(10L);
        after.setInventoryCount(8);

        // simulate updateInventoryIfNonNegative: first call returns 0 (no rows updated), second returns 1
        when(skuRepository.updateInventoryIfNonNegative(10L, 3)).thenReturn(0).thenReturn(1);

        // findById should return 'before' on first relevant call, and 'after' after successful update
        AtomicInteger calls = new AtomicInteger(0);
        when(skuRepository.findById(10L)).thenAnswer((Answer<Optional<Sku>>) invocation -> {
            int c = calls.incrementAndGet();
            if (c == 1) {
                return Optional.of(before);
            }
            return Optional.of(after);
        });

        InventoryAdjustDTO adj = new InventoryAdjustDTO();
        adj.setDelta(3);
        adj.setReason("test");

        SkuDTO result = skuService.adjustInventory(10L, adj);

        assertThat(result).isNotNull();
        assertThat(result.getInventoryCount()).isEqualTo(8);

        verify(skuRepository, atLeast(2)).updateInventoryIfNonNegative(10L, 3);
        verify(skuRepository, atLeast(1)).findById(10L);
        verify(adjustmentRepository, times(1)).save(any(InventoryAdjustment.class));
    }
}
