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

public class LoginUserTests {
    private String email;
    private String password;
    private String name;
    private final List<String> tokens = new ArrayList<>();
    UserRequests userRequests;
    UserCredentials userCredentials;

    @Before
    @DisplayName("Подготовка тестовых данных перед началом каждой проверки")
    public void prepareTestData() {
        email = UUID.randomUUID() + "@somemail.com";
        password = "password";
        name = "Name";
        userRequests = new UserRequests();
        User user = new User(email, password, name);
        ValidatableResponse requestResponse = userRequests.createUser(user);
        tokens.add(requestResponse.extract().path("accessToken"));
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
    @DisplayName("Авторизация пользователя с корректными логином и паролем")
    public void userLogin() {
        userCredentials = new UserCredentials(email, password);
        ValidatableResponse requestResponse = userRequests.loginUser(userCredentials);
        int actualStatusCode = requestResponse.extract().statusCode();
        String responseMessage = requestResponse.extract().path("success").toString();

        Assert.assertEquals("Неправильный код ответа", HttpStatus.SC_OK, actualStatusCode);
        Assert.assertEquals("Неправильное сообщение в ответе", "true", responseMessage);
    }

    @Test
    @DisplayName("Попытка авторизации с некорректными данными")
    public void userLoginWithIncorrectCredentials() {
        userCredentials = new UserCredentials(email, "wrong_password");
        ValidatableResponse requestResponse = userRequests.loginUser(userCredentials);
        int actualStatusCode = requestResponse.extract().statusCode();
        String responseMessage = requestResponse.extract().path("success").toString();

        Assert.assertEquals("Неправильный код ответа", HttpStatus.SC_UNAUTHORIZED, actualStatusCode);
        Assert.assertEquals("Неправильное сообщение в ответе", "false", responseMessage);
    }

    @Test
    @DisplayName("Попытка авторизации с отсутствующими данными")
    public void userLoginWithAbsentCredential() {
        userCredentials = new UserCredentials("", password);
        ValidatableResponse requestResponse = userRequests.loginUser(userCredentials);
        int actualStatusCode = requestResponse.extract().statusCode();
        String responseMessage = requestResponse.extract().path("success").toString();

        Assert.assertEquals("Неправильный код ответа", HttpStatus.SC_UNAUTHORIZED, actualStatusCode);
        Assert.assertEquals("Неправильное сообщение в ответе", "false", responseMessage);
    }
}
