package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
class WarehouseEndpointTest {

  private static final String PATH = "/warehouse";

  // --- GET all ---

  @Test
  void getAll_shouldReturnWarehouses_whenWarehousesExist() {
    given()
        .when()
        .get(PATH)
        .then()
        .statusCode(200)
        .body("size()", greaterThanOrEqualTo(3))
        .body("businessUnitCode", hasItems("MWH.001", "MWH.012", "MWH.023"));
  }

  // --- GET by id ---

  @Test
  void getById_shouldReturnWarehouse_whenWarehouseExists() {
    given()
        .when()
        .get(PATH + "/1")
        .then()
        .statusCode(200)
        .body("id", is("1"))
        .body("businessUnitCode", is("MWH.001"))
        .body("location", is("ZWOLLE-001"));
  }

  @Test
  void getById_shouldReturn404_whenWarehouseDoesNotExist() {
    given().when().get(PATH + "/99999").then().statusCode(404);
  }

  // --- POST create ---

  @Test
  void create_shouldCreateWarehouse_whenDataIsValid() {
    String buCode = "CRT." + System.currentTimeMillis();
    given()
        .contentType(ContentType.JSON)
        .body(
            "{\"businessUnitCode\": \""
                + buCode
                + "\", \"location\": \"ZWOLLE-002\", \"capacity\": 20, \"stock\": 5}")
        .post(PATH)
        .then()
        .statusCode(200)
        .body("businessUnitCode", is(buCode))
        .body("location", is("ZWOLLE-002"))
        .body("capacity", is(20))
        .body("stock", is(5));
  }

  @Test
  void create_shouldReturn400_whenBusinessUnitCodeAlreadyExists() {
    given()
        .contentType(ContentType.JSON)
        .body(
            "{\"businessUnitCode\": \"MWH.012\", \"location\": \"AMSTERDAM-002\", \"capacity\": 10, \"stock\": 1}")
        .post(PATH)
        .then()
        .statusCode(400);
  }

  @Test
  void create_shouldReturn400_whenLocationDoesNotExist() {
    given()
        .contentType(ContentType.JSON)
        .body(
            "{\"businessUnitCode\": \"TEST.BADLOC\", \"location\": \"INVALID-LOCATION\", \"capacity\": 20, \"stock\": 5}")
        .post(PATH)
        .then()
        .statusCode(400);
  }

  @Test
  void create_shouldReturn400_whenCapacityExceedsLocationMax() {
    given()
        .contentType(ContentType.JSON)
        .body(
            "{\"businessUnitCode\": \"TEST.BIGCAP\", \"location\": \"HELMOND-001\", \"capacity\": 999, \"stock\": 5}")
        .post(PATH)
        .then()
        .statusCode(400);
  }

  @Test
  void create_shouldReturn400_whenStockExceedsCapacity() {
    given()
        .contentType(ContentType.JSON)
        .body(
            "{\"businessUnitCode\": \"TEST.BIGSTK\", \"location\": \"AMSTERDAM-002\", \"capacity\": 5, \"stock\": 50}")
        .post(PATH)
        .then()
        .statusCode(400);
  }

  @Test
  void create_shouldReturn400_whenMaxWarehousesReachedAtLocation() {
    given()
        .contentType(ContentType.JSON)
        .body(
            "{\"businessUnitCode\": \"TEST.MAXWH\", \"location\": \"ZWOLLE-001\", \"capacity\": 10, \"stock\": 5}")
        .post(PATH)
        .then()
        .statusCode(400);
  }

  // --- DELETE archive ---

  @Test
  void archive_shouldReturn204AndRemoveFromList_whenWarehouseExists() {
    String buCode = "ARCH." + System.currentTimeMillis();
    String warehouseId =
        given()
            .contentType(ContentType.JSON)
            .body(
                "{\"businessUnitCode\": \""
                    + buCode
                    + "\", \"location\": \"VETSBY-001\", \"capacity\": 20, \"stock\": 5}")
            .post(PATH)
            .then()
            .statusCode(200)
            .extract()
            .path("id");

    given().when().delete(PATH + "/" + warehouseId).then().statusCode(204);

    given().when().get(PATH + "/" + warehouseId).then().statusCode(404);
  }

  @Test
  void archive_shouldReturn404_whenWarehouseDoesNotExist() {
    given().when().delete(PATH + "/99999").then().statusCode(404);
  }

  // --- POST replacement ---

  @Test
  void replace_shouldReplaceWarehouse_whenDataIsValid() {
    String buCode = "REP." + System.currentTimeMillis();
    given()
        .contentType(ContentType.JSON)
        .body(
            "{\"businessUnitCode\": \""
                + buCode
                + "\", \"location\": \"AMSTERDAM-002\", \"capacity\": 20, \"stock\": 5}")
        .post(PATH)
        .then()
        .statusCode(200);

    given()
        .contentType(ContentType.JSON)
        .body(
            "{\"businessUnitCode\": \""
                + buCode
                + "\", \"location\": \"EINDHOVEN-001\", \"capacity\": 30, \"stock\": 5}")
        .post(PATH + "/" + buCode + "/replacement")
        .then()
        .statusCode(200)
        .body("businessUnitCode", is(buCode))
        .body("location", is("EINDHOVEN-001"))
        .body("capacity", is(30))
        .body("stock", is(5));
  }

  @Test
  void replace_shouldReturn404_whenWarehouseDoesNotExist() {
    given()
        .contentType(ContentType.JSON)
        .body(
            "{\"businessUnitCode\": \"NON-EXISTENT\", \"location\": \"AMSTERDAM-002\", \"capacity\": 20, \"stock\": 5}")
        .post(PATH + "/NON-EXISTENT/replacement")
        .then()
        .statusCode(404);
  }

  @Test
  void replace_shouldReturn400_whenStockDoesNotMatch() {
    String buCode = "REPS." + System.currentTimeMillis();
    given()
        .contentType(ContentType.JSON)
        .body(
            "{\"businessUnitCode\": \""
                + buCode
                + "\", \"location\": \"AMSTERDAM-002\", \"capacity\": 20, \"stock\": 5}")
        .post(PATH)
        .then()
        .statusCode(200);

    given()
        .contentType(ContentType.JSON)
        .body(
            "{\"businessUnitCode\": \""
                + buCode
                + "\", \"location\": \"AMSTERDAM-002\", \"capacity\": 30, \"stock\": 99}")
        .post(PATH + "/" + buCode + "/replacement")
        .then()
        .statusCode(400);
  }

  @Test
  void replace_shouldReturn400_whenNewCapacityCannotAccommodateExistingStock() {
    String buCode = "REPC." + System.currentTimeMillis();
    given()
        .contentType(ContentType.JSON)
        .body(
            "{\"businessUnitCode\": \""
                + buCode
                + "\", \"location\": \"AMSTERDAM-002\", \"capacity\": 30, \"stock\": 25}")
        .post(PATH)
        .then()
        .statusCode(200);

    given()
        .contentType(ContentType.JSON)
        .body(
            "{\"businessUnitCode\": \""
                + buCode
                + "\", \"location\": \"AMSTERDAM-002\", \"capacity\": 10, \"stock\": 25}")
        .post(PATH + "/" + buCode + "/replacement")
        .then()
        .statusCode(400);
  }
}
