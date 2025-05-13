package utils;

import com.github.javafaker.Faker;
import dto.ProdutoDTO;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;

import static io.restassured.RestAssured.given;

public class ProdUtils {

    // Função para criar um produto e retornar o ID do produto criado
    public static String criarProduto(String token) {
        // Usando o Faker para gerar dados aleatórios do produto
        Faker faker = new Faker();
        ProdutoDTO produtoDTO = new ProdutoDTO();
        produtoDTO.setNome(faker.commerce().material() + " " + faker.commerce().productName());
        produtoDTO.setPreco(faker.number().numberBetween(1, 100));
        produtoDTO.setDescricao(faker.lorem().sentence());
        produtoDTO.setQuantidade(faker.number().numberBetween(1, 100));


        // Cadastrando o produto
        Response response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", token)  // Passando o token corretamente
                .body(produtoDTO)  // Envia os dados do produto
                .when()
                .post("/produtos");

        // Verificando se o produto foi criado com sucesso (status 201)
        response.then().log().all();
        response.then().statusCode(HttpStatus.SC_CREATED);

        System.out.println(response.jsonPath());
        // Retorna o ID do produto criado
        return response.jsonPath().getString("_id");
    }
}
