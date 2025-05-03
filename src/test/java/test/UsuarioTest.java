import com.github.javafaker.Faker;
import dto.UsuarioDTO;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;

@TestMethodOrder(OrderAnnotation.class)  // Controla a ordem dos testes
public class UsuarioTest {

    private static String usuarioId;  // Variável para armazenar o ID do usuário

    @BeforeAll
    public static void beforeAll() {
        baseURI = "https://serverest.dev/"; // URL base
    }

    @Test
    @Order(1)  // Define que este teste será executado primeiro
    public void cadastroUsuario() {
        UsuarioDTO usuarioDTO = new UsuarioDTO();
        Faker faker = new Faker();

        usuarioDTO.setAdministrador("true");
        usuarioDTO.setNome(faker.name().fullName());
        usuarioDTO.setEmail(faker.internet().emailAddress());
        usuarioDTO.setPassword("123");

        // Cadastro do usuário
        usuarioId = given()
                .contentType(ContentType.JSON)
                .body(usuarioDTO)
                .when().post("usuarios")
                .then()
                .statusCode(HttpStatus.SC_CREATED)  // Espera um status 201 Created
                .extract()
                .path("_id");  // Extrai o campo _id da resposta
        System.out.println("ID do usuário criado: " + usuarioId);  // Exibe o ID gerado para referência
    }

    @Test
    @Order(2)  // Define que este teste será executado em segundo lugar
    public void listarUsuarios() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("usuarios")
                .then()
                .statusCode(HttpStatus.SC_OK) // Espera o status de sucesso
                .log().all();
    }

    @Test
    @Order(3)  // Define que este teste será executado em terceiro lugar
    public void listarUsuarioPorId() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("usuarios/" + usuarioId)  // Usa o ID armazenado
                .then()
                .statusCode(HttpStatus.SC_OK) // Espera o status de sucesso
                .log().all();
    }

    @Test
    @Order(4)  // Define que este teste será executado por último
    public void atualizarUsuario() {
        UsuarioDTO usuarioDTO = new UsuarioDTO();
        Faker faker = new Faker();

        usuarioDTO.setAdministrador("false");
        usuarioDTO.setNome(faker.name().fullName());
        usuarioDTO.setEmail(faker.internet().emailAddress());
        usuarioDTO.setPassword(faker.internet().password());

        given()
                .contentType(ContentType.JSON)
                .body(usuarioDTO)
                .when()
                .put("usuarios/" + usuarioId)  // Usa o ID armazenado
                .then()
                .statusCode(HttpStatus.SC_OK) // Espera o status "200 OK"
                .log().all();
    }
    @Test
    @Order(5)  // Define que este teste será executado em quarto lugar
    public void apagarUsuario() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .delete("usuarios/" + usuarioId)  // Usa o ID armazenado
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log().all();
    }
}
