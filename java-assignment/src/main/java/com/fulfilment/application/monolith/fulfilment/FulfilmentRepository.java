package com.fulfilment.application.monolith.fulfilment;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class FulfilmentRepository implements PanacheRepository<Fulfilment> {

  public List<Fulfilment> findByStoreId(Long storeId) {
    return find("storeId", storeId).list();
  }

  public List<Fulfilment> findByProductId(Long productId) {
    return find("productId", productId).list();
  }

  public List<Fulfilment> findByWarehouseBusinessUnitCode(String warehouseBusinessUnitCode) {
    return find("warehouseBusinessUnitCode", warehouseBusinessUnitCode).list();
  }

  public long countWarehousesForProductInStore(Long storeId, Long productId) {
    return count("storeId = ?1 and productId = ?2", storeId, productId);
  }

  public long countDistinctWarehousesForStore(Long storeId) {
    return find("storeId", storeId).stream()
        .map(f -> f.warehouseBusinessUnitCode)
        .distinct()
        .count();
  }

  public long countDistinctProductsForWarehouse(String warehouseBusinessUnitCode) {
    return find("warehouseBusinessUnitCode", warehouseBusinessUnitCode).stream()
        .map(f -> f.productId)
        .distinct()
        .count();
  }

  public boolean exists(Long storeId, Long productId, String warehouseBusinessUnitCode) {
    return count(
            "storeId = ?1 and productId = ?2 and warehouseBusinessUnitCode = ?3",
            storeId,
            productId,
            warehouseBusinessUnitCode)
        > 0;
  }
}
