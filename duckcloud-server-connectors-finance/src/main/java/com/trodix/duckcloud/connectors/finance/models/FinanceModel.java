package com.trodix.duckcloud.connectors.finance.models;

public class FinanceModel {

    public static final String prefix = "finance";
    private static final String separator = ":";

    private static String getName(String model) {
        return prefix + separator + model;
    }

    public static final String TYPE_ORDER = getName("order");

    public static final String TYPE_INVOICE = getName("invoice");

}
