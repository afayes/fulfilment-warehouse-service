package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fulfilment.application.monolith.location.LocationNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReplaceWarehouseUseCaseTest {

  private WarehouseStore warehouseStore;
  private LocationResolver locationResolver;
  private ReplaceWarehouseUseCase useCase;

  @BeforeEach
  void setUp() {
    warehouseStore = mock(WarehouseStore.class);
    locationResolver = mock(LocationResolver.class);
    useCase = new ReplaceWarehouseUseCase(warehouseStore, locationResolver);
  }

  @Test
  void replace_shouldArchiveOldAndCreateNew_whenAllValidationsPass() {
    Warehouse existing = buildWarehouse("MWH.001", "ZWOLLE-001", 30, 10);
    Warehouse replacement = buildWarehouse("MWH.001", "AMSTERDAM-001", 50, 10);

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existing);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001"))
        .thenReturn(new Location("AMSTERDAM-001", 5, 100));
    when(warehouseStore.getAll()).thenReturn(List.of(existing));

    useCase.replace(replacement);

    assertNotNull(existing.archivedAt);
    verify(warehouseStore).update(existing);
    assertNotNull(replacement.createdAt);
    verify(warehouseStore).create(replacement);
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
  void replace_shouldThrowValidationException_whenNewCapacityCannotAccommodateExistingStock() {
    Warehouse existing = buildWarehouse("MWH.001", "ZWOLLE-001", 30, 25);
    Warehouse replacement = buildWarehouse("MWH.001", "AMSTERDAM-001", 20, 25);

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existing);

    WarehouseValidationException exception =
        assertThrows(WarehouseValidationException.class, () -> useCase.replace(replacement));

    assertTrue(exception.getMessage().contains("cannot accommodate"));
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void replace_shouldThrowValidationException_whenStockDoesNotMatch() {
    Warehouse existing = buildWarehouse("MWH.001", "ZWOLLE-001", 30, 10);
    Warehouse replacement = buildWarehouse("MWH.001", "AMSTERDAM-001", 50, 20);

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existing);

    WarehouseValidationException exception =
        assertThrows(WarehouseValidationException.class, () -> useCase.replace(replacement));

    assertTrue(exception.getMessage().contains("must match"));
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void replace_shouldThrowLocationNotFoundException_whenLocationDoesNotExist() {
    Warehouse existing = buildWarehouse("MWH.001", "ZWOLLE-001", 30, 10);
    Warehouse replacement = buildWarehouse("MWH.001", "INVALID-LOC", 50, 10);

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existing);
    when(locationResolver.resolveByIdentifier("INVALID-LOC"))
        .thenThrow(new LocationNotFoundException("INVALID-LOC"));

    assertThrows(LocationNotFoundException.class, () -> useCase.replace(replacement));
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void replace_shouldThrowValidationException_whenMaxWarehousesReachedAtNewLocation() {
    Warehouse existing = buildWarehouse("MWH.001", "ZWOLLE-001", 30, 10);
    Warehouse replacement = buildWarehouse("MWH.001", "TILBURG-001", 35, 10);
    Warehouse atTilburg = buildWarehouse("MWH.023", "TILBURG-001", 30, 27);

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existing);
    when(locationResolver.resolveByIdentifier("TILBURG-001"))
        .thenReturn(new Location("TILBURG-001", 1, 40));
    when(warehouseStore.getAll()).thenReturn(List.of(existing, atTilburg));

    WarehouseValidationException exception =
        assertThrows(WarehouseValidationException.class, () -> useCase.replace(replacement));

    assertTrue(exception.getMessage().contains("Maximum number of warehouses"));
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void replace_shouldThrowValidationException_whenCapacityExceedsLocationMax() {
    Warehouse existing = buildWarehouse("MWH.001", "ZWOLLE-001", 30, 10);
    Warehouse replacement = buildWarehouse("MWH.001", "AMSTERDAM-001", 80, 10);
    Warehouse atAmsterdam = buildWarehouse("MWH.012", "AMSTERDAM-001", 50, 5);

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existing);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001"))
        .thenReturn(new Location("AMSTERDAM-001", 5, 100));
    when(warehouseStore.getAll()).thenReturn(List.of(existing, atAmsterdam));

    WarehouseValidationException exception =
        assertThrows(WarehouseValidationException.class, () -> useCase.replace(replacement));

    assertTrue(exception.getMessage().contains("exceeds maximum capacity"));
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
