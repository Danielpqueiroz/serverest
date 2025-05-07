package utils;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static io.restassured.RestAssured.*;

import java.util.UUID;

public class AuthUtils {

    public static String criarUsuarioEObterToken() {
        String email = "admin_" + UUID.randomUUID() + "@teste.com";

        // 1. Cria usuário admin
        given()
                .baseUri("https://serverest.dev")
                .contentType(ContentType.JSON)
                .body("{ \"nome\": \"Usuário Admin\", " +
                        "\"email\": \"" + email + "\", " +
                        "\"password\": \"teste123\", " +
                        "\"administrador\": \"true\" }")
                .when()
                .post("/usuarios")
                .then()
                .statusCode(201);

        // 2. Faz login
        Response loginResponse = given()
                .baseUri("https://serverest.dev")
                .contentType(ContentType.JSON)
                .body("{ \"email\": \"" + email + "\", \"password\": \"teste123\" }")
                .when()
                .post("/login");

        loginResponse.then().statusCode(200);

        return loginResponse.jsonPath().getString("authorization");
    }
}
