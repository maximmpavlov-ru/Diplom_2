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

public class CreateOrderTests {
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
    @DisplayName("Создание заказа существующим пользователем")
    public void createOrder() {
        Ingredients ingredients = new Ingredients(orderRequests.getIngredients());
        ValidatableResponse requestResponse = orderRequests.createOrder(ingredients, token);
        int actualStatusCode = requestResponse.extract().statusCode();
        String orderBody = requestResponse.extract().body().toString();

        Assert.assertEquals("Неправильный код ответа", HttpStatus.SC_OK, actualStatusCode);
        Assert.assertNotNull("Тело ответа не может быть пустым", orderBody);
    }

    @Test
    @DisplayName("Создание заказа без авторизации")
    public void createOrderWithoutAuthToken() {
        Ingredients ingredients = new Ingredients(orderRequests.getIngredients());
        ValidatableResponse requestResponse = orderRequests.createOrder(ingredients, "");
        int actualStatusCode = requestResponse.extract().statusCode();
        String orderBody = requestResponse.extract().body().toString();

        Assert.assertEquals("Неправильный код ответа", HttpStatus.SC_OK, actualStatusCode);
        Assert.assertNotNull("Тело ответа не может быть пустым", orderBody);
    }

    @Test
    @DisplayName("Создание заказа без указания ингредиентов")
    public void createOrderWithoutIngredients() {
        Ingredients emptyListOfIngredients = new Ingredients(null);
        ValidatableResponse requestResponse = orderRequests.createOrder(emptyListOfIngredients, token);
        int actualStatusCode = requestResponse.extract().statusCode();
        String responseMessage = requestResponse.extract().path("success").toString();

        Assert.assertEquals("Неправильный код ответа", HttpStatus.SC_BAD_REQUEST, actualStatusCode);
        Assert.assertEquals("Неправильное сообщение в ответе", "false", responseMessage);
    }

    @Test
    @DisplayName("Создание заказа с указанием некорректного ингредиента")
    public void createOrderWithIncorrectIngredients() {
        List<String> badIngredients = new ArrayList<>();
        badIngredients.add("badIngredient");
        Ingredients listOfIncorrectIngredients = new Ingredients(badIngredients);
        ValidatableResponse requestResponse = orderRequests.createOrder(listOfIncorrectIngredients, token);
        int actualStatusCode = requestResponse.extract().statusCode();

        Assert.assertEquals("Неправильный код ответа", HttpStatus.SC_INTERNAL_SERVER_ERROR, actualStatusCode);
    }
}
