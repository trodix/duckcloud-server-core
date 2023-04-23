package com.trodix.duckcloud.connectors.finance.utils;

import com.trodix.duckcloud.connectors.finance.models.FinanceModel;
import com.trodix.duckcloud.persistance.entities.Node;

public class FinanceUtils {

    public static boolean isInvoiceType(Node node) {
        return node.getType() == null || !node.getType().toString().equals(FinanceModel.TYPE_INVOICE);
    }

}
