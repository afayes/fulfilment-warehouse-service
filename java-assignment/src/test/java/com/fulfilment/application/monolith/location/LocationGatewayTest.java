package com.fulfilment.application.monolith.location;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LocationGatewayTest {

  private LocationGateway locationGateway;

  @BeforeEach
  void setUp() {
    locationGateway = new LocationGateway();
  }

  @Test
  void testResolveByIdentifierWhenExistingLocationThenReturnLocation() {
    Location location = locationGateway.resolveByIdentifier("ZWOLLE-001");

    assertNotNull(location);
    assertEquals("ZWOLLE-001", location.identification);
    assertEquals(1, location.maxNumberOfWarehouses);
    assertEquals(40, location.maxCapacity);
  }

  @Test
  void testResolveByIdentifierWhenAnotherExistingLocationThenReturnLocation() {
    Location location = locationGateway.resolveByIdentifier("AMSTERDAM-001");

    assertNotNull(location);
    assertEquals("AMSTERDAM-001", location.identification);
    assertEquals(5, location.maxNumberOfWarehouses);
    assertEquals(100, location.maxCapacity);
  }

  @Test
  void testResolveByIdentifierWhenNonExistingLocationThenThrowLocationNotFoundException() {
    LocationNotFoundException exception =
        assertThrows(
            LocationNotFoundException.class,
            () -> locationGateway.resolveByIdentifier("NON-EXISTENT"));

    assertEquals("Location with identifier 'NON-EXISTENT' not found", exception.getMessage());
  }

  @Test
  void testResolveByIdentifierWhenNullIdentifierThenThrowIllegalArgumentException() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> locationGateway.resolveByIdentifier(null));

    assertEquals("Identifier cannot be null or blank", exception.getMessage());
  }

  @Test
  void testResolveByIdentifierWhenEmptyIdentifierThenThrowIllegalArgumentException() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> locationGateway.resolveByIdentifier(""));

    assertEquals("Identifier cannot be null or blank", exception.getMessage());
  }

  @Test
  void testResolveByIdentifierWhenBlankIdentifierThenThrowIllegalArgumentException() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> locationGateway.resolveByIdentifier("   "));

    assertEquals("Identifier cannot be null or blank", exception.getMessage());
  }
}
