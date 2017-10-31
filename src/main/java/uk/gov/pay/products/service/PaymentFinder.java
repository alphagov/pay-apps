package uk.gov.pay.products.service;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import uk.gov.pay.products.model.Payment;
import uk.gov.pay.products.persistence.dao.PaymentDao;
import uk.gov.pay.products.persistence.entity.PaymentEntity;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PaymentFinder {

    private final PaymentDao paymentDao;
    private final LinksDecorator linksDecorator;

    @Inject
    public PaymentFinder(PaymentDao paymentDao, LinksDecorator linksDecorator){
        this.paymentDao = paymentDao;
        this.linksDecorator = linksDecorator;
    }

    @Transactional
    public Optional<Payment> findByExternalId(String paymentExternalId) {
        return paymentDao.findByExternalId(paymentExternalId)
                .map(paymentEntity -> linksDecorator.decorate(paymentEntity.toPayment()));
    }

    @Transactional
    public List<Payment> findByProductId(Integer productId){
        return paymentDao.findByProductId(productId)
                .stream()
                .map(PaymentEntity::toPayment)
                .collect(Collectors.toList())
                .stream()
                .map(payment -> linksDecorator.decorate(payment))
                .collect(Collectors.toList());
    }
}