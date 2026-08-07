package com.edutrack.studentportal.repository;

import com.edutrack.studentportal.model.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PaymentRepository extends MongoRepository<Payment, String> {
}
