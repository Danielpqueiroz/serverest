package test;

import dto.ProdutoDTO;
import com.github.javafaker.Faker;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import utils.AuthUtils;

import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProdutoTest {

    private static String usuarioId;
    private static String produtoId;
    private static String token;

    @BeforeAll
    public static void setup() {
        token = AuthUtils.criarUsuarioEObterToken();
    }

    @Test
    @Order(1)
    public void cadastrarProduto() {
        ProdutoDTO produtoDTO = criarProdutoDTO();

        produtoId = given()
                .header("Authorization", token)
                .contentType(ContentType.JSON)
                .body(produtoDTO)
                .when()
                .post("produtos")
                .then()
                .log().all()
                .statusCode(HttpStatus.SC_CREATED)
                .body("message", is("Cadastro realizado com sucesso"))
                .body("_id", notNullValue())
                .extract()
                .path("_id");

        System.out.println("ID do produto criado: " + produtoId);
    }

    @Test
    @Order(2)
    public void buscarTodosProdutos() {
        given()
                .header("Authorization", token)
                .when()
                .get("produtos")
                .then()
                .log().all()
                .statusCode(HttpStatus.SC_OK)
                .body("produtos", not(empty()))
                .body("produtos._id", hasItem(produtoId));
    }

    @Test
    @Order(3)
    public void buscarProdutoPorId() {
        given()
                .header("Authorization", token)
                .when()
                .get("produtos/{id}", produtoId)
                .then()
                .log().all()
                .statusCode(HttpStatus.SC_OK)
                .body("_id", is(produtoId))
                .body("nome", notNullValue())
                .body("preco", greaterThanOrEqualTo(0))
                .body("descricao", notNullValue())
                .body("quantidade", greaterThanOrEqualTo(0));
    }

    @Test
    @Order(4)
    public void atualizarProduto() {
        ProdutoDTO produtoAtualizado = criarProdutoDTO();

        given()
                .header("Authorization", token)
                .contentType(ContentType.JSON)
                .body(produtoAtualizado)
                .when()
                .put("produtos/{id}", produtoId)
                .then()
                .log().all()
                .statusCode(HttpStatus.SC_OK)
                .body("message", is("Registro alterado com sucesso"));
    }

    @Test
    @Order(5)
    public void apagarProduto() {
        given()
                .header("Authorization", token)
                .when()
                .delete("produtos/{id}", produtoId)
                .then()
                .log().all()
                .statusCode(HttpStatus.SC_OK)
                .body("message", is("Registro excluído com sucesso"));
    }

    private ProdutoDTO criarProdutoDTO() {
        Faker faker = new Faker();
        ProdutoDTO produtoDTO = new ProdutoDTO();

        produtoDTO.setNome("Notebook Lenovo " + UUID.randomUUID());
        produtoDTO.setPreco(faker.number().numberBetween(1, 3500)); // preco > 0
        produtoDTO.setDescricao(faker.book().title());
        produtoDTO.setQuantidade(faker.number().numberBetween(1, 100)); // quantidade >= 1

        return produtoDTO;
    }

    @Test
    @Order(6)
    public void cadastrarProdutoSemToken() {
        ProdutoDTO produtoDTO = criarProdutoDTO();

        given()
                .contentType(ContentType.JSON)
                .body(produtoDTO)
                .when()
                .post("produtos")
                .then()
                .log().all()
                .statusCode(HttpStatus.SC_UNAUTHORIZED)
                .body("message", is("Token de acesso ausente, inválido, expirado ou usuário do token não existe mais"));
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
                    .delete("/usuarios/" + usuarioId);

            response.then()
                    .statusCode(HttpStatus.SC_OK);
            System.out.println("Usuário com ID " + usuarioId + " deletado com sucesso.");
        }
    }
}
