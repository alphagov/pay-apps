package uk.gov.pay.products.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.products.model.Product;
import uk.gov.pay.products.service.ProductsFactory;
import uk.gov.pay.products.validations.ProductRequestValidator;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;
import static uk.gov.pay.products.resources.ProductResource.PRODUCTS_RESOURCE;

@Path(PRODUCTS_RESOURCE)
public class ProductResource {
    private static Logger logger = LoggerFactory.getLogger(HealthCheckResource.class);

    public static final String PRODUCTS_RESOURCE = "/v1/api/products";

    private final ProductRequestValidator requestValidator;
    private final ProductsFactory productsFactory;


    @Inject
    public ProductResource(ProductRequestValidator requestValidator, ProductsFactory productsFactory) {
        this.requestValidator = requestValidator;
        this.productsFactory = productsFactory;
    }

    @POST
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @PermitAll
    public Response createProduct(JsonNode payload) {
        logger.info("Create Service POST request - [ {} ]", payload);
        return requestValidator.validateCreateRequest(payload)
                .map(errors -> Response.status(Status.BAD_REQUEST).entity(errors).build())
                .orElseGet(() -> {
                    Product product = productsFactory.productsCreator().doCreate(Product.from(payload));
                    return Response.status(Status.CREATED).entity(product).build();
                });

    }

    @GET
    @Path("/{productExternalId}")
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @PermitAll
    public Response findProduct(@PathParam("productExternalId") String productExternalId) {
        logger.info("Find a product with externalId - [ {} ]", productExternalId);
        return productsFactory.productsFinder().findByExternalId(productExternalId)
                .map(product ->
                        Response.status(OK).entity(product).build())
                .orElseGet(() ->
                        Response.status(NOT_FOUND).build());
    }

    @DELETE
    @Path("/{productExternalId}")
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @PermitAll
    public Response deleteProduct(@PathParam("productExternalId") String productExternalId) {
        logger.info("Disabling a product with externalId - [ {} ]", productExternalId);
        productsFactory.productsFinder().disableProduct(productExternalId);
        return Response.status(NO_CONTENT).build();
    }
}
