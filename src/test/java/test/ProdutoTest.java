package test;

import dto.ProdutoDTO;
import com.github.javafaker.Faker;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import utils.AuthUtils;

import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)  // Controla a ordem dos testes
public class ProdutoTest {

    private static String produtoId;  // Variável para armazenar o ID do produto

    @Test
    @Order(1)  // Define que este teste será executado primeiro
    public void cadastrarProduto() {
        String token = AuthUtils.criarUsuarioEObterToken();

        ProdutoDTO produtoDTO = new ProdutoDTO();
        Faker faker = new Faker();

        produtoDTO.setNome("Notebook Lenovo " + UUID.randomUUID());
        produtoDTO.setPreco(faker.number().numberBetween(0, 3500));
        produtoDTO.setDescricao(faker.book().title());
        produtoDTO.setQuantidade(faker.number().numberBetween(1, 100));

        // Cadastro do Produto
        produtoId = given()
                .baseUri("https://serverest.dev")
                .header("Authorization", token)
                .contentType(ContentType.JSON)
                .body(produtoDTO)
                .when()
                .post("produtos")
                .then()
                .log().all()
                .statusCode(HttpStatus.SC_CREATED) // Espera um status 201 Created
                .extract()
                .path("_id"); // Extrai o campo _id da resposta
        System.out.println("ID do produto criado: " + produtoId);  // Exibe o ID gerado para referência
    }

    @Test
    @Order(2)  // Define que este teste será executado em segundo lugar
    public void cadastrarProdutoSemToken() {
        ProdutoDTO produtoDTO = new ProdutoDTO();
        Faker faker = new Faker();

        produtoDTO.setNome("Notebook Lenovo " + UUID.randomUUID());
        produtoDTO.setPreco(faker.number().numberBetween(0, 3500));
        produtoDTO.setDescricao(faker.book().title());
        produtoDTO.setQuantidade(faker.number().numberBetween(1, 100));

        // Cadastro do Produto sem Token
        produtoId = given()
                .baseUri("https://serverest.dev")
                .contentType(ContentType.JSON)
                .body(produtoDTO)
                .when()
                .post("produtos")
                .then()
                .log().all()
                .statusCode(HttpStatus.SC_UNAUTHORIZED) // Espera um status 401 Unauthorized para login com falha
                .extract()
                .path("message", String.valueOf(is("Token de acesso ausente, inválido, expirado ou usuário do token não existe mais")));
    }
}
