package test;

import com.github.javafaker.Faker;
import dto.ProdutoDTO;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.*;
import utils.AuthUtils;
import utils.ProdUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

@TestMethodOrder(org.junit.jupiter.api.MethodOrderer.OrderAnnotation.class)
public class CarrinhoTest {

    private static String token;
    private static String usuarioId;
    private static String produtoId;
    private static String carrinhoId;

    @BeforeAll
    public static void beforeAll() {
        token = AuthUtils.criarUsuarioEObterToken();

        // Criar produto usando utilitário que retorna o ID
        produtoId = ProdUtils.criarProduto(token);
        System.out.println("Produto criado: ID = " + produtoId);
    }


    @Test
    @Order(1)
    public void cadastrarCarrinhoComProduto() {
        Map<String, Object> produtoItem = new HashMap<>();
        produtoItem.put("idProduto", produtoId);
        produtoItem.put("quantidade", 2);

        Map<String, Object> carrinhoBody = new HashMap<>();
        carrinhoBody.put("produtos", Collections.singletonList(produtoItem));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", token)
                .body(carrinhoBody)
                .when()
                .post("/carrinhos");

        response.then()
                .statusCode(HttpStatus.SC_CREATED)
                .log().all();

        carrinhoId = response.jsonPath().getString("_id");
        System.out.println("Carrinho criado: ID = " + carrinhoId);
    }

    @Test
    @Order(2)
    public void buscarCarrinhoPeloId() {
        given()
                .header("Authorization", token)
                .when()
                .get("/carrinhos/{id}", carrinhoId)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log().all();
    }

    @Test
    @Order(3)
    public void listarCarrinhos() {
        given()
                .header("Authorization", token)
                .when()
                .get("/carrinhos")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log().all();
    }

    @Test
    @Order(4)
    public void apagarCarrinho() {
        // Supondo que a API apaga carrinho pela ID
        given()
                .header("Authorization", token)
                .when()
                .delete("/carrinhos/concluir-compra")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log().all();
    }

    @Test
    @Order(5)
    public void cancelarCompraCarrinho() {
        // Gerar novo token (novo usuário)
        String novoToken = AuthUtils.criarUsuarioEObterToken();

        // Criar um produto novo para o carrinho
        String novoProdutoId = ProdUtils.criarProduto(novoToken);

        // Montar corpo do carrinho com o produto
        Map<String, Object> produtoItem = new HashMap<>();
        produtoItem.put("idProduto", novoProdutoId);
        produtoItem.put("quantidade", 1);

        Map<String, Object> carrinhoBody = new HashMap<>();
        carrinhoBody.put("produtos", Collections.singletonList(produtoItem));

        // Criar o carrinho para o usuário
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", novoToken)
                .body(carrinhoBody)
                .when()
                .post("/carrinhos")
                .then()
                .statusCode(HttpStatus.SC_CREATED);

        // Agora cancelar a compra (não passa ID, só token)
        given()
                .header("Authorization", novoToken)
                .when()
                .delete("/carrinhos/cancelar-compra")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log().all();
    }

    @AfterAll
    public static void afterAll() {
        // Deletando o usuário criado após todos os testes
        usuarioId = AuthUtils.getUsuarioId();
        if (usuarioId != null) {
            Response response = given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", token)
                    .when()
                    .delete("/usuarios/" + usuarioId);  // Endpoint para deletar o usuário com o ID específico

            // Verificando se o usuário foi apagado com sucesso
            response.then()
                    .statusCode(HttpStatus.SC_OK);  // Espera o status 200 OK
            System.out.println("Usuário com ID " + usuarioId + " deletado com sucesso.");
        }
    }
}
