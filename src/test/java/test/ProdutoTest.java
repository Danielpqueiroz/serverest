package test;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.TestMethodOrder;
import utils.AuthUtils;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)  // Controla a ordem dos testes
public class ProdutoTest {
    @Test
    @Order(1)  // Define que este teste será executado primeiro
    public void deveCadastrarProdutoComSucesso() {
        String token = AuthUtils.criarUsuarioEObterToken();
        String nomeProduto = "Notebook Lenovo " + UUID.randomUUID();

        given()
                .baseUri("https://serverest.dev")
                .header("Authorization", token)
                .contentType(ContentType.JSON)
                .body("{ \"nome\": \"" + nomeProduto + "\", \"preco\": 3500, \"descricao\": \"Notebook novo\", \"quantidade\": 10 }")
                .when()
                .post("/produtos")
                .then()
                .log().all()
                .statusCode(201)
                .body("message", is("Cadastro realizado com sucesso"))
                .body("_id", notNullValue());
    }

    @Test
    @Order(2)  // Define que este teste será executado em segundo lugar
    public void naoDeveCadastrarProdutoSemToken() {
        given()
                .baseUri("https://serverest.dev")
                .contentType(ContentType.JSON)
                .body("{ \"nome\": \"Notebook HP\", \"preco\": 4000, \"descricao\": \"HP top\", \"quantidade\": 5 }")
                .when()
                .post("/produtos")
                .then()
                .log().all()
                .statusCode(401)
                .body("message", is("Token de acesso ausente, inválido, expirado ou usuário do token não existe mais"));
    }
}
