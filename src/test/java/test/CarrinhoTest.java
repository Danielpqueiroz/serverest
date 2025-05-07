package test;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import utils.AuthUtils;

import java.util.UUID;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)  // Controla a ordem dos testes
public class CarrinhoTest {
    @Test
    @Order(1)  // Define que este teste será executado primeiro
    public void deveCriarCarrinhoComSucesso() {
        String token = AuthUtils.criarUsuarioEObterToken();

        // 1. Cria produto
        String nomeProduto = "Produto Teste " + UUID.randomUUID();
        Response produtoResponse = given()
                .baseUri("https://serverest.dev")
                .header("Authorization", token)
                .contentType(ContentType.JSON)
                .body("{ \"nome\": \"" + nomeProduto + "\", \"preco\": 100, \"descricao\": \"Desc\", \"quantidade\": 10 }")
                .when()
                .post("/produtos");

        produtoResponse.then().statusCode(201);
        String idProduto = produtoResponse.jsonPath().getString("_id");

        // 2. Cria carrinho com esse produto
        given()
                .baseUri("https://serverest.dev")
                .header("Authorization", token)
                .contentType(ContentType.JSON)
                .body("{ \"produtos\": [ { \"idProduto\": \"" + idProduto + "\", \"quantidade\": 1 } ] }")
                .when()
                .post("/carrinhos")
                .then()
                .log().all()
                .statusCode(201)
                .body("message", is("Cadastro realizado com sucesso"));
    }

    @Test
    @Order(2)  // Define que este teste será executado em segundo lugar
    public void naoDeveCriarCarrinhoComProdutoInvalido() {
        String token = AuthUtils.criarUsuarioEObterToken();

        // ID de produto inexistente
        String idProdutoFalso = "000000000000000000000000";

        given()
                .baseUri("https://serverest.dev")
                .header("Authorization", token)
                .contentType(ContentType.JSON)
                .body("{ \"produtos\": [ { \"idProduto\": \"" + idProdutoFalso + "\", \"quantidade\": 1 } ] }")
                .when()
                .post("/carrinhos")
                .then()
                .log().all()
                .statusCode(400)
                .body("message", containsString("Produto não encontrado"));
    }
}
