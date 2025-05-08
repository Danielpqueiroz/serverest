package test;

import com.github.javafaker.Faker;
import dto.ProdutoDTO;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import utils.AuthUtils;

import static io.restassured.RestAssured.given;

public class CarrinhoTest {

    private static String token;
    private static String produtoId;

    @BeforeAll
    public static void beforeAll() {
        // Autentica e obtém o token
        token = AuthUtils.criarUsuarioEObterToken();

        // Cria um produto diretamente
        Faker faker = new Faker();
        ProdutoDTO produtoDTO = new ProdutoDTO();
        produtoDTO.setNome(faker.commerce().material() + " " + faker.commerce().productName());
        produtoDTO.setPreco(faker.number().numberBetween(1, 100));
        produtoDTO.setDescricao(faker.lorem().sentence());
        produtoDTO.setQuantidade(faker.number().numberBetween(1, 100));
        System.out.println(produtoDTO);
        System.out.println("Nome: " + produtoDTO.getNome());
        System.out.println("Preço: " + produtoDTO.getPreco());
        System.out.println("Descrição: " + produtoDTO.getDescricao());
        System.out.println("Quantidade: " + produtoDTO.getQuantidade());
        Response response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", token)
                .body(produtoDTO)
                .when()
                .post("/produtos");

        response.then().statusCode(HttpStatus.SC_CREATED);
        produtoId = response.jsonPath().getString("_id");
        System.out.println(produtoId);
        System.out.println(response.jsonPath());
    }

    @Test
    public void cadastrarCarrinhoComProduto() {
        String carrinhoJson = "{\n" +
                "  \"produtos\": [\n" +
                "    { \"idProduto\": \"" + produtoId + "\", \"quantidade\": 2 }\n" +
                "  ]\n" +
                "}";

        Response response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", token)
                .body(carrinhoJson)
                .when()
                .post("/carrinhos");

        response.then()
                .statusCode(HttpStatus.SC_CREATED)
                .log().all();
    }

    @Test
    public void listarCarrinhos() {
        given()
                .header("Authorization", token)
                .when()
                .get("/carrinhos")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log().all();
    }
}
