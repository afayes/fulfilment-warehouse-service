package com.fulfilment.application.monolith.fulfilment;

import com.fulfilment.application.monolith.products.ProductRepository;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

@ApplicationScoped
public class FulfilmentService {

  static final int MAX_WAREHOUSES_PER_PRODUCT_PER_STORE = 2;
  static final int MAX_WAREHOUSES_PER_STORE = 3;
  static final int MAX_PRODUCTS_PER_WAREHOUSE = 5;

  @Inject FulfilmentRepository fulfilmentRepository;

  @Inject ProductRepository productRepository;

  @Inject WarehouseStore warehouseStore;

  public List<Fulfilment> getAll() {
    return fulfilmentRepository.listAll();
  }

  public List<Fulfilment> getByStoreId(Long storeId) {
    if (Store.findById(storeId) == null) {
      throw new FulfilmentValidationException(
          "Store with id " + storeId + " does not exist");
    }
    return fulfilmentRepository.findByStoreId(storeId);
  }

  public Fulfilment create(Fulfilment fulfilment) {
    validate(fulfilment);
    fulfilmentRepository.persist(fulfilment);
    return fulfilment;
  }

  public void delete(Long id) {
    Fulfilment entity = fulfilmentRepository.findById(id);
    if (entity == null) {
      throw new FulfilmentValidationException("Fulfilment with id " + id + " does not exist");
    }
    fulfilmentRepository.delete(entity);
  }

  void validate(Fulfilment fulfilment) {
    validateEntitiesExist(fulfilment);
    validateNoDuplicate(fulfilment);
    validateMaxWarehousesPerProductPerStore(fulfilment);
    validateMaxWarehousesPerStore(fulfilment);
    validateMaxProductsPerWarehouse(fulfilment);
  }

  void validateEntitiesExist(Fulfilment fulfilment) {
    if (Store.findById(fulfilment.storeId) == null) {
      throw new FulfilmentValidationException(
          "Store with id " + fulfilment.storeId + " does not exist");
    }

    if (productRepository.findById(fulfilment.productId) == null) {
      throw new FulfilmentValidationException(
          "Product with id " + fulfilment.productId + " does not exist");
    }

    if (warehouseStore.findByBusinessUnitCode(fulfilment.warehouseBusinessUnitCode) == null) {
      throw new FulfilmentValidationException(
          "Warehouse with code '" + fulfilment.warehouseBusinessUnitCode + "' does not exist");
    }
  }

  void validateNoDuplicate(Fulfilment fulfilment) {
    if (fulfilmentRepository.exists(
        fulfilment.storeId, fulfilment.productId, fulfilment.warehouseBusinessUnitCode)) {
      throw new FulfilmentValidationException("This fulfilment association already exists");
    }
  }

  void validateMaxWarehousesPerProductPerStore(Fulfilment fulfilment) {
    long count =
        fulfilmentRepository.countWarehousesForProductInStore(
            fulfilment.storeId, fulfilment.productId);
    if (count >= MAX_WAREHOUSES_PER_PRODUCT_PER_STORE) {
      throw new FulfilmentValidationException(
          "Product can be fulfilled by a maximum of "
              + MAX_WAREHOUSES_PER_PRODUCT_PER_STORE
              + " warehouses per store");
    }
  }

  void validateMaxWarehousesPerStore(Fulfilment fulfilment) {
    boolean isNewWarehouseForStore =
        fulfilmentRepository.findByStoreId(fulfilment.storeId).stream()
            .noneMatch(
                f -> f.warehouseBusinessUnitCode.equals(fulfilment.warehouseBusinessUnitCode));
    if (isNewWarehouseForStore) {
      long count = fulfilmentRepository.countDistinctWarehousesForStore(fulfilment.storeId);
      if (count >= MAX_WAREHOUSES_PER_STORE) {
        throw new FulfilmentValidationException(
            "Store can be fulfilled by a maximum of " + MAX_WAREHOUSES_PER_STORE + " warehouses");
      }
    }
  }

  void validateMaxProductsPerWarehouse(Fulfilment fulfilment) {
    boolean isNewProductForWarehouse =
        fulfilmentRepository
            .findByWarehouseBusinessUnitCode(fulfilment.warehouseBusinessUnitCode)
            .stream()
            .noneMatch(f -> f.productId.equals(fulfilment.productId));
    if (isNewProductForWarehouse) {
      long count =
          fulfilmentRepository.countDistinctProductsForWarehouse(
              fulfilment.warehouseBusinessUnitCode);
      if (count >= MAX_PRODUCTS_PER_WAREHOUSE) {
        throw new FulfilmentValidationException(
            "Warehouse can store a maximum of "
                + MAX_PRODUCTS_PER_WAREHOUSE
                + " types of products");
      }
    }
  }
}
