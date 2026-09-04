package com.recovery.revenuerecovery.controller;

import com.recovery.revenuerecovery.model.PaymentRecord;
import com.recovery.revenuerecovery.repository.PaymentRecordRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin
public class PaymentRecordController {

    private final PaymentRecordRepository paymentRecordRepository;

    public PaymentRecordController(
            PaymentRecordRepository paymentRecordRepository) {

        this.paymentRecordRepository = paymentRecordRepository;
    }

    @GetMapping
    public List<PaymentRecord> getAllPayments() {

        return paymentRecordRepository.findAll();
    }
}