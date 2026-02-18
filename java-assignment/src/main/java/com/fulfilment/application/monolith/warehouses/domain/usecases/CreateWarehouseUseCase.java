package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public CreateWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void create(Warehouse warehouse) {
    Warehouse existing = warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode);
    if (existing != null) {
      throw new WarehouseValidationException(
          "Business unit code '" + warehouse.businessUnitCode + "' already exists");
    }

    Location location = locationResolver.resolveByIdentifier(warehouse.location);

    List<Warehouse> warehousesAtLocation =
        warehouseStore.getAll().stream()
            .filter(w -> warehouse.location.equals(w.location))
            .toList();

    if (warehousesAtLocation.size() >= location.maxNumberOfWarehouses) {
      throw new WarehouseValidationException(
          "Maximum number of warehouses reached at location '" + warehouse.location + "'");
    }

    int totalCapacity = warehousesAtLocation.stream().mapToInt(w -> w.capacity).sum();
    if (totalCapacity + warehouse.capacity > location.maxCapacity) {
      throw new WarehouseValidationException(
          "Warehouse capacity exceeds maximum capacity for location '" + warehouse.location + "'");
    }

    if (warehouse.stock != null && warehouse.capacity < warehouse.stock) {
      throw new WarehouseValidationException(
          "Warehouse capacity cannot handle the specified stock");
    }

    warehouse.createdAt = LocalDateTime.now();
    warehouseStore.create(warehouse);
  }
}
