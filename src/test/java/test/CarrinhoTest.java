package test;

import dto.ProdutoDTO;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Order;
import utils.AuthUtils;
import utils.ProdUtils;

import static io.restassured.RestAssured.given;

@TestMethodOrder(org.junit.jupiter.api.MethodOrderer.OrderAnnotation.class)  // Garante a execução na ordem correta
public class CarrinhoTest {

    private static String token;
    private static String usuarioId;  // ID do usuário criado
    private static String produtoId;
    private static String carrinhoId;

    @BeforeAll
    public static void beforeAll() {
        // Autentica e obtém o token
        token = AuthUtils.criarUsuarioEObterToken();

        // Usando o ProdUtils para criar o produto e obter o ID do produto
        produtoId = ProdUtils.criarProduto(token);  // Passa o token para o ProdUtils
        System.out.println("Produto Criado: ID = " + produtoId);
    }

    @Test
    @Order(1)
    public void cadastrarCarrinhoComProduto() {
        String carrinhoJson = "{\n" +
                "  \"produtos\": [\n" +
                "    { \"idProduto\": \"" + produtoId + "\", \"quantidade\": 2 }\n" +
                "  ]\n" +
                "}";

        // Cadastrando o carrinho
        Response response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", token)
                .body(carrinhoJson)
                .when()
                .post("/carrinhos");

        // Verificando se o Carrinho foi criado com sucesso
        response.then()
                .statusCode(HttpStatus.SC_CREATED)
                .log().all();

        // Armazenando o ID do carrinho para ser usado nos testes seguintes
        carrinhoId = response.jsonPath().getString("_id");
        System.out.println("Carrinho Criado: ID = " + carrinhoId);
    }

    @Test
    @Order(2)
    public void buscarCarrinhoPeloId() {
        // Buscando o carrinho pelo ID
        Response response = given()
                .header("Authorization", token)
                .when()
                .get("/carrinhos/" + carrinhoId);

        // Validando a resposta da busca pelo carrinho
        response.then()
                .statusCode(HttpStatus.SC_OK)
                .log().all();
    }

    @Test
    @Order(3)
    public void listarCarrinhos() {
        // Buscando todos os carrinhos
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
        // Apagando o carrinho criado
        Response response = given()
                .header("Authorization", token)
                .when()
                .delete("/carrinhos/concluir-compra");

        // Verificando se o carrinho foi apagado com sucesso
        response.then()
                .statusCode(HttpStatus.SC_OK)  // Espera o status 204 No Content
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
