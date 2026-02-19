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
import org.jboss.logging.Logger;

@Path("fulfilment")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class FulfilmentResource {

  private static final Logger LOGGER = Logger.getLogger(FulfilmentResource.class);

  @Inject FulfilmentService fulfilmentService;

  @GET
  public List<Fulfilment> getAll() {
    return fulfilmentService.getAll();
  }

  @GET
  @Path("store/{storeId}")
  public List<Fulfilment> getByStore(Long storeId) {
    return fulfilmentService.getByStoreId(storeId);
  }

  @POST
  @Transactional
  public Response create(Fulfilment fulfilment) {
    if (fulfilment.id != null) {
      throw new WebApplicationException("Id was invalidly set on request", 422);
    }

    fulfilmentService.create(fulfilment);

    LOGGER.infof(
        "Fulfilment created: store=%d, product=%d, warehouse=%s",
        fulfilment.storeId, fulfilment.productId, fulfilment.warehouseBusinessUnitCode);
    return Response.ok(fulfilment).status(201).build();
  }

  @DELETE
  @Path("{id}")
  @Transactional
  public Response delete(Long id) {
    fulfilmentService.delete(id);
    LOGGER.infof("Fulfilment deleted: id=%d", id);
    return Response.status(204).build();
  }
}
