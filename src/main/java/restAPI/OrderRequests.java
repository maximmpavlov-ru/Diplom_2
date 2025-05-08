package restAPI;

import Ingredient.Ingredients;
import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;

import java.util.ArrayList;

import static io.restassured.RestAssured.given;

public class OrderRequests extends SpecificationData {

    @Step("Получение массива хеш кодов ингредиентов")
    public ArrayList<String> getIngredients() {
        ValidatableResponse validatableResponse = given()
                .spec(getBaseReqSpec())
                .when()
                .get("ingredients")
                .then();
        return validatableResponse.extract().path("data._id");
    }

    @Step("Создание нового заказа")
    public ValidatableResponse createOrder(Ingredients ingredients, String token) {
        return given()
                .spec(getBaseReqSpec())
                .header("Authorization", token)
                .body(ingredients)
                .when()
                .post("orders")
                .then();
    }

    @Step("Получение заказов для конкретного пользователя")
    public ValidatableResponse getOrdersForUser(String token) {
        return given()
                .spec(getBaseReqSpec())
                .header("Authorization", token)
                .when()
                .get("orders")
                .then();
    }
}
