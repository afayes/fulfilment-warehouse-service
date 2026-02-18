package com.fulfilment.application.monolith.fulfilment;

public class FulfilmentNotFoundException extends RuntimeException {

  public FulfilmentNotFoundException(String message) {
    super(message);
  }
}
