package com.fulfilment.application.monolith.exceptions;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
public class DomainValidationExceptionMapper implements ExceptionMapper<DomainValidationException> {

  private static final Logger LOGGER = Logger.getLogger(DomainValidationExceptionMapper.class);

  @Override
  public Response toResponse(DomainValidationException exception) {
    LOGGER.warnf("Validation error: %s", exception.getMessage());
    return Response.status(400).entity(new ErrorResponse(exception.getMessage())).build();
  }
}
