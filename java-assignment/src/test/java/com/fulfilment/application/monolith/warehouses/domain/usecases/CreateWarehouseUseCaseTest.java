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

class CreateWarehouseUseCaseTest {

  private WarehouseStore warehouseStore;
  private LocationResolver locationResolver;
  private CreateWarehouseUseCase useCase;

  @BeforeEach
  void setUp() {
    warehouseStore = mock(WarehouseStore.class);
    locationResolver = mock(LocationResolver.class);
    useCase = new CreateWarehouseUseCase(warehouseStore, locationResolver);
  }

  @Test
  void create_shouldCreateWarehouse_whenAllValidationsPass() {
    Warehouse warehouse = buildWarehouse("NEW.001", "AMSTERDAM-001", 20, 5);
    when(warehouseStore.findByBusinessUnitCode("NEW.001")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001"))
        .thenReturn(new Location("AMSTERDAM-001", 5, 100));
    when(warehouseStore.getAll()).thenReturn(List.of());

    useCase.create(warehouse);

    assertNotNull(warehouse.createdAt);
    verify(warehouseStore).create(warehouse);
  }

  @Test
  void create_shouldThrowValidationException_whenBusinessUnitCodeAlreadyExists() {
    Warehouse warehouse = buildWarehouse("MWH.001", "AMSTERDAM-001", 20, 5);
    Warehouse existing = buildWarehouse("MWH.001", "ZWOLLE-001", 40, 10);
    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existing);

    WarehouseValidationException exception =
        assertThrows(WarehouseValidationException.class, () -> useCase.create(warehouse));

    assertTrue(exception.getMessage().contains("already exists"));
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void create_shouldThrowLocationNotFoundException_whenLocationDoesNotExist() {
    Warehouse warehouse = buildWarehouse("NEW.001", "INVALID-LOC", 20, 5);
    when(warehouseStore.findByBusinessUnitCode("NEW.001")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("INVALID-LOC"))
        .thenThrow(new LocationNotFoundException("INVALID-LOC"));

    assertThrows(LocationNotFoundException.class, () -> useCase.create(warehouse));
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void create_shouldThrowValidationException_whenMaxWarehousesReachedAtLocation() {
    Warehouse warehouse = buildWarehouse("NEW.001", "ZWOLLE-001", 20, 5);
    when(warehouseStore.findByBusinessUnitCode("NEW.001")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("ZWOLLE-001"))
        .thenReturn(new Location("ZWOLLE-001", 1, 40));
    Warehouse existingAtLocation = buildWarehouse("MWH.001", "ZWOLLE-001", 30, 10);
    when(warehouseStore.getAll()).thenReturn(List.of(existingAtLocation));

    WarehouseValidationException exception =
        assertThrows(WarehouseValidationException.class, () -> useCase.create(warehouse));

    assertTrue(exception.getMessage().contains("Maximum number of warehouses"));
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void create_shouldThrowValidationException_whenCapacityExceedsLocationMax() {
    Warehouse warehouse = buildWarehouse("NEW.001", "AMSTERDAM-001", 80, 5);
    when(warehouseStore.findByBusinessUnitCode("NEW.001")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001"))
        .thenReturn(new Location("AMSTERDAM-001", 5, 100));
    Warehouse existing = buildWarehouse("MWH.012", "AMSTERDAM-001", 50, 5);
    when(warehouseStore.getAll()).thenReturn(List.of(existing));

    WarehouseValidationException exception =
        assertThrows(WarehouseValidationException.class, () -> useCase.create(warehouse));

    assertTrue(exception.getMessage().contains("exceeds maximum capacity"));
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void create_shouldThrowValidationException_whenCapacityCannotHandleStock() {
    Warehouse warehouse = buildWarehouse("NEW.001", "AMSTERDAM-001", 10, 20);
    when(warehouseStore.findByBusinessUnitCode("NEW.001")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001"))
        .thenReturn(new Location("AMSTERDAM-001", 5, 100));
    when(warehouseStore.getAll()).thenReturn(List.of());

    WarehouseValidationException exception =
        assertThrows(WarehouseValidationException.class, () -> useCase.create(warehouse));

    assertTrue(exception.getMessage().contains("cannot handle"));
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
