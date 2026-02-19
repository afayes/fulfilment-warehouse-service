package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.location.LocationNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.warehouse.api.WarehouseResource;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;
import org.jboss.logging.Logger;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

  private static final Logger LOGGER = Logger.getLogger(WarehouseResourceImpl.class);

  @Inject private WarehouseStore warehouseStore;

  @Inject private CreateWarehouseOperation createWarehouseOperation;

  @Inject private ArchiveWarehouseOperation archiveWarehouseOperation;

  @Inject private ReplaceWarehouseOperation replaceWarehouseOperation;

  @Override
  public List<com.warehouse.api.beans.Warehouse> listAllWarehousesUnits() {
    return warehouseStore.getAll().stream().map(this::toWarehouseResponse).toList();
  }

  @Override
  @Transactional
  public com.warehouse.api.beans.Warehouse createANewWarehouseUnit(
      @NotNull com.warehouse.api.beans.Warehouse data) {
    var warehouse = toDomainWarehouse(data);
    try {
      createWarehouseOperation.create(warehouse);
    } catch (WarehouseValidationException | LocationNotFoundException e) {
      LOGGER.warnf("Warehouse creation failed: %s", e.getMessage());
      throw new WebApplicationException(e.getMessage(), 400);
    }
    LOGGER.infof("Warehouse created: %s", warehouse.businessUnitCode);
    return toWarehouseResponse(warehouse);
  }

  @Override
  public com.warehouse.api.beans.Warehouse getAWarehouseUnitByID(String id) {
    Warehouse warehouse = findWarehouseByIdentifier(id);
    return toWarehouseResponse(warehouse);
  }

  @Override
  @Transactional
  public void archiveAWarehouseUnitByID(String id) {
    Warehouse warehouse = findWarehouseByIdentifier(id);
    try {
      archiveWarehouseOperation.archive(warehouse);
    } catch (WarehouseNotFoundException e) {
      LOGGER.warnf("Warehouse archive failed: %s", e.getMessage());
      throw new WebApplicationException(e.getMessage(), 404);
    }
    LOGGER.infof("Warehouse archived: %s", id);
  }

  @Override
  @Transactional
  public com.warehouse.api.beans.Warehouse replaceTheCurrentActiveWarehouse(
      String businessUnitCode, @NotNull com.warehouse.api.beans.Warehouse data) {
    var newWarehouse = toDomainWarehouse(data);
    newWarehouse.businessUnitCode = businessUnitCode;
    try {
      replaceWarehouseOperation.replace(newWarehouse);
    } catch (WarehouseNotFoundException e) {
      LOGGER.warnf("Warehouse replacement failed: %s", e.getMessage());
      throw new WebApplicationException(e.getMessage(), 404);
    } catch (WarehouseValidationException | LocationNotFoundException e) {
      LOGGER.warnf("Warehouse replacement failed: %s", e.getMessage());
      throw new WebApplicationException(e.getMessage(), 400);
    }
    LOGGER.infof("Warehouse replaced: %s", businessUnitCode);
    return toWarehouseResponse(newWarehouse);
  }

  private Warehouse findWarehouseByIdentifier(String id) {
    Warehouse warehouse = warehouseStore.getById(id);
    if (warehouse == null) {
      LOGGER.warnf("Warehouse not found: %s", id);
      throw new WebApplicationException("Warehouse with identifier '" + id + "' not found", 404);
    }
    return warehouse;
  }

  private Warehouse toDomainWarehouse(com.warehouse.api.beans.Warehouse data) {
    var warehouse = new Warehouse();
    warehouse.businessUnitCode = data.getBusinessUnitCode();
    warehouse.location = data.getLocation();
    warehouse.capacity = data.getCapacity();
    warehouse.stock = data.getStock();
    return warehouse;
  }

  private com.warehouse.api.beans.Warehouse toWarehouseResponse(Warehouse warehouse) {
    var response = new com.warehouse.api.beans.Warehouse();
    response.setBusinessUnitCode(warehouse.businessUnitCode);
    response.setLocation(warehouse.location);
    response.setCapacity(warehouse.capacity);
    response.setStock(warehouse.stock);
    return response;
  }
}
