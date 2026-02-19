package com.fulfilment.application.monolith.fulfilment;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

@QuarkusTest
class FulfilmentResourceTest {

  private static final String PATH = "/fulfilment";

  // Seed data: stores 1-3, products 1-3, warehouses MWH.001/MWH.012/MWH.023

  @Test
  void create_shouldCreateFulfilment_whenDataIsValid() {
    int id =
        given()
            .contentType(ContentType.JSON)
            .body("{\"storeId\": 1, \"productId\": 1, \"warehouseBusinessUnitCode\": \"MWH.001\"}")
            .post(PATH)
            .then()
            .statusCode(201)
            .body("storeId", is(1))
            .body("productId", is(1))
            .body("warehouseBusinessUnitCode", is("MWH.001"))
            .extract()
            .path("id");

    deleteFulfilment(id);
  }

  @Test
  void create_shouldReturn400_whenStoreDoesNotExist() {
    given()
        .contentType(ContentType.JSON)
        .body("{\"storeId\": 9999, \"productId\": 1, \"warehouseBusinessUnitCode\": \"MWH.001\"}")
        .post(PATH)
        .then()
        .statusCode(400);
  }

  @Test
  void create_shouldReturn400_whenProductDoesNotExist() {
    given()
        .contentType(ContentType.JSON)
        .body("{\"storeId\": 1, \"productId\": 9999, \"warehouseBusinessUnitCode\": \"MWH.001\"}")
        .post(PATH)
        .then()
        .statusCode(400);
  }

  @Test
  void create_shouldReturn400_whenWarehouseDoesNotExist() {
    given()
        .contentType(ContentType.JSON)
        .body("{\"storeId\": 1, \"productId\": 1, \"warehouseBusinessUnitCode\": \"NON-EXISTENT\"}")
        .post(PATH)
        .then()
        .statusCode(400);
  }

  @Test
  void create_shouldReturn400_whenDuplicateAssociation() {
    int id = createFulfilment(2, 1, "MWH.001");

    given()
        .contentType(ContentType.JSON)
        .body("{\"storeId\": 2, \"productId\": 1, \"warehouseBusinessUnitCode\": \"MWH.001\"}")
        .post(PATH)
        .then()
        .statusCode(400);

    deleteFulfilment(id);
  }

  @Test
  void create_shouldReturn400_whenProductExceedsMaxWarehousesPerStore() {
    // Max 2 warehouses per product per store
    int id1 = createFulfilment(3, 2, "MWH.001");
    int id2 = createFulfilment(3, 2, "MWH.012");

    given()
        .contentType(ContentType.JSON)
        .body("{\"storeId\": 3, \"productId\": 2, \"warehouseBusinessUnitCode\": \"MWH.023\"}")
        .post(PATH)
        .then()
        .statusCode(400);

    deleteFulfilment(id1);
    deleteFulfilment(id2);
  }

  @Test
  void create_shouldReturn400_whenStoreExceedsMaxWarehouses() {
    // Max 3 distinct warehouses per store — need a 4th warehouse
    int id1 = createFulfilment(1, 1, "MWH.001");
    int id2 = createFulfilment(1, 2, "MWH.012");
    int id3 = createFulfilment(1, 3, "MWH.023");

    // Create a 4th warehouse for testing
    String buCode = "FUL." + System.currentTimeMillis();
    given()
        .contentType(ContentType.JSON)
        .body(
            "{\"businessUnitCode\": \""
                + buCode
                + "\", \"location\": \"AMSTERDAM-002\", \"capacity\": 20, \"stock\": 5}")
        .post("/warehouse")
        .then()
        .statusCode(200);

    given()
        .contentType(ContentType.JSON)
        .body(
            "{\"storeId\": 1, \"productId\": 1, \"warehouseBusinessUnitCode\": \"" + buCode + "\"}")
        .post(PATH)
        .then()
        .statusCode(400);

    deleteFulfilment(id1);
    deleteFulfilment(id2);
    deleteFulfilment(id3);
    archiveWarehouse(buCode);
  }

  @Test
  void create_shouldReturn400_whenWarehouseExceedsMaxProducts() {
    // Max 5 product types per warehouse — need 6 products
    // We only have 3 seed products, so we create more
    Long p4 = createProduct("FULTEST-P4");
    Long p5 = createProduct("FULTEST-P5");
    Long p6 = createProduct("FULTEST-P6");

    int id1 = createFulfilment(1, 1, "MWH.012");
    int id2 = createFulfilment(1, 2, "MWH.012");
    int id3 = createFulfilment(1, 3, "MWH.012");
    int id4 = createFulfilment(1, p4, "MWH.012");
    int id5 = createFulfilment(1, p5, "MWH.012");

    // 6th product type should fail
    given()
        .contentType(ContentType.JSON)
        .body("{\"storeId\": 1, \"productId\": " + p6 + ", \"warehouseBusinessUnitCode\": \"MWH.012\"}")
        .post(PATH)
        .then()
        .statusCode(400);

    deleteFulfilment(id1);
    deleteFulfilment(id2);
    deleteFulfilment(id3);
    deleteFulfilment(id4);
    deleteFulfilment(id5);
    deleteProduct(p4);
    deleteProduct(p5);
    deleteProduct(p6);
  }

  @Test
  void getByStore_shouldReturnFulfilments_whenStoreHasFulfilments() {
    int id = createFulfilment(2, 2, "MWH.023");

    given()
        .when()
        .get(PATH + "/store/2")
        .then()
        .statusCode(200)
        .body("size()", greaterThanOrEqualTo(1));

    deleteFulfilment(id);
  }

  @Test
  void getByStore_shouldReturn404_whenStoreDoesNotExist() {
    given().when().get(PATH + "/store/9999").then().statusCode(404);
  }

  @Test
  void delete_shouldReturn404_whenFulfilmentDoesNotExist() {
    given().when().delete(PATH + "/9999").then().statusCode(404);
  }

  private void deleteFulfilment(int id) {
    given().when().delete(PATH + "/" + id).then().statusCode(204);
  }

  private int createFulfilment(long storeId, long productId, String warehouseCode) {
    return given()
        .contentType(ContentType.JSON)
        .body(
            "{\"storeId\": "
                + storeId
                + ", \"productId\": "
                + productId
                + ", \"warehouseBusinessUnitCode\": \""
                + warehouseCode
                + "\"}")
        .post(PATH)
        .then()
        .statusCode(201)
        .extract()
        .path("id");
  }

  private Long createProduct(String name) {
    return given()
        .contentType(ContentType.JSON)
        .body("{\"name\": \"" + name + "\", \"stock\": 10}")
        .post("/product")
        .then()
        .statusCode(201)
        .extract()
        .jsonPath()
        .getLong("id");
  }

  private void archiveWarehouse(String businessUnitCode) {
    List<Map<String, Object>> warehouses =
        given().when().get("/warehouse").then().statusCode(200).extract().jsonPath().getList("$");
    String warehouseId =
        warehouses.stream()
            .filter(w -> businessUnitCode.equals(w.get("businessUnitCode")))
            .map(w -> String.valueOf(w.get("id")))
            .findFirst()
            .orElseThrow();
    given().when().delete("/warehouse/" + warehouseId).then().statusCode(204);
  }

  private void deleteProduct(Long id) {
    given().when().delete("/product/" + id).then().statusCode(204);
  }
}
