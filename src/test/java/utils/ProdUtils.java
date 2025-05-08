package utils;

import com.github.javafaker.Faker;
import dto.ProdutoDTO;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;


import static io.restassured.RestAssured.*;

public class ProdUtils {
    private static String token;
    @BeforeAll
    public static void beforeAll() {
        // Obter o token de autenticação com o método de AuthUtils
        token = AuthUtils.criarUsuarioEObterToken();


    }
    // Método para criar um único produto e retornar seu ID
    public static String criarProduto() {

        Faker faker = new Faker(); // Criando uma instância do Faker
        // Produto
        ProdutoDTO produtoDTO = new ProdutoDTO();
        produtoDTO.setNome(faker.commerce().productName()); // Nome aleatório do produto
        produtoDTO.setPreco(faker.number().numberBetween(1, 100));  // Preço aleatório do produto
        produtoDTO.setDescricao(faker.lorem().sentence());  // Descrição aleatória do produto
        produtoDTO.setQuantidade(faker.number().numberBetween(1, 100));  // Quantidade aleatória

        // Cadastrando o Produto
        Response response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", token)
                .body(produtoDTO)
                .when()
                .post("/produtos");

        // Verificando se o Produto foi criado com sucesso
        response.then().statusCode(201);

        // Obtendo e retornando o ID do produto
        String produtoId = response.jsonPath().getString("_id");
        System.out.println("ID do Produto: " + produtoId);
        return produtoId;
    }
}
