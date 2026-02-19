package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fulfilment.application.monolith.location.LocationNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CreateWarehouseUseCaseTest {

  private WarehouseStore warehouseStore;
  private WarehouseValidator validator;
  private CreateWarehouseUseCase useCase;

  @BeforeEach
  void setUp() {
    warehouseStore = mock(WarehouseStore.class);
    validator = mock(WarehouseValidator.class);
    useCase = new CreateWarehouseUseCase(warehouseStore, validator);
  }

  @Test
  void create_shouldValidateAndCreateWarehouse_whenAllValidationsPass() {
    Warehouse warehouse = buildWarehouse("NEW.001", "AMSTERDAM-001", 20, 5);

    useCase.create(warehouse);

    verify(validator).validateBusinessUnitCodeUnique("NEW.001");
    verify(validator).validateLocationConstraints(warehouse, null);
    verify(validator).validateStockWithinCapacity(warehouse);
    assertNotNull(warehouse.createdAt);
    verify(warehouseStore).create(warehouse);
  }

  @Test
  void create_shouldNotCreate_whenBusinessUnitCodeValidationFails() {
    Warehouse warehouse = buildWarehouse("MWH.001", "AMSTERDAM-001", 20, 5);
    doThrow(new WarehouseValidationException("already exists"))
        .when(validator)
        .validateBusinessUnitCodeUnique("MWH.001");

    assertThrows(WarehouseValidationException.class, () -> useCase.create(warehouse));
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void create_shouldNotCreate_whenLocationValidationFails() {
    Warehouse warehouse = buildWarehouse("NEW.001", "INVALID-LOC", 20, 5);
    doThrow(new LocationNotFoundException("INVALID-LOC"))
        .when(validator)
        .validateLocationConstraints(warehouse, null);

    assertThrows(LocationNotFoundException.class, () -> useCase.create(warehouse));
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void create_shouldNotCreate_whenStockValidationFails() {
    Warehouse warehouse = buildWarehouse("NEW.001", "AMSTERDAM-001", 10, 20);
    doThrow(new WarehouseValidationException("cannot handle"))
        .when(validator)
        .validateStockWithinCapacity(warehouse);

    assertThrows(WarehouseValidationException.class, () -> useCase.create(warehouse));
    verify(warehouseStore, never()).create(any());
  }

  private Warehouse buildWarehouse(
      String businessUnitCode, String location, int capacity, int stock) {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = businessUnitCode;
    warehouse.location = location;
    warehouse.capacity = capacity;
    warehouse.stock = stock;
    return warehouse;
  }
}
