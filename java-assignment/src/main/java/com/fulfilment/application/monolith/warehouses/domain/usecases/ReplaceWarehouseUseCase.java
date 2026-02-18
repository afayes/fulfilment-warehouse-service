package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.List;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private static final Logger LOGGER = Logger.getLogger(ReplaceWarehouseUseCase.class);

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void replace(Warehouse newWarehouse) {
    Warehouse existing = warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);
    if (existing == null) {
      throw new WarehouseNotFoundException(newWarehouse.businessUnitCode);
    }

    if (newWarehouse.capacity < existing.stock) {
      throw new WarehouseValidationException(
          "New warehouse capacity cannot accommodate the stock from the replaced warehouse");
    }

    if (!newWarehouse.stock.equals(existing.stock)) {
      throw new WarehouseValidationException(
          "Stock of new warehouse must match the stock of the replaced warehouse");
    }

    Location location = locationResolver.resolveByIdentifier(newWarehouse.location);

    List<Warehouse> warehousesAtLocation =
        warehouseStore.getAll().stream()
            .filter(w -> newWarehouse.location.equals(w.location))
            .filter(w -> !w.businessUnitCode.equals(existing.businessUnitCode)) // exclude the existing warehouse so that we don't count it twice
            .toList();

    if (warehousesAtLocation.size() >= location.maxNumberOfWarehouses) {
      throw new WarehouseValidationException(
          "Maximum number of warehouses reached at location '" + newWarehouse.location + "'");
    }

    int totalCapacity = warehousesAtLocation.stream().mapToInt(w -> w.capacity).sum();
    if (totalCapacity + newWarehouse.capacity > location.maxCapacity) {
      throw new WarehouseValidationException(
          "Warehouse capacity exceeds maximum capacity for location '"
              + newWarehouse.location
              + "'");
    }

    existing.archivedAt = LocalDateTime.now();
    warehouseStore.update(existing);

    newWarehouse.createdAt = LocalDateTime.now();
    warehouseStore.create(newWarehouse);
    LOGGER.infof(
        "Warehouse replaced: code=%s, oldLocation=%s, newLocation=%s",
        newWarehouse.businessUnitCode, existing.location, newWarehouse.location);
  }
}
