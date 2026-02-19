package com.fulfilment.application.monolith.warehouses.domain.exceptions;

import com.fulfilment.application.monolith.exceptions.DomainValidationException;

public class WarehouseValidationException extends DomainValidationException {

  public WarehouseValidationException(String message) {
    super(message);
  }
}
