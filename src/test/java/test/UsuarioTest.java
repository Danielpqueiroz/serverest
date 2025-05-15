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

@TestMethodOrder(OrderAnnotation.class)
public class UsuarioTest {

    private static String usuarioId;
    private static Faker faker;

    @BeforeAll
    public static void beforeAll() {
        baseURI = "https://serverest.dev/";
        faker = new Faker();
    }

    @Test
    @Order(1)
    public void cadastroUsuario() {
        UsuarioDTO usuarioDTO = criarUsuarioDTO("true");

        usuarioId = given()
                .contentType(ContentType.JSON)
                .body(usuarioDTO)
                .when()
                .post("usuarios")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("_id");

        System.out.println("ID do usuário criado: " + usuarioId);
    }

    @Test
    @Order(2)
    public void listarUsuarios() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("usuarios")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log().all();
    }

    @Test
    @Order(3)
    public void listarUsuarioPorId() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("usuarios/{id}", usuarioId)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log().all();
    }

    @Test
    @Order(4)
    public void atualizarUsuario() {
        UsuarioDTO usuarioDTO = criarUsuarioDTO("false");

        given()
                .contentType(ContentType.JSON)
                .body(usuarioDTO)
                .when()
                .put("usuarios/{id}", usuarioId)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log().all();
    }

    @Test
    @Order(5)
    public void apagarUsuario() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .delete("usuarios/{id}", usuarioId)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log().all();
    }

    private UsuarioDTO criarUsuarioDTO(String administrador) {
        UsuarioDTO usuarioDTO = new UsuarioDTO();
        usuarioDTO.setAdministrador(administrador);
        usuarioDTO.setNome(faker.name().fullName());
        usuarioDTO.setEmail(faker.internet().emailAddress());
        usuarioDTO.setPassword("123");  // Pode alterar para faker.internet().password() se quiser senha aleatória
        return usuarioDTO;
    }
}
