package com.kp.invoice.service;

import com.kp.invoice.dto.InvoiceDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class OcrServiceMock {
    public InvoiceDTO extractFromImage(MultipartFile file) {
        // Mock response for now
        InvoiceDTO dto = new InvoiceDTO();
        dto.invoiceNumber = "INV-1234";
        dto.vendor = "Test Vendor";
        dto.date = "2025-08-08";
        dto.tax = 5.0;
        dto.totalAmount = 100.0;
        dto.items = List.of(
            createItem("Item A", 2, 20.0),
            createItem("Item B", 1, 50.0)
        );
        return dto;
    }

    private InvoiceDTO.Item createItem(String name, int qty, double price) {
        InvoiceDTO.Item item = new InvoiceDTO.Item();
        item.name = name;
        item.quantity = qty;
        item.unitPrice = price;
        return item;
    }
}
