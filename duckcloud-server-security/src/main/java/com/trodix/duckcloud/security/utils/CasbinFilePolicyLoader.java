package com.trodix.duckcloud.security.utils;

import org.casbin.jcasbin.main.Enforcer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CasbinFilePolicyLoader {

    public ConfigRules parseRules(String content) {
        List<String[]> standardRules = new ArrayList<>();
        List<String[]> groupingRules = new ArrayList<>();

        String[] rows = content.split("\n");
        for (String row : rows) {
            row = row.indexOf('#') > -1 ? row.substring(0, row.indexOf('#')) : row;
            Object[] columns = Arrays.stream(row.split(","))
                    .map(s -> s.trim())
                    .toArray();

            String[] columnsAsStringArray = Arrays.copyOf(columns, columns.length, String[].class);

            if (columns[0].toString().startsWith("g")) {
                groupingRules.add(columnsAsStringArray);
            } else if (columns[0].toString().startsWith("p")) {
                standardRules.add(columnsAsStringArray);
            }

        }

        return new ConfigRules(standardRules.toArray(String[][]::new), groupingRules.toArray(String[][]::new));
    }

    public record ConfigRules(
            String[][] standardRules,
            String[][]groupingRules
    ) { }

    public void persistLocalPolicy(ConfigRules configRules, Enforcer enforcer) {
        for (String[] rule : configRules.standardRules()) {
            if (!enforcer.hasPolicy(rule)) {
                enforcer.addNamedPolicy(rule[0], Arrays.stream(rule).skip(1).toArray(String[]::new));
            }
        }

        for (String[] groupingRule : configRules.groupingRules()) {
            if (!enforcer.hasGroupingPolicy(groupingRule)) {
                enforcer.addNamedGroupingPolicy(groupingRule[0], Arrays.stream(groupingRule).skip(1).toArray(String[]::new));
            }
        }
    }

}
