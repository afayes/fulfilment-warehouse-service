package com.fulfilment.application.monolith.warehouses.domain.exceptions;

public class WarehouseNotFoundException extends RuntimeException {

  public WarehouseNotFoundException(String identifier) {
    super("Warehouse with identifier '" + identifier + "' not found");
  }
}
