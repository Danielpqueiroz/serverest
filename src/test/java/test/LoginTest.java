package test;

import com.github.javafaker.Faker;
import dto.UsuarioDTO;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;  // Importando @AfterAll
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class LoginTest {

    private static String usuarioId;  // Variável para armazenar o ID do usuário
    private static String emailCadastro;
    private static String senhaCadastro;

    // Método @BeforeAll, executado uma vez antes de todos os testes
    @BeforeAll
    public static void beforeAll() {
        baseURI = "https://serverest.dev/"; // URL base da API

        // Criando o usuário apenas uma vez antes de todos os testes
        UsuarioDTO usuarioDTO = new UsuarioDTO();
        Faker faker = new Faker();

        usuarioDTO.setAdministrador("true");
        usuarioDTO.setNome(faker.name().fullName());
        emailCadastro = faker.internet().emailAddress();  // Gerando um email para o cadastro
        usuarioDTO.setEmail(emailCadastro);
        senhaCadastro = "123";  // Definindo uma senha fixa para o cadastro
        usuarioDTO.setPassword(senhaCadastro);

        // Cadastro do usuário com validação da resposta
        usuarioId = given()
                .contentType(ContentType.JSON)
                .body(usuarioDTO)
                .when().post("usuarios")
                .then()
                .statusCode(HttpStatus.SC_CREATED)  // Espera um status 201 Created
                .body("_id", notNullValue())
                .body("message", equalTo("Cadastro realizado com sucesso"))
                .log().all()
                .extract()
                .path("_id");  // Extrai o campo _id da resposta

        System.out.println("ID do usuário criado: " + usuarioId);  // Exibe o ID gerado para referência
    }

    // Teste de Login com Sucesso (usuário cadastrado com sucesso)
    @Test
    public void loginUsuarioComSucesso() {
        // Realizando o login com o usuário criado
        String loginPayload = "{ \"email\": \"" + emailCadastro + "\", \"password\": \"" + senhaCadastro + "\" }";

        given()
                .contentType(ContentType.JSON)
                .body(loginPayload)  // Dados do login
                .when()
                .post("login")  // Endpoint de login
                .then()
                .statusCode(HttpStatus.SC_OK)  // Espera um status 200 OK para login bem-sucedido
                .body("authorization", notNullValue()) // Verifica se o token de autorização está presente
                .body("message", equalTo("Login realizado com sucesso"))
                .log().all();
    }

    // Teste de Login com Falha (usuário não encontrado ou senha errada)
    @Test
    public void loginUsuarioComFalha() {
        // Tentativa de login com credenciais incorretas
        String loginPayload = "{ \"email\": \"wrongemail@example.com\", \"password\": \"wrongpassword\" }";

        given()
                .contentType(ContentType.JSON)
                .body(loginPayload)  // Dados do login com credenciais erradas
                .when()
                .post("login")  // Endpoint de login
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED)  // Espera um status 401 Unauthorized para login com falha
                .body("message", equalTo("Email e/ou senha inválidos"))
                .log().all();
    }

    // Teste de Login com Falta de Senha
    @Test
    public void loginUsuarioSemSenha() {
        // Tentativa de login sem fornecer senha
        String loginPayload = "{ \"email\": \"" + emailCadastro + "\" }";  // Sem a chave de senha

        given()
                .contentType(ContentType.JSON)
                .body(loginPayload)  // Dados do login com senha faltando
                .when()
                .post("login")  // Endpoint de login
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)  // Espera um status 400 Bad Request
                .body("password", equalTo("password é obrigatório"))
                .log().all();
    }

    // Método @AfterAll, executado uma vez após todos os testes
    @AfterAll
    public static void afterAll() {
        // Deletar o usuário criado após os testes
        given()
                .contentType(ContentType.JSON)
                .when()
                .delete("usuarios/" + usuarioId)  // Endpoint para deletar o usuário com o ID específico
                .then()
                .statusCode(HttpStatus.SC_OK)  // Espera um status 200 OK para deleção bem sucedida
                .body("message", equalTo("Registro excluído com sucesso"))
                .log().all();
        System.out.println("Usuário com ID " + usuarioId + " deletado com sucesso.");
    }
}
