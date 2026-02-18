package com.fulfilment.application.monolith.stores;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class StoreEndpointTest {

  private static final String PATH = "store";

  @Test
  void getAll_shouldReturnStores_whenStoresExist() {
    createStore("LIST-TEST-STORE", 7);
    createStore("LIST-TEST-STORE-2", 7);
    createStore("LIST-TEST-STORE-3", 7);

    given()
        .when()
        .get(PATH)
        .then()
        .statusCode(200)
        .body(
          containsString("LIST-TEST-STORE"), 
          containsString("LIST-TEST-STORE-2"), 
          containsString("LIST-TEST-STORE-3") );
  }

  @Test
  void getById_shouldReturnStore_whenStoreExists() {
    int id = createStore("GET-BY-ID-STORE", 15);

    given()
        .when()
        .get(PATH + "/" + id)
        .then()
        .statusCode(200)
        .body("name", equalTo("GET-BY-ID-STORE"))
        .body("quantityProductsInStock", equalTo(15));
  }

  @Test
  void getById_shouldReturn404_whenStoreDoesNotExist() {
    given()
        .when()
        .get(PATH + "/9999")
        .then()
        .statusCode(404)
        .body("error", containsString("does not exist"));
  }

  @Test
  void create_shouldCreateAndReturnStore_whenValidStoreIsProvided() {
    given()
        .contentType(ContentType.JSON)
        .body("{\"name\": \"BRAND-NEW-STORE\", \"quantityProductsInStock\": 42}")
        .when()
        .post(PATH)
        .then()
        .statusCode(201)
        .body("id", notNullValue())
        .body("name", equalTo("BRAND-NEW-STORE"))
        .body("quantityProductsInStock", equalTo(42));
  }

  @Test
  void create_shouldReturn422_whenIdIsSet() {
    given()
        .contentType(ContentType.JSON)
        .body("{\"id\": 999, \"name\": \"INVALID-STORE\", \"quantityProductsInStock\": 1}")
        .when()
        .post(PATH)
        .then()
        .statusCode(422)
        .body("error", containsString("Id was invalidly set on request"));
  }

  @Test
  void update_shouldUpdateNameAndStock_whenValidStoreIsProvided() {
    int id = createStore("BEFORE-UPDATE", 5);

    given()
        .contentType(ContentType.JSON)
        .body("{\"name\": \"AFTER-UPDATE\", \"quantityProductsInStock\": 20}")
        .when()
        .put(PATH + "/" + id)
        .then()
        .statusCode(200)
        .body("name", equalTo("AFTER-UPDATE"))
        .body("quantityProductsInStock", equalTo(20));

    given()
        .when()
        .get(PATH + "/" + id)
        .then()
        .statusCode(200)
        .body("name", equalTo("AFTER-UPDATE"))
        .body("quantityProductsInStock", equalTo(20));
  }

  @Test
  void update_shouldReturn404_whenStoreDoesNotExist() {
    given()
        .contentType(ContentType.JSON)
        .body("{\"name\": \"STORE\", \"quantityProductsInStock\": 0}")
        .when()
        .put(PATH + "/9999")
        .then()
        .statusCode(404)
        .body("error", containsString("does not exist"));
  }

  @Test
  void update_shouldReturn422_whenNameIsNull() {
    int id = createStore("UPDATE-VALIDATION", 3);

    given()
        .contentType(ContentType.JSON)
        .body("{\"quantityProductsInStock\": 5}")
        .when()
        .put(PATH + "/" + id)
        .then()
        .statusCode(422)
        .body("error", containsString("Store Name was not set on request"));
  }

  @Test
  void patch_shouldUpdateOnlyName_whenStockIsNotProvided() {
    int id = createStore("PATCH-NAME-ONLY", 25);

    given()
        .contentType(ContentType.JSON)
        .body("{\"name\": \"PATCHED-NAME\"}")
        .when()
        .patch(PATH + "/" + id)
        .then()
        .statusCode(200)
        .body("name", equalTo("PATCHED-NAME"))
        .body("quantityProductsInStock", equalTo(25));
  }

  @Test
  void patch_shouldUpdateNameAndStock_whenBothNameAndStockAreProvided() {
    int id = createStore("BEFORE-PATCH", 10);

    given()
        .contentType(ContentType.JSON)
        .body("{\"name\": \"AFTER-PATCH\", \"quantityProductsInStock\": 99}")
        .when()
        .patch(PATH + "/" + id)
        .then()
        .statusCode(200)
        .body("name", equalTo("AFTER-PATCH"))
        .body("quantityProductsInStock", equalTo(99));

     // verify the patch changes have been persisted
    given()
        .when()
        .get(PATH + "/" + id)
        .then()
        .statusCode(200)
        .body("name", equalTo("AFTER-PATCH"))
        .body("quantityProductsInStock", equalTo(99));
  }

  @Test
  void patch_shouldReturn404_whenStoreDoesNotExist() {
    given()
        .contentType(ContentType.JSON)
        .body("{\"name\": \"Store\", \"quantityProductsInStock\": 0}")
        .when()
        .patch(PATH + "/9999")
        .then()
        .statusCode(404)
        .body("error", containsString("does not exist"));
  }

  @Test
  void patch_shouldReturn422_whenNameIsNull() {
    int id = createStore("PATCH-VALIDATION", 8);

    given()
        .contentType(ContentType.JSON)
        .body("{\"quantityProductsInStock\": 5}")
        .when()
        .patch(PATH + "/" + id)
        .then()
        .statusCode(422)
        .body("error", containsString("Store Name was not set on request"));
  }

  @Test
  void delete_shouldRemoveStore_whenStoreExists() {
    int id = createStore("TO-BE-DELETED", 1);

    given()
        .when()
        .delete(PATH + "/" + id)
        .then()
        .statusCode(204);

    given()
        .when()
        .get(PATH + "/" + id)
        .then()
        .statusCode(404);
  }

  @Test
  void delete_shouldReturn404_whenStoreDoesNotExist() {
    given()
        .when()
        .delete(PATH + "/9999")
        .then()
        .statusCode(404)
        .body("error", containsString("does not exist"));
  }


  private int createStore(String name, int quantityProductsInStock) {
    return given()
        .contentType(ContentType.JSON)
        .body(
            "{\"name\": \""
                + name
                + "\", \"quantityProductsInStock\": "
                + quantityProductsInStock
                + "}")
        .post(PATH)
        .then()
        .statusCode(201)
        .extract()
        .path("id");
  }
}
