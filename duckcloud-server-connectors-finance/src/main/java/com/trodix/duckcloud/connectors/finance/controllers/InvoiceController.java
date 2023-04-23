package com.trodix.duckcloud.connectors.finance.controllers;

import com.trodix.duckcloud.connectors.finance.annotations.ValidateInvoiceData;
import com.trodix.duckcloud.connectors.finance.services.InvoiceService;
import com.trodix.duckcloud.persistance.entities.Node;
import com.trodix.duckcloud.presentation.dto.mappers.NodeMapper;
import com.trodix.duckcloud.presentation.dto.requests.NodeRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/integration/finance/invoice")
public class InvoiceController {

    private final InvoiceService invoiceService;

    private final NodeMapper nodeMapper;

    @PostMapping("")
    @ValidateInvoiceData
    public void createInvoice(@RequestBody @Valid NodeRequest request) {
        final Node data = nodeMapper.toEntity(request);
        invoiceService.createInvoice(data);
    }

}
