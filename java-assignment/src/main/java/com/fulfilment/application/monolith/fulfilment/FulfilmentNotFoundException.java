package com.fulfilment.application.monolith.fulfilment;

import com.fulfilment.application.monolith.exceptions.DomainNotFoundException;

public class FulfilmentNotFoundException extends DomainNotFoundException {

  public FulfilmentNotFoundException(String message) {
    super(message);
  }
}
