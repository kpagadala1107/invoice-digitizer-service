package com.kp.invoice.repository;

import com.kp.invoice.model.Invoice;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface InvoiceRepository extends MongoRepository<Invoice, String> {
}
