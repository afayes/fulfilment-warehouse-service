package com.fulfilment.application.monolith.fulfilment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FulfilmentServiceTest {

  private FulfilmentRepository fulfilmentRepository;
  private FulfilmentService service;

  @BeforeEach
  void setUp() {
    fulfilmentRepository = mock(FulfilmentRepository.class);
    service = new FulfilmentService();
    service.fulfilmentRepository = fulfilmentRepository;
  }

  // --- validateNoDuplicate ---

  @Test
  void validateNoDuplicate_shouldPass_whenAssociationDoesNotExist() {
    when(fulfilmentRepository.exists(1L, 1L, "MWH.001")).thenReturn(false);

    assertDoesNotThrow(() -> service.validateNoDuplicate(buildFulfilment(1L, 1L, "MWH.001")));
  }

  @Test
  void validateNoDuplicate_shouldThrow_whenAssociationAlreadyExists() {
    when(fulfilmentRepository.exists(1L, 1L, "MWH.001")).thenReturn(true);

    FulfilmentValidationException exception =
        assertThrows(
            FulfilmentValidationException.class,
            () -> service.validateNoDuplicate(buildFulfilment(1L, 1L, "MWH.001")));

    assertTrue(exception.getMessage().contains("already exists"));
  }

  // --- validateMaxWarehousesPerProductPerStore ---

  @Test
  void validateMaxWarehousesPerProductPerStore_shouldPass_whenUnderLimit() {
    when(fulfilmentRepository.countWarehousesForProductInStore(1L, 1L)).thenReturn(1L);

    assertDoesNotThrow(
        () ->
            service.validateMaxWarehousesPerProductPerStore(buildFulfilment(1L, 1L, "MWH.001")));
  }

  @Test
  void validateMaxWarehousesPerProductPerStore_shouldThrow_whenLimitReached() {
    when(fulfilmentRepository.countWarehousesForProductInStore(1L, 1L)).thenReturn(2L);

    FulfilmentValidationException exception =
        assertThrows(
            FulfilmentValidationException.class,
            () ->
                service.validateMaxWarehousesPerProductPerStore(
                    buildFulfilment(1L, 1L, "MWH.003")));

    assertTrue(exception.getMessage().contains("maximum of 2 warehouses per store"));
  }

  // --- validateMaxWarehousesPerStore ---

  @Test
  void validateMaxWarehousesPerStore_shouldPass_whenWarehouseAlreadyAssociatedToStore() {
    Fulfilment existing = buildFulfilment(1L, 2L, "MWH.001");
    existing.id = 10L;
    when(fulfilmentRepository.findByStoreId(1L)).thenReturn(List.of(existing));

    assertDoesNotThrow(
        () -> service.validateMaxWarehousesPerStore(buildFulfilment(1L, 1L, "MWH.001")));
  }

  @Test
  void validateMaxWarehousesPerStore_shouldPass_whenNewWarehouseAndUnderLimit() {
    when(fulfilmentRepository.findByStoreId(1L)).thenReturn(List.of());
    when(fulfilmentRepository.countDistinctWarehousesForStore(1L)).thenReturn(2L);

    assertDoesNotThrow(
        () -> service.validateMaxWarehousesPerStore(buildFulfilment(1L, 1L, "MWH.001")));
  }

  @Test
  void validateMaxWarehousesPerStore_shouldThrow_whenNewWarehouseAndLimitReached() {
    Fulfilment e1 = buildFulfilment(1L, 1L, "MWH.001");
    e1.id = 1L;
    Fulfilment e2 = buildFulfilment(1L, 2L, "MWH.012");
    e2.id = 2L;
    Fulfilment e3 = buildFulfilment(1L, 3L, "MWH.023");
    e3.id = 3L;
    when(fulfilmentRepository.findByStoreId(1L)).thenReturn(List.of(e1, e2, e3));
    when(fulfilmentRepository.countDistinctWarehousesForStore(1L)).thenReturn(3L);

    FulfilmentValidationException exception =
        assertThrows(
            FulfilmentValidationException.class,
            () -> service.validateMaxWarehousesPerStore(buildFulfilment(1L, 1L, "MWH.NEW")));

    assertTrue(exception.getMessage().contains("maximum of 3 warehouses"));
  }

  // --- validateMaxProductsPerWarehouse ---

  @Test
  void validateMaxProductsPerWarehouse_shouldPass_whenProductAlreadyInWarehouse() {
    Fulfilment existing = buildFulfilment(2L, 1L, "MWH.001");
    existing.id = 10L;
    when(fulfilmentRepository.findByWarehouseBusinessUnitCode("MWH.001"))
        .thenReturn(List.of(existing));

    assertDoesNotThrow(
        () -> service.validateMaxProductsPerWarehouse(buildFulfilment(1L, 1L, "MWH.001")));
  }

  @Test
  void validateMaxProductsPerWarehouse_shouldPass_whenNewProductAndUnderLimit() {
    when(fulfilmentRepository.findByWarehouseBusinessUnitCode("MWH.001"))
        .thenReturn(List.of());
    when(fulfilmentRepository.countDistinctProductsForWarehouse("MWH.001")).thenReturn(4L);

    assertDoesNotThrow(
        () -> service.validateMaxProductsPerWarehouse(buildFulfilment(1L, 1L, "MWH.001")));
  }

  @Test
  void validateMaxProductsPerWarehouse_shouldThrow_whenNewProductAndLimitReached() {
    when(fulfilmentRepository.findByWarehouseBusinessUnitCode("MWH.001"))
        .thenReturn(List.of());
    when(fulfilmentRepository.countDistinctProductsForWarehouse("MWH.001")).thenReturn(5L);

    FulfilmentValidationException exception =
        assertThrows(
            FulfilmentValidationException.class,
            () -> service.validateMaxProductsPerWarehouse(buildFulfilment(1L, 1L, "MWH.001")));

    assertTrue(exception.getMessage().contains("maximum of 5 types of products"));
  }

  private Fulfilment buildFulfilment(Long storeId, Long productId, String warehouseCode) {
    Fulfilment f = new Fulfilment();
    f.storeId = storeId;
    f.productId = productId;
    f.warehouseBusinessUnitCode = warehouseCode;
    return f;
  }
}
