package user;

import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import restAPI.UserRequests;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CreateUserTests {
    private String email;
    private String password;
    private String name;
    private final List<String> tokens = new ArrayList<>();
    UserRequests userRequests;

    @Before
    @DisplayName("Подготовка тестовых данных перед началом каждой проверки")
    public void prepareTestData() {
        email = UUID.randomUUID() + "@somemail.com";
        password = "password";
        name = "Name";
        userRequests = new UserRequests();
    }

    @After
    @DisplayName("Удаление тестовых данных после каждой проверки")
    public void cleanUp() {
        if (tokens.isEmpty())
            return;
        for (String token : tokens) {
            userRequests.deleteUser(token);
        }
    }

    @Test
    @DisplayName("Успешное создание нового пользователя")
    public void createUser() {
        User user = new User(email, password, name);
        ValidatableResponse requestResponse = userRequests.createUser(user);
        tokens.add(requestResponse.extract().path("accessToken"));
        int actualStatusCode = requestResponse.extract().statusCode();
        String responseMessage = requestResponse.extract().path("success").toString();

        Assert.assertEquals("Неправильный код ответа", HttpStatus.SC_OK, actualStatusCode);
        Assert.assertEquals("Неправильное сообщение в ответе", "true", responseMessage);

    }

    @Test
    @DisplayName("Попытка создания пользователя, который уже существует в системе")
    public void createUserWithDuplicatedData() {
        User user = new User(email, password, name);
        ValidatableResponse firstRequestResponse = userRequests.createUser(user);
        tokens.add(firstRequestResponse.extract().path("accessToken"));
        ValidatableResponse secondRequestResponse = userRequests.createUser(user);
        int actualStatusCode = secondRequestResponse.extract().statusCode();
        String responseMessage = secondRequestResponse.extract().path("success").toString();

        Assert.assertEquals("Неправильный код ответа", HttpStatus.SC_FORBIDDEN, actualStatusCode);
        Assert.assertEquals("Неправильное сообщение в ответе", "false", responseMessage);
    }

    @Test
    @DisplayName("Попытка создания пользователя без указания всех обязательных полей")
    public void createUserWithoutMandatoryData() {
        User user = new User(email, "", name);
        ValidatableResponse requestResponse = userRequests.createUser(user);
        int actualStatusCode = requestResponse.extract().statusCode();
        String responseMessage = requestResponse.extract().path("success").toString();

        Assert.assertEquals("Неправильный код ответа", HttpStatus.SC_FORBIDDEN, actualStatusCode);
        Assert.assertEquals("Неправильное сообщение в ответе", "false", responseMessage);
    }
}
