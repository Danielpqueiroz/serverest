package utils;

import com.github.javafaker.Faker;
import dto.ProdutoDTO;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;

import static io.restassured.RestAssured.given;

public class ProdUtils {

    private static final Faker faker = new Faker();

    // Método para criar um ProdutoDTO com dados aleatórios
    public static ProdutoDTO criarProdutoDTO() {
        ProdutoDTO produtoDTO = new ProdutoDTO();
        produtoDTO.setNome(faker.commerce().material() + " " + faker.commerce().productName());
        produtoDTO.setPreco(faker.number().numberBetween(1, 100));
        produtoDTO.setDescricao(faker.lorem().sentence());
        produtoDTO.setQuantidade(faker.number().numberBetween(1, 100));
        return produtoDTO;
    }

    // Método para cadastrar um produto e retornar o ID criado
    public static String criarProduto(String token) {
        ProdutoDTO produtoDTO = criarProdutoDTO();

        Response response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", token)
                .body(produtoDTO)
                .when()
                .post("/produtos");

        response.then().statusCode(HttpStatus.SC_CREATED);

        return response.jsonPath().getString("_id");
    }

    // Método para cadastrar produto com ProdutoDTO passado, retornando Response
    public static Response cadastrarProduto(String token, ProdutoDTO produtoDTO) {
        return given()
                .contentType(ContentType.JSON)
                .header("Authorization", token)
                .body(produtoDTO)
                .when()
                .post("/produtos");
    }
}
