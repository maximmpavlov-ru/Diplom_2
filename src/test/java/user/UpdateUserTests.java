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

public class UpdateUserTests {
    private String email;
    private String password;
    private String name;
    private String token;
    private final List<String> tokens = new ArrayList<>();
    UserRequests userRequests;

    @Before
    @DisplayName("Подготовка тестовых данных перед началом каждой проверки")
    public void prepareTestData() {
        email = UUID.randomUUID() + "@somemail.com";
        password = "password";
        name = "Name";
        userRequests = new UserRequests();
        User user = new User(email, password, name);
        ValidatableResponse requestResponse = userRequests.createUser(user);
        tokens.add(token = requestResponse.extract().path("accessToken"));
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
    @DisplayName("Успешное обновление данных пользователя")
    public void updateUser() {
        String newEmail = email + "_update";
        String newPassword = password + "_update";
        String newName = name + "_update";
        User newUser = new User(newEmail, newPassword, newName);
        ValidatableResponse requestResponse = userRequests.updateUser(newUser, token);
        int actualStatusCode = requestResponse.extract().statusCode();
        String updatedEmail = requestResponse.extract().body().path("user.email");
        String updatedName = requestResponse.extract().body().path("user.name");

        Assert.assertEquals("Неправильный код ответа", HttpStatus.SC_OK, actualStatusCode);
        Assert.assertEquals("e-mail пользователя не был обновлён", newEmail, updatedEmail);
        Assert.assertEquals("Имя пользователя не было обновлено", newName, updatedName);
    }

    @Test
    @DisplayName("Попытка обновления данных пользователя без авторизации")
    public void updateUserWithoutAuthToken() {
        User user = new User(email, password, name);
        ValidatableResponse requestResponse = userRequests.updateUser(user, "bad_token");
        int actualStatusCode = requestResponse.extract().statusCode();
        String responseMessage = requestResponse.extract().path("message").toString();

        Assert.assertEquals("Неправильный код ответа", HttpStatus.SC_UNAUTHORIZED, actualStatusCode);
        Assert.assertEquals("Неправильное сообщение в ответе", "You should be authorised", responseMessage);
    }
}
