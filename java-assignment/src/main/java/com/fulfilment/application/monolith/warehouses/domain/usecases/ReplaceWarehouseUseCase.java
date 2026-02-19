package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private static final Logger LOGGER = Logger.getLogger(ReplaceWarehouseUseCase.class);

  private final WarehouseStore warehouseStore;
  private final WarehouseValidator validator;

  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore, WarehouseValidator validator) {
    this.warehouseStore = warehouseStore;
    this.validator = validator;
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

    validator.validateLocationConstraints(newWarehouse, existing.businessUnitCode);

    existing.archivedAt = LocalDateTime.now();
    warehouseStore.update(existing);

    newWarehouse.createdAt = LocalDateTime.now();
    warehouseStore.create(newWarehouse);
    LOGGER.infof(
        "Warehouse replaced: code=%s, oldLocation=%s, newLocation=%s",
        newWarehouse.businessUnitCode, existing.location, newWarehouse.location);
  }
}
