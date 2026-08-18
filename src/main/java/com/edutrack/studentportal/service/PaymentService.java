package com.edutrack.studentportal.service;

import com.edutrack.studentportal.config.RazorpayConfig;
import com.edutrack.studentportal.dto.PaymentDTO;
import com.edutrack.studentportal.exception.ResourceNotFoundException;
import com.edutrack.studentportal.model.Payment;
import com.edutrack.studentportal.repository.PaymentRepository;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.razorpay.Order;

import java.time.LocalDateTime;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ModelMapper modelMapper;
    private final RazorpayService razorpayService;
    private final RazorpayConfig.RazorpayProperties razorpayProperties;

    public PaymentService(PaymentRepository paymentRepository,
                          ModelMapper modelMapper,
                          RazorpayService razorpayService,
                          RazorpayConfig.RazorpayProperties razorpayProperties) {

        this.paymentRepository = paymentRepository;
        this.modelMapper = modelMapper;
        this.razorpayService = razorpayService;
        this.razorpayProperties = razorpayProperties;
    }

    //create Razorpay order
    public PaymentDTO createPaymentOrder(PaymentDTO paymentDTO)
            throws Exception {

        // Create Razorpay order
        Order order = razorpayService.createOrder(paymentDTO.getAmount());

        // Get Razorpay order ID
        String orderId = order.get("id");

        // Create Payment entity
        Payment payment = new Payment();

        payment.setStudentId(paymentDTO.getStudentId());
        payment.setAmount(paymentDTO.getAmount());
        payment.setOrderId(orderId);
        payment.setStatus("CREATED");

        //Save payment
        Payment savedPayment = paymentRepository.save(payment);

        //Entity -> DTO
        return modelMapper.map(savedPayment, PaymentDTO.class);
    }

    //Verify Razorpay payment
    public PaymentDTO verifyPayment(PaymentDTO paymentDTO)
    throws Exception{

        //Find Payment using Razorpay order id
        Payment payment =
                paymentRepository
                        .findByOrderId(paymentDTO.getOrderId());

        //payment order not found
        if(payment == null){

            throw new ResourceNotFoundException(
                    "payment order not found with orderid : "
                    +paymentDTO.getOrderId()
            );
        }

        //Data reqired for RazorPay signature verification
        String data =
                paymentDTO.getOrderId()
                +"|"
                +paymentDTO.getPaymentId();

        //verify Razorpay signature
        boolean isValid =
                Utils.verifySignature(
                        data,
                        paymentDTO.getSignature(),
                        razorpayProperties.getKeySecret()
                );

        //Invalid signature
        if(!isValid){

            throw new RuntimeException(
                    "Invalid razorpay payment signature"
            );
        }

        //Payment successfully verified
        payment.setPaymentId(
                paymentDTO.getPaymentId());

        payment.setSignature(
                paymentDTO.getSignature()
        );

        payment.setPaymentMethod(
                paymentDTO.getPaymentMethod()
        );

        payment.setPaymentDate(
                LocalDateTime.now()
        );

        //sav updated payment
        Payment updatedPayment =
                paymentRepository.save(payment);

        //convert entity to DTO
        return modelMapper.map(updatedPayment, PaymentDTO.class);
    }
}