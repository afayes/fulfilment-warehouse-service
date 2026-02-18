package com.fulfilment.application.monolith.location;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;

public class LocationGatewayTest {

  private final LocationGateway locationGateway = new LocationGateway();

  @Test
  void resolveByIdentifier_shouldReturnLocation_whenLocationExists() {
    // when
    Location location = locationGateway.resolveByIdentifier("ZWOLLE-001");

    // then
    assertNotNull(location, "The resolved location should not be null");
    assertEquals("ZWOLLE-001", location.identification, "The location identifier does not match");
    assertEquals(1, location.maxNumberOfWarehouses, "The maximum number of warehouses does not match");
    assertEquals(40, location.maxCapacity, "The maximum capacity does not match");
  }

  @Test
  void resolveByIdentifier_shouldThrowLocationNotFoundException_whenLocationDoesNotExist() {
    LocationNotFoundException exception =
        assertThrows(
            LocationNotFoundException.class,
            () -> locationGateway.resolveByIdentifier("NON-EXISTENT"));

    assertEquals("Location with identifier 'NON-EXISTENT' not found", exception.getMessage(), "The exception message does not match");
  }

  @Test
  void resolveByIdentifier_shouldThrowIllegalArgumentException_whenIdentifierIsNull() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> locationGateway.resolveByIdentifier(null));

    assertEquals("Identifier cannot be null or blank", exception.getMessage(), "The exception message does not match");
  }

  @Test
  void resolveByIdentifier_shouldThrowIllegalArgumentException_whenIdentifierIsEmpty() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> locationGateway.resolveByIdentifier(""));

    assertEquals("Identifier cannot be null or blank", exception.getMessage(), "The exception message does not match");
  }

  @Test
  void resolveByIdentifier_shouldThrowIllegalArgumentException_whenIdentifierIsBlank() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> locationGateway.resolveByIdentifier("   "));

    assertEquals("Identifier cannot be null or blank", exception.getMessage(), "The exception message does not match");
  }
}
