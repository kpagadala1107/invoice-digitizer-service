package com.kp.invoice.controller;

import com.kp.invoice.dto.InvoiceDTO;
import com.kp.invoice.model.Invoice;
import com.kp.invoice.service.InvoiceService;
import com.kp.invoice.service.PdfExtractorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    private final PdfExtractorService pdfExtractorService;

    public InvoiceController(InvoiceService invoiceService, PdfExtractorService pdfExtractorService) {
        this.pdfExtractorService = pdfExtractorService;
        this.invoiceService = invoiceService;
    }

    @PostMapping("/upload")
    public InvoiceDTO upload(@RequestParam("file") MultipartFile file) {
        return invoiceService.processInvoice(file);
    }

    @PostMapping("/content")
    public String uploadPdf(@RequestParam("file") MultipartFile file) {
        return pdfExtractorService.extractTextFromPdf(file);
    }

    @PostMapping("/save")
    public ResponseEntity<Invoice> save(@RequestBody InvoiceDTO dto) {
        return ResponseEntity.ok(invoiceService.saveInvoice(dto));
    }

    @GetMapping
    public List<Invoice> getAll() {
        return invoiceService.getAll();
    }

    @GetMapping("/{id}")
    public Invoice getInvoiceById(@PathVariable String id) {
        return invoiceService.getInvoiceById(id);
    }

    @PutMapping("/{id}")
    public Invoice updateInvoice(@PathVariable String id) {
        return invoiceService.deleteInvoice(id);
    }

    @DeleteMapping("/{id}")
    public Invoice deleteInvoice(@PathVariable String id) {
        return invoiceService.deleteInvoice(id);
    }
}