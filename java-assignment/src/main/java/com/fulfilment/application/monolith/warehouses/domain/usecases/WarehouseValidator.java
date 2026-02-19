package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class WarehouseValidator {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public WarehouseValidator(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  public void validateBusinessUnitCodeUnique(String businessUnitCode) {
    if (warehouseStore.findByBusinessUnitCode(businessUnitCode) != null) {
      throw new WarehouseValidationException(
          "Business unit code '" + businessUnitCode + "' already exists");
    }
  }

  /**
   * @param excludeBusinessUnitCode business unit code to exclude from location counts (used during
   *     replacement to avoid counting the warehouse being replaced)
   */
  public void validateLocationConstraints(
      Warehouse warehouse, String excludeBusinessUnitCode) {
    Location location = locationResolver.resolveByIdentifier(warehouse.location);

    List<Warehouse> warehousesAtLocation =
        warehouseStore.getAll().stream()
            .filter(w -> warehouse.location.equals(w.location))
            .filter(
                w ->
                    excludeBusinessUnitCode == null
                        || !w.businessUnitCode.equals(excludeBusinessUnitCode))
            .toList();

    if (warehousesAtLocation.size() >= location.maxNumberOfWarehouses) {
      throw new WarehouseValidationException(
          "Maximum number of warehouses reached at location '" + warehouse.location + "'");
    }

    int totalCapacity = warehousesAtLocation.stream().mapToInt(w -> w.capacity).sum();
    if (totalCapacity + warehouse.capacity > location.maxCapacity) {
      throw new WarehouseValidationException(
          "Warehouse capacity exceeds maximum capacity for location '"
              + warehouse.location
              + "'");
    }
  }

  public void validateStockWithinCapacity(Warehouse warehouse) {
    if (warehouse.stock != null && warehouse.capacity < warehouse.stock) {
      throw new WarehouseValidationException(
          "Warehouse capacity cannot handle the specified stock");
    }
  }
}
