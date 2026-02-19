package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fulfilment.application.monolith.location.LocationNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WarehouseValidatorTest {

  private WarehouseStore warehouseStore;
  private LocationResolver locationResolver;
  private WarehouseValidator validator;

  @BeforeEach
  void setUp() {
    warehouseStore = mock(WarehouseStore.class);
    locationResolver = mock(LocationResolver.class);
    validator = new WarehouseValidator(warehouseStore, locationResolver);
  }

  // --- validateBusinessUnitCodeUnique ---

  @Test
  void validateBusinessUnitCodeUnique_shouldPass_whenCodeDoesNotExist() {
    when(warehouseStore.findByBusinessUnitCode("NEW.001")).thenReturn(null);

    assertDoesNotThrow(() -> validator.validateBusinessUnitCodeUnique("NEW.001"));
  }

  @Test
  void validateBusinessUnitCodeUnique_shouldThrow_whenCodeAlreadyExists() {
    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(buildWarehouse("MWH.001", "ZWOLLE-001", 40, 10));

    WarehouseValidationException exception =
        assertThrows(
            WarehouseValidationException.class,
            () -> validator.validateBusinessUnitCodeUnique("MWH.001"));

    assertTrue(exception.getMessage().contains("already exists"));
  }

  // --- validateLocationConstraints ---

  @Test
  void validateLocationConstraints_shouldPass_whenAllConstraintsMet() {
    Warehouse warehouse = buildWarehouse("NEW.001", "AMSTERDAM-001", 20, 5);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001"))
        .thenReturn(new Location("AMSTERDAM-001", 5, 100));
    when(warehouseStore.getAll()).thenReturn(List.of());

    assertDoesNotThrow(() -> validator.validateLocationConstraints(warehouse, null));
  }

  @Test
  void validateLocationConstraints_shouldThrow_whenLocationDoesNotExist() {
    Warehouse warehouse = buildWarehouse("NEW.001", "INVALID-LOC", 20, 5);
    when(locationResolver.resolveByIdentifier("INVALID-LOC"))
        .thenThrow(new LocationNotFoundException("INVALID-LOC"));

    assertThrows(
        LocationNotFoundException.class,
        () -> validator.validateLocationConstraints(warehouse, null));
  }

  @Test
  void validateLocationConstraints_shouldThrow_whenMaxWarehousesReached() {
    Warehouse warehouse = buildWarehouse("NEW.001", "ZWOLLE-001", 20, 5);
    when(locationResolver.resolveByIdentifier("ZWOLLE-001"))
        .thenReturn(new Location("ZWOLLE-001", 1, 100));
    Warehouse existing = buildWarehouse("MWH.001", "ZWOLLE-001", 30, 10);
    when(warehouseStore.getAll()).thenReturn(List.of(existing));

    WarehouseValidationException exception =
        assertThrows(
            WarehouseValidationException.class,
            () -> validator.validateLocationConstraints(warehouse, null));

    assertTrue(exception.getMessage().contains("Maximum number of warehouses"));
  }

  @Test
  void validateLocationConstraints_shouldThrow_whenCapacityExceedsLocationMax() {
    Warehouse warehouse = buildWarehouse("NEW.001", "AMSTERDAM-001", 80, 5);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001"))
        .thenReturn(new Location("AMSTERDAM-001", 5, 100));
    Warehouse existing = buildWarehouse("MWH.012", "AMSTERDAM-001", 50, 5);
    when(warehouseStore.getAll()).thenReturn(List.of(existing));

    WarehouseValidationException exception =
        assertThrows(
            WarehouseValidationException.class,
            () -> validator.validateLocationConstraints(warehouse, null));

    assertTrue(exception.getMessage().contains("exceeds maximum capacity"));
  }

  @Test
  void validateLocationConstraints_shouldExcludeWarehouse_whenExcludeCodeProvided() {
    Warehouse warehouse = buildWarehouse("MWH.001", "ZWOLLE-001", 20, 5);
    when(locationResolver.resolveByIdentifier("ZWOLLE-001"))
        .thenReturn(new Location("ZWOLLE-001", 1, 100));
    Warehouse existing = buildWarehouse("MWH.001", "ZWOLLE-001", 30, 10);
    when(warehouseStore.getAll()).thenReturn(List.of(existing));

    assertDoesNotThrow(
        () -> validator.validateLocationConstraints(warehouse, "MWH.001"));
  }

  // --- validateStockWithinCapacity ---

  @Test
  void validateStockWithinCapacity_shouldPass_whenStockWithinCapacity() {
    Warehouse warehouse = buildWarehouse("NEW.001", "AMSTERDAM-001", 20, 5);

    assertDoesNotThrow(() -> validator.validateStockWithinCapacity(warehouse));
  }

  @Test
  void validateStockWithinCapacity_shouldPass_whenStockIsNull() {
    Warehouse warehouse = buildWarehouse("NEW.001", "AMSTERDAM-001", 20, 0);
    warehouse.stock = null;

    assertDoesNotThrow(() -> validator.validateStockWithinCapacity(warehouse));
  }

  @Test
  void validateStockWithinCapacity_shouldThrow_whenStockExceedsCapacity() {
    Warehouse warehouse = buildWarehouse("NEW.001", "AMSTERDAM-001", 10, 20);

    WarehouseValidationException exception =
        assertThrows(
            WarehouseValidationException.class,
            () -> validator.validateStockWithinCapacity(warehouse));

    assertTrue(exception.getMessage().contains("cannot handle"));
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
