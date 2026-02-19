package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import org.jboss.logging.Logger;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private static final Logger LOGGER = Logger.getLogger(CreateWarehouseUseCase.class);

  private final WarehouseStore warehouseStore;
  private final WarehouseValidator validator;

  public CreateWarehouseUseCase(WarehouseStore warehouseStore, WarehouseValidator validator) {
    this.warehouseStore = warehouseStore;
    this.validator = validator;
  }

  @Override
  public void create(Warehouse warehouse) {
    validator.validateBusinessUnitCodeUnique(warehouse.businessUnitCode);
    validator.validateLocationConstraints(warehouse, null);
    validator.validateStockWithinCapacity(warehouse);

    warehouse.createdAt = LocalDateTime.now();
    warehouseStore.create(warehouse);
    LOGGER.infof(
        "Warehouse created: code=%s, location=%s, capacity=%d",
        warehouse.businessUnitCode, warehouse.location, warehouse.capacity);
  }
}
