package com.example.inventory.repository;

import com.example.inventory.entity.Sku;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface SkuRepository extends JpaRepository<Sku, Long> {
    Optional<Sku> findByCode(String code);
    Optional<Sku> findByBarcode(String barcode);
    Page<Sku> findByProductId(Long productId, Pageable pageable);

    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query(value = "UPDATE skus SET inventory_count = inventory_count + :delta, updated_at = CURRENT_TIMESTAMP WHERE id = :id AND inventory_count + :delta >= 0", nativeQuery = true)
    int updateInventoryIfNonNegative(@Param("id") Long id, @Param("delta") int delta);
}
