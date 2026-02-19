package com.fulfilment.application.monolith.exceptions;

public class DomainNotFoundException extends RuntimeException {

  public DomainNotFoundException(String message) {
    super(message);
  }
}
