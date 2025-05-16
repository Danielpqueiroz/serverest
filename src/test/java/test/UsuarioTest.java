import com.github.javafaker.Faker;
import dto.UsuarioDTO;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UsuarioTest {

    private static String usuarioId;
    private static String emailUsado;
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

        emailUsado = usuarioDTO.getEmail(); // Salva o email criado para usar depois

        usuarioId = given()
                .contentType(ContentType.JSON)
                .body(usuarioDTO)
                .when()
                .post("usuarios")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .body("_id", notNullValue())
                .body("message", equalTo("Cadastro realizado com sucesso"))
                .log().all()
                .extract()
                .path("_id");


        System.out.println("ID do usuário criado: " + usuarioId);
    }

    @Test
    @Order(2)
    public void cadastroUsuarioComEmailExistente() {
        UsuarioDTO usuarioDTO = criarUsuarioDTO("false");
        usuarioDTO.setEmail(emailUsado); // Usa o email que já foi cadastrado

        given()
                .contentType(ContentType.JSON)
                .body(usuarioDTO)
                .when()
                .post("usuarios")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("message", containsString("Este email já está sendo usado"))
                .log().all();
    }

    @Test
    @Order(3)
    public void listarUsuarios() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("usuarios")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("quantidade", greaterThan(0))
                .body("usuarios", not(empty()))
                .body("usuarios[0].nome", not(emptyString()))
                .body("usuarios[0].email", not(emptyString()))
                .body("usuarios[0].password", not(emptyString()))
                .body("usuarios[0].administrador", anyOf(equalTo("true"), equalTo("false")))
                .body("usuarios[0]._id", notNullValue())
                .log().all();
    }

    @Test
    @Order(4)
    public void listarUsuarioPorId() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("usuarios/{id}", usuarioId)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("_id", equalTo(usuarioId))
                .body("nome", not(emptyString()))
                .body("email", not(emptyString()))
                .body("password", not(emptyString()))
                .body("administrador", anyOf(equalTo("true"), equalTo("false")))
                .log().all();
    }

    @Test
    @Order(5)
    public void atualizarUsuario() {
        UsuarioDTO usuarioDTO = criarUsuarioDTO("false");

        given()
                .contentType(ContentType.JSON)
                .body(usuarioDTO)
                .when()
                .put("usuarios/{id}", usuarioId)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("message", equalTo("Registro alterado com sucesso"))
                .log().all();
    }

    @Test
    @Order(6)
    public void apagarUsuario() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .delete("usuarios/{id}", usuarioId)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("message", equalTo("Registro excluído com sucesso"))
                .log().all();
    }

    private UsuarioDTO criarUsuarioDTO(String administrador) {
        UsuarioDTO usuarioDTO = new UsuarioDTO();
        usuarioDTO.setAdministrador(administrador);
        usuarioDTO.setNome(faker.name().fullName());
        usuarioDTO.setEmail(faker.internet().emailAddress());
        usuarioDTO.setPassword("123");
        return usuarioDTO;
    }
}
