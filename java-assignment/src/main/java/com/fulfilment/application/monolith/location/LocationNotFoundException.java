package com.fulfilment.application.monolith.location;

public class LocationNotFoundException extends RuntimeException {

  private static final String ERROR_MESSAGE_FORMAT = "Location with identifier '%s' not found";

  public LocationNotFoundException(String identifier) {
    super(String.format(ERROR_MESSAGE_FORMAT, identifier));
  }
}
