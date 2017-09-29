package uk.gov.pay.products.resources;

import com.google.common.collect.ImmutableMap;
import io.restassured.response.ValidatableResponse;
import org.junit.Test;

import javax.ws.rs.HttpMethod;
import java.io.Serializable;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static uk.gov.pay.products.util.RandomIdGenerator.randomUuid;

public class ProductsResourceTest extends IntegrationTest {

    private static final String EXTERNAL_SERVICE_ID = "external_service_id";
    private static final String PAY_API_TOKEN = "pay_api_token";
    private static final String NAME = "name";
    private static final String PRICE = "price";
    private static final String EXTERNAL_ID = "external_id";
    private static final String DESCRIPTION = "description";
    private static final String RETURN_URL = "return_url";

    @Test
    public void shouldSuccess_whenSavingAValidProduct_withMinimumMandatoryFields() throws Exception {

        String externalServiceId = randomUuid();
        String payApiToken = randomUuid();
        String name = "Flashy new GOV Service";
        Long price = 1050L;

        ImmutableMap<String, ? extends Serializable> payload = ImmutableMap.of(
                EXTERNAL_SERVICE_ID, externalServiceId,
                PAY_API_TOKEN, payApiToken,
                NAME, name,
                PRICE, price);

        ValidatableResponse response = givenSetup()
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(mapper.writeValueAsString(payload))
                .post("/v1/api/products")
                .then()
                .statusCode(201);

        response
                .body(NAME, is("Flashy new GOV Service"))
                .body(EXTERNAL_SERVICE_ID, is(externalServiceId))
                .body(PRICE, is(1050))
                .body(EXTERNAL_ID, matchesPattern("^[0-9a-z]{32}$"));

        String externalId = response.extract().path(EXTERNAL_ID);

        System.out.println(response.extract().path("_links").toString());

        String productsUrl = "http://localhost:8080/v1/api/products/";
        String productsUIUrl = "http://localhost:3000/pay/";
        response
                .body("_links", hasSize(2))
                .body("_links[0].href", matchesPattern(productsUrl + externalId))
                .body("_links[0].method", is(HttpMethod.GET))
                .body("_links[0].rel", is("self"))
                .body("_links[1].href", matchesPattern(productsUIUrl + externalId))
                .body("_links[1].method", is(HttpMethod.POST))
                .body("_links[1].rel", is("pay"));

    }

    @Test
    public void shouldSuccess_whenSavingAValidProduct_withAllFields() throws Exception {

        String externalServiceId = randomUuid();
        String payApiToken = randomUuid();
        String name = "Flashy new GOV Service";
        Long price = 1050L;
        String description = "Some test description";

        String returnUrl = "http://some.valid.url";
        ImmutableMap<Object, Object> payload = ImmutableMap.builder()
                .put(EXTERNAL_SERVICE_ID, externalServiceId)
                .put(PAY_API_TOKEN, payApiToken)
                .put(NAME, name)
                .put(PRICE, price)
                .put(DESCRIPTION, description)
                .put(RETURN_URL, returnUrl)
                .build();

        ValidatableResponse response = givenSetup()
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(mapper.writeValueAsString(payload))
                .post("/v1/api/products")
                .then()
                .statusCode(201);

        response
                .body(NAME, is(name))
                .body(EXTERNAL_SERVICE_ID, is(externalServiceId))
                .body(PRICE, is(1050))
                .body(EXTERNAL_ID, matchesPattern("^[0-9a-z]{32}$"))
                .body(DESCRIPTION, is(description))
                .body(RETURN_URL, is(returnUrl));

        String externalId = response.extract().path(EXTERNAL_ID);

        System.out.println(response.extract().path("_links").toString());

        String productsUrl = "http://localhost:8080/v1/api/products/";
        String productsUIUrl = "http://localhost:3000/pay/";
        response
                .body("_links", hasSize(2))
                .body("_links[0].href", matchesPattern(productsUrl + externalId))
                .body("_links[0].method", is(HttpMethod.GET))
                .body("_links[0].rel", is("self"))
                .body("_links[1].href", matchesPattern(productsUIUrl + externalId))
                .body("_links[1].method", is(HttpMethod.POST))
                .body("_links[1].rel", is("pay"));

    }

    @Test
    public void shouldError_whenSavingAProduct_withMandatoryFieldsMissing() throws Exception {
        givenSetup()
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(mapper.writeValueAsString("{}"))
                .post("/v1/api/products")
                .then()
                .statusCode(400);
    }
}