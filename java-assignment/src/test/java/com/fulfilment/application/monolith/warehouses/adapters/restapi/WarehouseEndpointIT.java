package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusIntegrationTest
public class WarehouseEndpointIT {

  @Test
  public void testSimpleListWarehouses() {

    final String path = "warehouse";

    given()
        .when()
        .get(path)
        .then()
        .statusCode(200)
        .body(containsString("MWH.001"), containsString("MWH.012"), containsString("MWH.023"));
  }

  @Test
  public void testSimpleCheckingArchivingWarehouses() {

    final String path = "warehouse";

    given()
        .when()
        .get(path)
        .then()
        .statusCode(200)
        .body(
            containsString("MWH.001"),
            containsString("MWH.012"),
            containsString("MWH.023"),
            containsString("ZWOLLE-001"),
            containsString("AMSTERDAM-001"),
            containsString("TILBURG-001"));

    given().when().delete(path + "/1").then().statusCode(204);

    given()
        .when()
        .get(path)
        .then()
        .statusCode(200)
        .body(
            not(containsString("ZWOLLE-001")),
            containsString("AMSTERDAM-001"),
            containsString("TILBURG-001"));
  }

  @Test
  public void testSimpleCreatingAndRetrievingWarehouse() {

    final String path = "warehouse";

    String warehouseId =
        given()
            .contentType(ContentType.JSON)
            .body(
                "{\"businessUnitCode\": \"IT.CREATE\", \"location\": \"AMSTERDAM-002\", \"capacity\": 20, \"stock\": 5}")
            .post(path)
            .then()
            .statusCode(200)
            .body("businessUnitCode", is("IT.CREATE"))
            .body("location", is("AMSTERDAM-002"))
            .extract()
            .path("id");

    given()
        .when()
        .get(path + "/" + warehouseId)
        .then()
        .statusCode(200)
        .body("businessUnitCode", is("IT.CREATE"))
        .body("capacity", is(20))
        .body("stock", is(5));
  }

  @Test
  public void testSimpleReplacingWarehouse() {

    final String path = "warehouse";

    given()
        .contentType(ContentType.JSON)
        .body(
            "{\"businessUnitCode\": \"IT.REPLACE\", \"location\": \"AMSTERDAM-002\", \"capacity\": 20, \"stock\": 5}")
        .post(path)
        .then()
        .statusCode(200);

    String newWarehouseId =
        given()
            .contentType(ContentType.JSON)
            .body(
                "{\"businessUnitCode\": \"IT.REPLACE\", \"location\": \"EINDHOVEN-001\", \"capacity\": 30, \"stock\": 5}")
            .post(path + "/IT.REPLACE/replacement")
            .then()
            .statusCode(200)
            .body("businessUnitCode", is("IT.REPLACE"))
            .body("location", is("EINDHOVEN-001"))
            .body("capacity", is(30))
            .extract()
            .path("id");

    given()
        .when()
        .get(path + "/" + newWarehouseId)
        .then()
        .statusCode(200)
        .body("location", is("EINDHOVEN-001"))
        .body("capacity", is(30))
        .body("stock", is(5));
  }
}
