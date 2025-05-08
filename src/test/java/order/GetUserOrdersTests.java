package order;

import Ingredient.Ingredients;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import restAPI.OrderRequests;
import restAPI.UserRequests;
import user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GetUserOrdersTests {
    private String token;
    private final List<String> tokens = new ArrayList<>();
    UserRequests userRequests;
    OrderRequests orderRequests;

    @Before
    @DisplayName("Подготовка тестовых данных перед началом каждой проверки")
    public void prepareTestData() {
        String email = UUID.randomUUID() + "@somemail.com";
        String password = "password";
        String name = "Name";
        userRequests = new UserRequests();
        orderRequests = new OrderRequests();
        User user = new User(email, password, name);
        ValidatableResponse requestResponse = userRequests.createUser(user);
        tokens.add(token = requestResponse.extract().path("accessToken"));
        Ingredients ingredients = new Ingredients(orderRequests.getIngredients());
        orderRequests.createOrder(ingredients, token);
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
    @DisplayName("Получение заказов пользователя")
    public void getUserOrderList() {
        ValidatableResponse requestResponse = orderRequests.getOrdersForUser(token);

        int actualStatusCode = requestResponse.extract().statusCode();
        String orderBody = requestResponse.extract().body().toString();

        Assert.assertEquals("Неправильный код ответа", HttpStatus.SC_OK, actualStatusCode);
        Assert.assertNotNull("Тело ответа не может быть пустым", orderBody);
    }

    @Test
    @DisplayName("Получение заказов пользователя без авторизации")
    public void getUserOrderListWithoutAuthToken() {
        ValidatableResponse requestResponse = orderRequests.getOrdersForUser("");

        int actualStatusCode = requestResponse.extract().statusCode();
        String responseMessage = requestResponse.extract().path("success").toString();

        Assert.assertEquals("Неправильный код ответа", HttpStatus.SC_UNAUTHORIZED, actualStatusCode);
        Assert.assertEquals("Неправильное сообщение в ответе", "false", responseMessage);
    }
}
