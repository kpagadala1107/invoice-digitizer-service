package com.kp.invoice.service;

import com.kp.invoice.dto.InvoiceDTO;
import com.kp.invoice.model.Invoice;
import com.kp.invoice.model.InvoiceItem;
import com.kp.invoice.repository.InvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final OcrService ocrService;
    private final OcrServiceMock ocrServiceMock;

    public InvoiceService(InvoiceRepository invoiceRepository, OcrService ocrService,OcrServiceMock ocrServiceMock) {
        this.invoiceRepository = invoiceRepository;
        this.ocrService = ocrService;
        this.ocrServiceMock = ocrServiceMock;
    }

    public InvoiceDTO processInvoice(MultipartFile file) {
        // InvoiceDTO dto = ocrServiceMock.extractFromImage(file);
        InvoiceDTO dto = ocrService.extractFromImage(file);
        Invoice invoice = this.saveInvoice(dto);
        return dto;
    }

    public Invoice saveInvoice(InvoiceDTO dto) {
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(dto.invoiceNumber);
        invoice.setVendor(dto.vendor);
        invoice.setDate(dto.date);
        invoice.setTax(dto.tax);
        invoice.setTotalAmount(dto.totalAmount);

        List<InvoiceItem> items = dto.items.stream().map(i -> {
            InvoiceItem item = new InvoiceItem();
            item.setName(i.name);
            item.setQuantity(i.quantity);
            item.setUnitPrice(i.unitPrice);
            return item;
        }).collect(Collectors.toList());

        invoice.setItems(items);
        return invoiceRepository.save(invoice);
    }

    public List<Invoice> getAll() {
        return invoiceRepository.findAll();
    }

    public Invoice getInvoiceById(String id) {
        return invoiceRepository.findById(String.valueOf(id)).orElseThrow();
    }

    public Invoice deleteInvoice(String id) {
        Invoice invoice = invoiceRepository.findById(String.valueOf(id)).orElseThrow();
        invoiceRepository.delete(invoice);
        return invoice;
    }
}