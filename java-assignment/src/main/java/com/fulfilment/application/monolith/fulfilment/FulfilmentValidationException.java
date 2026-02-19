package com.fulfilment.application.monolith.fulfilment;

import com.fulfilment.application.monolith.exceptions.DomainValidationException;

public class FulfilmentValidationException extends DomainValidationException {

  public FulfilmentValidationException(String message) {
    super(message);
  }
}
