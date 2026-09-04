package com.recovery.revenuerecovery.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private final RazorpayClient razorpayClient;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    public PaymentService(RazorpayClient razorpayClient) {
        this.razorpayClient = razorpayClient;
    }

    public String createOrder(long amount, String currency) throws Exception {

        JSONObject orderRequest = new JSONObject();

        orderRequest.put("amount", amount);
        orderRequest.put("currency", currency);
        orderRequest.put(
                "receipt",
                "receipt_" + System.currentTimeMillis()
        );

        Order order = razorpayClient.orders.create(orderRequest);

        return order.toString();
    }

    public boolean verifyPayment(
            String orderId,
            String paymentId,
            String signature) {

        try {

            JSONObject attributes = new JSONObject();

            attributes.put(
                    "razorpay_order_id",
                    orderId
            );

            attributes.put(
                    "razorpay_payment_id",
                    paymentId
            );

            attributes.put(
                    "razorpay_signature",
                    signature
            );

            return Utils.verifyPaymentSignature(
                    attributes,
                    razorpayKeySecret
            );

        } catch (Exception e) {

            System.out.println(
                    "Payment verification error: "
                            + e.getMessage()
            );

            return false;
        }
    }
}