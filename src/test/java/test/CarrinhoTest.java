package test;

import com.github.javafaker.Faker;
import dto.CarrinhoDTO;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.*;
import utils.AuthUtils;
import utils.ProdUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CarrinhoTest {

    private static String token;
    private static String produtoId;
    private static String carrinhoId;
    private static int quantidadeGlobal;
    private static final List<String> usuariosCriados = new ArrayList<>();

    @BeforeAll
    public static void beforeAll() {
        token = AuthUtils.criarUsuarioEObterToken();
        String usuarioId = AuthUtils.getUsuarioId();
        usuariosCriados.add(usuarioId);

        produtoId = ProdUtils.criarProduto(token);
        System.out.println("Produto criado: ID = " + produtoId);
    }

    @Test
    @Order(1)
    public void cadastrarCarrinhoComProduto() {
        Faker faker = new Faker();
        quantidadeGlobal = faker.number().numberBetween(1, 3);

        CarrinhoDTO item = new CarrinhoDTO(produtoId, quantidadeGlobal);

        Map<String, Object> carrinho = new HashMap<>();
        carrinho.put("produtos", List.of(item));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", token)
                .body(carrinho)
                .when()
                .post("/carrinhos");

        response.then()
                .statusCode(HttpStatus.SC_CREATED)
                .body("message", equalTo("Cadastro realizado com sucesso"))
                .body("_id", notNullValue())
                .log().all();

        carrinhoId = response.jsonPath().getString("_id");
        System.out.println("Carrinho criado: ID = " + carrinhoId + " | Quantidade: " + quantidadeGlobal);
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
                .body("_id", equalTo(carrinhoId))
                .body("produtos", notNullValue())
                .body("produtos.size()", greaterThan(0))
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
                .body("carrinhos", notNullValue())
                .body("carrinhos.size()", greaterThan(0))
                .log().all();
    }

    @Test
    @Order(4)
    public void apagarCarrinho() {
        given()
                .header("Authorization", token)
                .when()
                .delete("/carrinhos/concluir-compra")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("message", equalTo("Registro excluído com sucesso"))
                .log().all();
    }

    @Test
    @Order(5)
    public void cancelarCompraCarrinho() {
        String novoToken = AuthUtils.criarUsuarioEObterToken();
        String novoUsuarioId = AuthUtils.getUsuarioId();
        usuariosCriados.add(novoUsuarioId);

        CarrinhoDTO item = new CarrinhoDTO(produtoId, quantidadeGlobal);

        Map<String, Object> carrinhoBody = new HashMap<>();
        carrinhoBody.put("produtos", List.of(item));

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", novoToken)
                .body(carrinhoBody)
                .log().body()
                .when()
                .post("/carrinhos")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .body("message", equalTo("Cadastro realizado com sucesso"))
                .body("_id", notNullValue());

        given()
                .header("Authorization", novoToken)
                .when()
                .delete("/carrinhos/cancelar-compra")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("message", equalTo("Registro excluído com sucesso. Estoque dos produtos reabastecido"))
                .log().all();
    }

    @Test
    @Order(6)
    public void naoPermitirProdutoDuplicadoNoCarrinho() {
        // Novo usuário isolado para o teste
        String tokenDuplicado = AuthUtils.criarUsuarioEObterToken();
        String usuarioIdDuplicado = AuthUtils.getUsuarioId();
        usuariosCriados.add(usuarioIdDuplicado);

        // Criar dois itens com o MESMO produtoId
        CarrinhoDTO item1 = new CarrinhoDTO(produtoId, quantidadeGlobal);
        CarrinhoDTO item2 = new CarrinhoDTO(produtoId, quantidadeGlobal);

        // Enviar no corpo como lista de produtos duplicados
        Map<String, Object> carrinhoDuplicado = new HashMap<>();
        carrinhoDuplicado.put("produtos", List.of(item1, item2));

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", tokenDuplicado)
                .body(carrinhoDuplicado)
                .log().body()
                .when()
                .post("/carrinhos")
                .then()
                .log().all()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("message", equalTo("Não é permitido possuir produto duplicado"))
                .body("idProdutosDuplicados", hasItem(produtoId));
    }

    @AfterAll
    public static void afterAll() {
        for (String usuarioId : usuariosCriados) {
            String tokenAdmin = AuthUtils.criarUsuarioEObterToken();
            Response response = given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", tokenAdmin)
                    .when()
                    .delete("/usuarios/" + usuarioId);

            response.then()
                    .statusCode(HttpStatus.SC_OK);
            System.out.println("Usuário deletado: " + usuarioId);
        }
    }
}
