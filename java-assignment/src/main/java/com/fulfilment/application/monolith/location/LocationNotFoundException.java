package com.fulfilment.application.monolith.location;

import com.fulfilment.application.monolith.exceptions.DomainValidationException;

public class LocationNotFoundException extends DomainValidationException {

  private static final String ERROR_MESSAGE_FORMAT = "Location with identifier '%s' not found";

  public LocationNotFoundException(String identifier) {
    super(String.format(ERROR_MESSAGE_FORMAT, identifier));
  }
}
