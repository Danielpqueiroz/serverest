package utils;

import com.github.javafaker.Faker;
import dto.LoginDTO;
import dto.UsuarioDTO;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;

import static io.restassured.RestAssured.*;

import java.util.UUID;

public class AuthUtils {

    private static String token;
    private static String usuarioId;


    // Método para criar um usuário e obter o token de autenticação
    public static String criarUsuarioEObterToken() {
        baseURI = "https://serverest.dev/";
        //String email = "admin_" + UUID.randomUUID() + "@teste.com";  // Gerando um email único

        // Criando um objeto UsuarioDTO e Faker para preencher os dados
        UsuarioDTO usuarioDTO = new UsuarioDTO();
        Faker faker = new Faker();

        usuarioDTO.setAdministrador("true");
        usuarioDTO.setNome(faker.name().fullName());
        usuarioDTO.setEmail(faker.internet().emailAddress());  // Usando o email gerado
        usuarioDTO.setPassword(faker.internet().password(8, 12));  // Senha fixa para o usuário

        // 1. Criando o usuário admin
        Response createUserResponse = given()
                .contentType(ContentType.JSON)
                .body(usuarioDTO)
                .when()
                .post("/usuarios");

        // Verificando se o usuário foi criado com sucesso (status 201)
        createUserResponse.then().statusCode(201);

        // Extraindo o ID do usuário da resposta
        usuarioId = createUserResponse.jsonPath().getString("_id");
        System.out.println("ID do Usuário Criado: " + usuarioId);  // Imprimindo o ID do usuário

        // 2. Realizando o login para obter o token
        LoginDTO loginDTO = new LoginDTO(usuarioDTO.getEmail(), usuarioDTO.getPassword());
        Response loginResponse = given()
                .contentType(ContentType.JSON)
                .body(loginDTO)
                .when()
                .post("/login");

        // Validando se o login foi bem-sucedido (status 200)
        loginResponse.then().statusCode(200);

        // Extraindo o token de autorização da resposta e retornando
        token = loginResponse.jsonPath().getString("authorization");
        System.out.println("Token de Autenticação: " + token);  // Imprimindo o token

        return token;
    }



    // Getter para acessar o ID do usuário em outros testes, caso necessário
    public static String getUsuarioId() {
        return usuarioId;
    }
}
