package com.fulfilment.application.monolith.fulfilment;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("fulfilment")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class FulfilmentResource {

  @Inject FulfilmentService fulfilmentService;

  @GET
  public List<Fulfilment> getAll() {
    return fulfilmentService.getAll();
  }

  @GET
  @Path("store/{storeId}")
  public List<Fulfilment> getByStore(Long storeId) {
    try {
      return fulfilmentService.getByStoreId(storeId);
    } catch (FulfilmentValidationException e) {
      throw new WebApplicationException(e.getMessage(), 404);
    }
  }

  @POST
  @Transactional
  public Response create(Fulfilment fulfilment) {
    if (fulfilment.id != null) {
      throw new WebApplicationException("Id was invalidly set on request", 422);
    }

    try {
      fulfilmentService.create(fulfilment);
    } catch (FulfilmentValidationException e) {
      throw new WebApplicationException(e.getMessage(), 400);
    }

    return Response.ok(fulfilment).status(201).build();
  }

  @DELETE
  @Path("{id}")
  @Transactional
  public Response delete(Long id) {
    try {
      fulfilmentService.delete(id);
    } catch (FulfilmentValidationException e) {
      throw new WebApplicationException(e.getMessage(), 404);
    }
    return Response.status(204).build();
  }
}
