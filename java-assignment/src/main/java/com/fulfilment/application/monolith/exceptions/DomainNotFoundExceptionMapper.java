package com.fulfilment.application.monolith.exceptions;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
public class DomainNotFoundExceptionMapper implements ExceptionMapper<DomainNotFoundException> {

  private static final Logger LOGGER = Logger.getLogger(DomainNotFoundExceptionMapper.class);

  @Override
  public Response toResponse(DomainNotFoundException exception) {
    LOGGER.warnf("Not found: %s", exception.getMessage());
    return Response.status(404).entity(new ErrorResponse(exception.getMessage())).build();
  }
}
