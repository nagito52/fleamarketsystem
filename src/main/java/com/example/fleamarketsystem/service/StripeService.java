package com.example.fleamarketsystem.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;

@Service
public class StripeService {

	public StripeService(@Value("${stripe.api.key}") String secretKey) {
		Stripe.apiKey = secretKey;
	}

	public PaymentIntent createPaymentIntent(BigDecimal amount, String currency, String description)
			throws StripeException {
		long value = "jpy".equalsIgnoreCase(currency) ? amount.longValue()
				: amount.multiply(new BigDecimal(100)).longValue();
		PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
				.setAmount(value)
				.setCurrency(currency)
				.setDescription(description)
				.setAutomaticPaymentMethods(
						PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
								.setEnabled(true)
								.build())
				.build();

		return PaymentIntent.create(params);
	}

	public PaymentIntent retrievePaymentIntent(String paymentIntentId) throws StripeException {
		return PaymentIntent.retrieve(paymentIntentId);
	}

	public void refund(String paymentIntentId) throws StripeException {
		RefundCreateParams params = RefundCreateParams.builder()
				.setPaymentIntent(paymentIntentId)
				.build();
		Refund.create(params);
	}
}