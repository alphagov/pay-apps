package uk.gov.pay.products.service;

import com.google.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.products.client.publicapi.PaymentRequest;
import uk.gov.pay.products.client.publicapi.PaymentResponse;
import uk.gov.pay.products.client.publicapi.PublicApiResponseErrorException;
import uk.gov.pay.products.client.publicapi.PublicApiRestClient;
import uk.gov.pay.products.model.Payment;
import uk.gov.pay.products.persistence.dao.PaymentDao;
import uk.gov.pay.products.persistence.dao.ProductDao;
import uk.gov.pay.products.persistence.entity.PaymentEntity;
import uk.gov.pay.products.persistence.entity.ProductEntity;
import uk.gov.pay.products.service.transaction.NonTransactionalOperation;
import uk.gov.pay.products.service.transaction.TransactionContext;
import uk.gov.pay.products.service.transaction.TransactionFlow;
import uk.gov.pay.products.service.transaction.TransactionalOperation;
import uk.gov.pay.products.util.PaymentStatus;

import javax.inject.Inject;

import static uk.gov.pay.products.util.RandomIdGenerator.randomUuid;

public class PaymentCreator {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Provider<TransactionFlow> transactionFlowProvider;
    private final ProductDao productDao;
    private final PaymentDao paymentDao;
    private final PublicApiRestClient publicApiRestClient;
    private final LinksDecorator linksDecorator;


    @Inject
    public PaymentCreator(Provider<TransactionFlow> transactionFlowProvider, ProductDao productDao, PaymentDao paymentDao,
                          PublicApiRestClient publicApiRestClient, LinksDecorator linksDecorator) {
        this.transactionFlowProvider = transactionFlowProvider;
        this.productDao = productDao;
        this.paymentDao = paymentDao;
        this.publicApiRestClient = publicApiRestClient;
        this.linksDecorator = linksDecorator;
    }

    public Payment doCreate(Integer productId) {
        PaymentEntity paymentEntity = transactionFlowProvider.get()
                .executeNext(beforePaymentCreation(productId))
                .executeNext(paymentCreation())
                .executeNext(afterPaymentCreation())
                .complete().get(PaymentEntity.class);

        if (paymentEntity.getStatus() == PaymentStatus.ERROR) {
            throw new PaymentCreatorDownstreamException(paymentEntity.getProductEntity().getId());
        }
        return linksDecorator.decorate(paymentEntity.toPayment());
    }

    private TransactionalOperation<TransactionContext, PaymentEntity> beforePaymentCreation(Integer productId) {
        return context -> {
            logger.info("Creating a new payment for productId {}", productId);
            ProductEntity productEntity = productDao.findById(productId)
                    .orElseThrow(() -> new PaymentCreatorNotFoundException(productId));

            PaymentEntity paymentEntity = new PaymentEntity();
            paymentEntity.setExternalId(randomUuid());
            paymentEntity.setProductEntity(productEntity);
            paymentEntity.setStatus(PaymentStatus.CREATED);
            paymentDao.persist(paymentEntity);

            return paymentEntity;
        };
    }

    private NonTransactionalOperation<TransactionContext, PaymentEntity> paymentCreation() {
        return context -> {
            PaymentEntity paymentEntity = context.get(PaymentEntity.class);
            ProductEntity productEntity = paymentEntity.getProductEntity();
            PaymentRequest paymentRequest = new PaymentRequest(
                    productEntity.getPrice(),
                    productEntity.getExternalId(),
                    productEntity.getDescription(),
                    productEntity.getReturnUrl());

            try {
                PaymentResponse paymentResponse = publicApiRestClient.createPayment(paymentRequest);

                paymentEntity.setGovukPaymentId(paymentResponse.getPaymentId());
                paymentEntity.setNextUrl(getNextUrl(paymentResponse));
                paymentEntity.setStatus(PaymentStatus.SUCCESS);
                logger.info("Payment creation for productId {} successful {}", paymentEntity.getProductEntity().getId(), paymentEntity);
            } catch (PublicApiResponseErrorException e) {
                logger.error("Payment creation for productId {} failed {}", paymentEntity.getProductEntity().getId(), e);
                paymentEntity.setStatus(PaymentStatus.ERROR);
            }

            return paymentEntity;
        };
    }

    private TransactionalOperation<TransactionContext, PaymentEntity> afterPaymentCreation() {
        return context -> {
            PaymentEntity paymentEntity = context.get(PaymentEntity.class);
            paymentDao.merge(paymentEntity);

            logger.info("Payment creation for productId {} completed {}", paymentEntity.getProductEntity().getId());
            return paymentEntity;
        };
    }

    private String getNextUrl(PaymentResponse paymentResponse) {
        if ((paymentResponse.getLinks() != null) &&
                (paymentResponse.getLinks().getNextUrl() != null)) {
            return paymentResponse.getLinks().getNextUrl().getHref();
        }
        return "";
    }
}