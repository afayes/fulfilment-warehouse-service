package com.fulfilment.application.monolith.stores;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
public class StoreEventObserverTest {

  private static final String PATH = "store";

  @InjectMock
  LegacyStoreManagerGateway legacyStoreManagerGateway;

  @Test
  void create_shouldCallLegacyGateway_afterStoreIsCreated() {
    given()
        .contentType(ContentType.JSON)
        .body("{\"name\": \"LEGACY-CREATE-TEST\", \"quantityProductsInStock\": 5}")
        .post(PATH)
        .then()
        .statusCode(201);

    Mockito.verify(legacyStoreManagerGateway)
        .createStoreOnLegacySystem(Mockito.any(Store.class));
  }

  @Test
  void update_shouldCallLegacyGateway_afterStoreIsUpdated() {
    int id = createStore("LEGACY-UPDATE-BEFORE", 10);

    given()
        .contentType(ContentType.JSON)
        .body("{\"name\": \"LEGACY-UPDATE-AFTER\", \"quantityProductsInStock\": 20}")
        .put(PATH + "/" + id)
        .then()
        .statusCode(200);

    Mockito.verify(legacyStoreManagerGateway)
        .updateStoreOnLegacySystem(Mockito.any(Store.class));
  }

  @Test
  void patch_shouldCallLegacyGateway_afterStoreIsPatched() {
    int id = createStore("LEGACY-PATCH-BEFORE", 10);

    given()
        .contentType(ContentType.JSON)
        .body("{\"name\": \"LEGACY-PATCH-AFTER\", \"quantityProductsInStock\": 30}")
        .patch(PATH + "/" + id)
        .then()
        .statusCode(200);

    Mockito.verify(legacyStoreManagerGateway)
        .updateStoreOnLegacySystem(Mockito.any(Store.class));
  }

  @Test
  void create_shouldNotCallLegacyGateway_whenRequestIsInvalid() {
    given()
        .contentType(ContentType.JSON)
        .body("{\"id\": 999, \"name\": \"FAIL\", \"quantityProductsInStock\": 1}")
        .post(PATH)
        .then()
        .statusCode(422);

    Mockito.verifyNoInteractions(legacyStoreManagerGateway);
  }

  @Test
  void update_shouldNotCallLegacyGateway_whenStoreDoesNotExist() {
    given()
        .contentType(ContentType.JSON)
        .body("{\"name\": \"STORE\", \"quantityProductsInStock\": 0}")
        .put(PATH + "/9999")
        .then()
        .statusCode(404);

    Mockito.verifyNoInteractions(legacyStoreManagerGateway);
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
