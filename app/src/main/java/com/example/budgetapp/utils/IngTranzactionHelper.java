package com.example.budgetapp.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IngTranzactionHelper {

    //PAYMENT_KEYWORDS (-)
    //INCOME_KEYWORDS (+)
    private static String[] PAYMENT_KEYWORD = new String[]{ "Cumparare",
                            "Transfer", "Retragere","Plata" };

    private static String[] INCOME_KEYWORD = new String[]{ "Incasare",
                            "Porteaza" };

    private static String[] SUFFIX = new String[]{"SA", "RO"};

    private static String[] ORDONATOR = new String[]{"Ordonator", "Beneficiar"};

    private static String[] DETAILS = new String[]{"Detalii"};

    private static ArrayList<String> HEADER_KEYWORD = new ArrayList<>(Arrays.asList("Data", "Detalii", "Credit", "Debit", "Balanta"));

    public static boolean isPayment(String name){
        return Arrays.stream(PAYMENT_KEYWORD).anyMatch(name::equals);
    }

    public static boolean isIncome(String name){
        return Arrays.stream(INCOME_KEYWORD).anyMatch(name::equals);
    }

    public static boolean beginsWithNumber(String name){
        return Character.isDigit(name.charAt(0));
    }

    public static boolean isHeader(String line) {

        List<String> parseLine = Arrays.asList(line.split("\\s+"));
        return parseLine.containsAll(HEADER_KEYWORD);

    }

    public static boolean endsWithSufix(String name) {
        return Arrays.stream(SUFFIX).anyMatch(name::equals);
    }

    public static boolean isOrdonator(String line) {
        return Arrays.stream(ORDONATOR).anyMatch(line::contains);
    }

    public static boolean hasDetails(String line) {
        return Arrays.stream(DETAILS).anyMatch(line::contains);
    }
}
