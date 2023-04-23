package com.trodix.duckcloud.connectors.finance.services;

import com.trodix.duckcloud.persistance.entities.Node;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {

    public void createInvoice(Node node) {
        log.debug("Creating invoice from node: {}", node);
    }

}
