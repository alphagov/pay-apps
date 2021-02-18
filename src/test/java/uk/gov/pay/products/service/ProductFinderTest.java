package uk.gov.pay.products.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.products.model.Product;
import uk.gov.pay.products.persistence.dao.ProductDao;
import uk.gov.pay.products.persistence.entity.ProductEntity;
import uk.gov.pay.products.util.ProductStatus;
import uk.gov.pay.products.util.ProductType;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProductFinderTest {

    @Mock
    private ProductDao productDao;

    private ProductFinder productFinder;

    @Before
    public void setup() {
        LinksDecorator linksDecorator = new LinksDecorator("http://localhost", "http://localhost/pay", "http://localhost/payments");
        productFinder = new ProductFinder(productDao, linksDecorator);
    }

    @Test
    public void findByExternalId_shouldReturnProduct_whenFound() {
        String externalId = "1";
        ProductEntity productEntity = new ProductEntity();
        productEntity.setExternalId(externalId);
        productEntity.setReferenceEnabled(false);
        when(productDao.findByExternalId(externalId)).thenReturn(Optional.of(productEntity));

        Optional<Product> productOptional = productFinder.findByExternalId(externalId);

        assertTrue(productOptional.isPresent());
        assertThat(productOptional.get().getExternalId(), is(externalId));
    }

    @Test
    public void findByExternalId_shouldReturnEmpty_whenNotFound() {
        String externalId = "1";
        when(productDao.findByExternalId(externalId)).thenReturn(Optional.empty());

        Optional<Product> productOptional = productFinder.findByExternalId(externalId);

        assertFalse(productOptional.isPresent());
    }

    @Test
    public void findByGatewayAccountId_shouldReturnDecoratedProduct_whenFound() {
        Integer gatewayAccountId = 1;
        String externalId = "1";
        ProductEntity productEntity = new ProductEntity();
        productEntity.setExternalId(externalId);
        productEntity.setGatewayAccountId(gatewayAccountId);
        productEntity.setReferenceEnabled(false);
        when(productDao.findByGatewayAccountId(gatewayAccountId)).thenReturn(List.of(productEntity));

        List<Product> productsList = productFinder.findByGatewayAccountId(gatewayAccountId);

        assertThat(productsList.size(), is(1));
        assertThat(productsList.get(0).getExternalId(), is(externalId));
        assertThat(productsList.get(0).getLinks().size(), is(2));
    }

    @Test
    public void findByGatewayAccountIdAndType_shouldReturnDecoratedProduct_whenFound() {
        Integer gatewayAccountId = 1;
        String externalId = "1";
        ProductEntity productEntity = new ProductEntity();
        productEntity.setExternalId(externalId);
        productEntity.setGatewayAccountId(gatewayAccountId);
        productEntity.setType(ProductType.ADHOC);
        productEntity.setReferenceEnabled(false);
        when(productDao.findByGatewayAccountIdAndType(gatewayAccountId, ProductType.ADHOC)).thenReturn(List.of(productEntity));

        List<Product> productsList = productFinder.findByGatewayAccountIdAndType(gatewayAccountId, ProductType.ADHOC);

        assertThat(productsList.size(), is(1));
        assertThat(productsList.get(0).getExternalId(), is(externalId));
        assertThat(productsList.get(0).getLinks().size(), is(greaterThan(0)));
    }

    @Test
    public void findByGatewayAccountIdAndExternalId_shouldReturnProduct_whenFound() {
        Integer gatewayAccountId = 1;
        String externalId = "1";
        ProductEntity productEntity = new ProductEntity();
        productEntity.setExternalId(externalId);
        productEntity.setGatewayAccountId(gatewayAccountId);
        productEntity.setReferenceEnabled(false);
        when(productDao.findByGatewayAccountIdAndExternalId(gatewayAccountId, externalId)).thenReturn(Optional.of(productEntity));

        Optional<Product> productOptional = productFinder.findByGatewayAccountIdAndExternalId(gatewayAccountId, externalId);

        assertTrue(productOptional.isPresent());
        assertThat(productOptional.get().getExternalId(), is(externalId));
    }

    @Test
    public void findByGatewayAccountIdAndExternalId_shouldReturnEmpty_whenNotFound() {
        Integer gatewayAccountId = 1;
        String externalId = "1";
        when(productDao.findByGatewayAccountIdAndExternalId(gatewayAccountId, externalId)).thenReturn(Optional.empty());

        Optional<Product> productOptional = productFinder.findByGatewayAccountIdAndExternalId(gatewayAccountId, externalId);

        assertFalse(productOptional.isPresent());
    }

    @Test
    public void updatePayApiTokenByExternalId_shouldUpdateApiToken_whenFound() {
        String externalId = "1";
        String oldPayApiToken = "old Pay API token";
        String newPayApiToken = "new Pay API token";
        ProductEntity productEntity = new ProductEntity();
        productEntity.setExternalId(externalId);
        productEntity.setReferenceEnabled(false);
        productEntity.setPayApiToken(oldPayApiToken);
        when(productDao.findByExternalId(externalId)).thenReturn(Optional.of(productEntity));

        Optional<Product> productOptional = productFinder.findByExternalId(externalId);
        assertTrue(productOptional.isPresent());
        assertThat(productOptional.get().getPayApiToken(), is(oldPayApiToken));

        Optional<Product> updatedProduct = productFinder.updatePayApiTokenByExternalId(externalId, newPayApiToken);

        assertTrue(updatedProduct.isPresent());
        assertThat(updatedProduct.get().getPayApiToken(), is(newPayApiToken));
    }

    @Test
    public void updatePayApiTokenByExternalId_shouldReturnEmpty_whenNotFound() {
        String externalId = "1";
        when(productDao.findByExternalId(externalId)).thenReturn(Optional.empty());

        Optional<Product> product = productFinder.updatePayApiTokenByExternalId(externalId, "new Pay API token");

        assertFalse(product.isPresent());
    }

    @Test
    public void disableByExternalId_shouldDisableProduct_whenFound() {
        String externalId = "1";
        ProductEntity productEntity = new ProductEntity();
        productEntity.setExternalId(externalId);
        productEntity.setReferenceEnabled(false);
        when(productDao.findByExternalId(externalId)).thenReturn(Optional.of(productEntity));

        Optional<Product> productOptional = productFinder.findByExternalId(externalId);
        assertThat(productOptional.isPresent(), is(true));
        assertThat(productOptional.get().getStatus(), is(ProductStatus.ACTIVE));

        Optional<Product> disabledProduct = productFinder.disableByExternalId(externalId);

        assertThat(disabledProduct.isPresent(), is(true));
        assertThat(disabledProduct.get().getStatus(), is(ProductStatus.INACTIVE));
    }

    @Test
    public void disableByExternalId_shouldReturnEmpty_whenNotFound() {
        String externalId = "1";
        ProductEntity productEntity = new ProductEntity();
        productEntity.setExternalId(externalId);
        when(productDao.findByExternalId(externalId)).thenReturn(Optional.empty());

        Optional<Product> disabledProduct = productFinder.disableByExternalId(externalId);

        assertFalse(disabledProduct.isPresent());
    }

    @Test
    public void deleteByExternalId_shouldDeleteProduct_whenFound() {
        String externalId = "1";
        ProductEntity productEntity = new ProductEntity();
        productEntity.setExternalId(externalId);
        productEntity.setReferenceEnabled(false);
        when(productDao.findByExternalId(externalId)).thenReturn(Optional.of(productEntity));

        Optional<Product> productOptional = productFinder.findByExternalId(externalId);
        assertThat(productOptional.isPresent(), is(true));
        assertThat(productOptional.get().getStatus(), is(ProductStatus.ACTIVE));

        Boolean success = productFinder.deleteByExternalId(externalId);

        assertThat(success, is(true));
    }

    @Test
    public void deleteByExternalId_shouldReturnFalse_whenNotFound() {
        String externalId = "1";
        ProductEntity productEntity = new ProductEntity();
        productEntity.setExternalId(externalId);
        when(productDao.findByExternalId(externalId)).thenReturn(Optional.empty());

        Boolean success = productFinder.deleteByExternalId(externalId);

        assertThat(success, is(false));
    }

    @Test
    public void disableByGatewayAccountIdAndExternalId_shouldDisableProduct_whenFound() {
        Integer gatewayAccountId = 1;
        String externalId = "1";
        ProductEntity productEntity = new ProductEntity();
        productEntity.setExternalId(externalId);
        productEntity.setGatewayAccountId(gatewayAccountId);
        productEntity.setReferenceEnabled(false);
        when(productDao.findByGatewayAccountIdAndExternalId(gatewayAccountId, externalId)).thenReturn(Optional.of(productEntity));

        Optional<Product> productOptional = productFinder.findByGatewayAccountIdAndExternalId(gatewayAccountId, externalId);
        assertThat(productOptional.isPresent(), is(true));
        assertThat(productOptional.get().getStatus(), is(ProductStatus.ACTIVE));

        Optional<Product> disabledProduct = productFinder.disableByGatewayAccountIdAndExternalId(gatewayAccountId, externalId);

        assertThat(disabledProduct.isPresent(), is(true));
        assertThat(disabledProduct.get().getStatus(), is(ProductStatus.INACTIVE));
    }

    @Test
    public void deleteByGatewayAccountIdAndExternalId_shouldDeleteProduct_whenFound() {
        Integer gatewayAccountId = 1;
        String externalId = "1";
        ProductEntity productEntity = new ProductEntity();
        productEntity.setExternalId(externalId);
        productEntity.setGatewayAccountId(gatewayAccountId);
        productEntity.setReferenceEnabled(false);
        when(productDao.findByGatewayAccountIdAndExternalId(gatewayAccountId, externalId)).thenReturn(Optional.of(productEntity));

        Optional<Product> productOptional = productFinder.findByGatewayAccountIdAndExternalId(gatewayAccountId, externalId);
        assertThat(productOptional.isPresent(), is(true));
        assertThat(productOptional.get().getStatus(), is(ProductStatus.ACTIVE));

        Boolean success = productFinder.deleteByGatewayAccountIdAndExternalId(gatewayAccountId, externalId);

        assertThat(success, is(true));
    }

    @Test
    public void deleteByGatewayAccountIdAndExternalId_shouldReturnFalse_whenNotFound() {
        Integer gatewayAccountId = 1;
        String externalId = "1";
        when(productDao.findByGatewayAccountIdAndExternalId(gatewayAccountId, externalId)).thenReturn(Optional.empty());

        Boolean success = productFinder.deleteByGatewayAccountIdAndExternalId(gatewayAccountId, externalId);

        assertThat(success, is(false));
    }

    @Test
    public void disableByGatewayAccountIdAndExternalId_shouldReturnEmpty_whenNotFound() {
        Integer gatewayAccountId = 1;
        String externalId = "1";
        when(productDao.findByGatewayAccountIdAndExternalId(gatewayAccountId, externalId)).thenReturn(Optional.empty());

        Optional<Product> disabledProduct = productFinder.disableByGatewayAccountIdAndExternalId(gatewayAccountId, externalId);

        assertFalse(disabledProduct.isPresent());
    }

    @Test
    public void findByProductPath_shouldReturnProduct_whenFound() {
        String externalId = "1";
        String serviceNamePath = "service-name-path";
        String productNamePath = "product-name-path";
        ProductEntity productEntity = new ProductEntity();
        productEntity.setExternalId(externalId);
        productEntity.setServiceNamePath(serviceNamePath);
        productEntity.setProductNamePath(productNamePath);
        productEntity.setReferenceEnabled(false);

        when(productDao.findByProductPath(serviceNamePath, productNamePath)).thenReturn(Optional.of(productEntity));

        Optional<Product> productOptional = productFinder.findByProductPath(serviceNamePath, productNamePath);

        assertTrue(productOptional.isPresent());
        assertThat(productOptional.get().getServiceNamePath(), is(serviceNamePath));
        assertThat(productOptional.get().getProductNamePath(), is(productNamePath));
    }

    @Test
    public void findByProductPath_shouldReturnEmpty_whenNotFound() {
        String serviceNamePath = "service-name-path";
        String productNamePath = "product-name-path";
        when(productDao.findByProductPath(serviceNamePath, productNamePath)).thenReturn(Optional.empty());

        Optional<Product> productOptional = productFinder.findByProductPath(serviceNamePath, productNamePath);

        assertFalse(productOptional.isPresent());
    }
}
