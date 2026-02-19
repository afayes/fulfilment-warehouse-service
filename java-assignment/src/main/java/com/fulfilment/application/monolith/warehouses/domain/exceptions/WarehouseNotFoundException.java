package com.fulfilment.application.monolith.warehouses.domain.exceptions;

import com.fulfilment.application.monolith.exceptions.DomainNotFoundException;

public class WarehouseNotFoundException extends DomainNotFoundException {

  public WarehouseNotFoundException(String identifier) {
    super("Warehouse with identifier '" + identifier + "' not found");
  }
}
