package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ArchiveWarehouseUseCaseTest {

  private WarehouseStore warehouseStore;
  private ArchiveWarehouseUseCase useCase;

  @BeforeEach
  void setUp() {
    warehouseStore = mock(WarehouseStore.class);
    useCase = new ArchiveWarehouseUseCase(warehouseStore);
  }

  @Test
  void archive_shouldSetArchivedAtAndUpdate_whenWarehouseExists() {
    Warehouse input = new Warehouse();
    input.businessUnitCode = "MWH.001";

    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "MWH.001";
    existing.location = "ZWOLLE-001";
    existing.capacity = 40;
    existing.stock = 10;

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existing);

    useCase.archive(input);

    assertNotNull(existing.archivedAt);
    verify(warehouseStore).update(existing);
  }

  @Test
  void archive_shouldThrowNotFoundException_whenWarehouseDoesNotExist() {
    Warehouse input = new Warehouse();
    input.businessUnitCode = "NON-EXISTENT";

    when(warehouseStore.findByBusinessUnitCode("NON-EXISTENT")).thenReturn(null);

    assertThrows(WarehouseNotFoundException.class, () -> useCase.archive(input));
    verify(warehouseStore, never()).update(any());
  }
}
