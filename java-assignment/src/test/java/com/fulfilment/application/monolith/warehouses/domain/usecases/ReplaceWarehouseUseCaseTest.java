package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fulfilment.application.monolith.location.LocationNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReplaceWarehouseUseCaseTest {

  private WarehouseStore warehouseStore;
  private WarehouseValidator validator;
  private ReplaceWarehouseUseCase useCase;

  @BeforeEach
  void setUp() {
    warehouseStore = mock(WarehouseStore.class);
    validator = mock(WarehouseValidator.class);
    useCase = new ReplaceWarehouseUseCase(warehouseStore, validator);
  }

  @Test
  void replace_shouldArchiveOldAndCreateNew_whenAllValidationsPass() {
    Warehouse existing = buildWarehouse("MWH.001", "ZWOLLE-001", 30, 10);
    Warehouse replacement = buildWarehouse("MWH.001", "AMSTERDAM-001", 50, 10);
    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existing);

    useCase.replace(replacement);

    assertNotNull(existing.archivedAt);
    verify(warehouseStore).update(existing);
    assertNotNull(replacement.createdAt);
    verify(warehouseStore).create(replacement);
    verify(validator).validateLocationConstraints(replacement, "MWH.001");
  }

  @Test
  void replace_shouldThrowNotFoundException_whenWarehouseDoesNotExist() {
    Warehouse replacement = buildWarehouse("NON-EXISTENT", "AMSTERDAM-001", 50, 10);
    when(warehouseStore.findByBusinessUnitCode("NON-EXISTENT")).thenReturn(null);

    assertThrows(WarehouseNotFoundException.class, () -> useCase.replace(replacement));
    verify(warehouseStore, never()).update(any());
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void replace_shouldThrow_whenNewCapacityCannotAccommodateExistingStock() {
    Warehouse existing = buildWarehouse("MWH.001", "ZWOLLE-001", 30, 25);
    Warehouse replacement = buildWarehouse("MWH.001", "AMSTERDAM-001", 20, 25);
    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existing);

    WarehouseValidationException exception =
        assertThrows(WarehouseValidationException.class, () -> useCase.replace(replacement));

    assertTrue(exception.getMessage().contains("cannot accommodate"));
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void replace_shouldThrow_whenStockDoesNotMatch() {
    Warehouse existing = buildWarehouse("MWH.001", "ZWOLLE-001", 30, 10);
    Warehouse replacement = buildWarehouse("MWH.001", "AMSTERDAM-001", 50, 20);
    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existing);

    WarehouseValidationException exception =
        assertThrows(WarehouseValidationException.class, () -> useCase.replace(replacement));

    assertTrue(exception.getMessage().contains("must match"));
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void replace_shouldNotCreate_whenLocationValidationFails() {
    Warehouse existing = buildWarehouse("MWH.001", "ZWOLLE-001", 30, 10);
    Warehouse replacement = buildWarehouse("MWH.001", "INVALID-LOC", 50, 10);
    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existing);
    doThrow(new LocationNotFoundException("INVALID-LOC"))
        .when(validator)
        .validateLocationConstraints(replacement, "MWH.001");

    assertThrows(LocationNotFoundException.class, () -> useCase.replace(replacement));
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
