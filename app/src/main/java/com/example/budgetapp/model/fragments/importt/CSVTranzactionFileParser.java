package com.example.budgetapp.model.fragments.importt;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.budgetapp.interfaces.TranzactionCSVFileParser;
import com.example.budgetapp.model.CategoryModel;
import com.example.budgetapp.model.TransactionModel;
import com.example.budgetapp.service.DatabaseService;
import com.example.budgetapp.utils.IngTranzactionHelper;
import com.opencsv.CSVReader;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.text.TextUtils.isEmpty;

@RequiresApi(api = Build.VERSION_CODES.O)
public class CSVTranzactionFileParser implements TranzactionCSVFileParser {

    private static final int DATE_COLUMN = 0;
    private static final int DETAIL_COLUMN = 3;
    private static final int PAYMENT_COLUMN = 4;
    private static final int INCOME_COLUMN = 6;

    private static final String PAYMENT = "Payment";
    private static final String INCOME = "Income";

    DatabaseService mDatabaseService;

    List<TransactionModel> transactionModelList;
    List<CategoryModel> userCategoryModelList = new ArrayList<>();
    List<CategoryModel> defaultCategoryModelList = new ArrayList<>();

    public CSVTranzactionFileParser() {

        transactionModelList = new ArrayList<>();
        mDatabaseService = DatabaseService.instance();

        mDatabaseService.fetchDefaultCategories();

        mDatabaseService.fetchUserCategory();

        this.mDatabaseService.setDatabaseCategoryFetchListener((categoriesList, isUserCustomCategories) -> {

            if (isUserCustomCategories) {
                this.userCategoryModelList.clear();
                this.userCategoryModelList.addAll(categoriesList);
            } else {
                this.defaultCategoryModelList.clear();
                this.defaultCategoryModelList.addAll(categoriesList);
            }
        });
    }

    @Override
    public void parseFile(InputStream inputStream) {

        List<String[]> lines = new ArrayList<>();
        try {
            CSVReader reader = new CSVReader(new InputStreamReader(inputStream));
            for (; ; ) {
                String[] current_line = reader.readNext();
                if (current_line != null) {
                    lines.add(current_line);
                } else {
                    break;
                }
            }
            mDatabaseService.addAllTranzactions(parseTranzactions(lines));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<TransactionModel> parseTranzactions (List<String[]> lines) {

        lines.remove(0);

        List<String[]> currentLines = new ArrayList<>();
        List<TransactionModel> tranzactions = new ArrayList<>();

        for (String[] line : lines) {

            if (!line[0].isEmpty() && !IngTranzactionHelper.beginsWithNumber(line[0])) {
                continue;
            }
            if (!line[0].isEmpty() && currentLines.size() != 0) {
                tranzactions.add(getTranzaction(currentLines));
                currentLines.clear();
            }
            currentLines.add(line);
        }
        tranzactions.add(getTranzaction(currentLines));

        return tranzactions;
    }

    private TransactionModel getTranzaction(List<String[]> lines){
        TransactionModel transactionModel;

        //get fields for tranzaction model

        //lines => contains all the required one for a tranzaction
        Date date = getTranzactionDate(lines.get(0)[DATE_COLUMN]);

        String type = getTypeFromLines(lines.get(0));
        Double sum = getSumFromLines(lines.get(0), type);

        //name => category
        String name = getFullName(lines);

        String category = getCategoryModel(name);

        transactionModel = new TransactionModel(name, sum, category, date, "", type);
        System.out.println("Tranzaction" + name + " " + sum + " " + category + " " + date + " " + type);
        return transactionModel;
    }

    private String getCategoryModel(String name) {
//        userCategoryModelList.stream().map(CategoryModel::getStores).forEach(store -> name.contains(store));
//        if (userCategoryModelList.stream().anyMatch(store -> name.contains(store.getStores()))){
//            return userCategoryModelList.stream().map(CategoryModel::getStores).filter(store -> store.contains(name)).collect(Collectors.toList()).get(0).toString();
//        }
//        if (defaultCategoryModelList.stream().anyMatch(store -> store.getStores().contains(name))){
//            return defaultCategoryModelList.stream().map(CategoryModel::getStores).filter(store -> store.contains(name)).collect(Collectors.toList()).get(0).toString();
//        }
//        else {
            return "UNKNOWN";
        //}
    }

    //get details row
    private String getExtras(String[] tranzaction) {

        String[] detailLine = {};
        if (tranzaction[DETAIL_COLUMN].contains(":")) {
            detailLine = tranzaction[DETAIL_COLUMN].split(":");
        }

        String details =  detailLine.length == 0 ? tranzaction[DETAIL_COLUMN] : detailLine[1].trim();

        return details.length() != 0 ? details : "";
    }

    private String getFullName (List<String[]> lines) {
        List<String> line = new ArrayList<>();
        String name = new String();
        String result;

        for (int i = 1; i < lines.size(); i++) {
            if (lines.get(i)[DETAIL_COLUMN].contains("Terminal")) {
                name = " " +  getTerminal(lines.get(i));
            }

            if (lines.get(i)[DETAIL_COLUMN].contains("Ordonator") ||
                    lines.get(i)[DETAIL_COLUMN].contains("Beneficiar") ||
                    lines.get(i)[DETAIL_COLUMN].contains("Detalii")) {

                result = name.concat(" " + getExtras(lines.get(i)));

                name = result;
            }
        }
    return name.substring(1);
    }

    //get the name of the tranzaction
    private String getTerminal(String[] line) {

        List<String> name = new ArrayList<>();

        String[] aux = line[DETAIL_COLUMN].split("\\s+");

        for (String string : aux) {
            if (IngTranzactionHelper.endsWithSufix(string)){
                break;
            }
            name.add(string);
        }

        name.remove(0);

        return StringUtils.join(name, " ");
    }

    //get type : {Payment, Income}
    private String getTypeFromLines(String[] line) {
        if (!isEmpty(line[PAYMENT_COLUMN])) {
            return PAYMENT;
        }
        if (!isEmpty(line[INCOME_COLUMN])) {
            return INCOME;
        }
        return null;
    }

    //get the value of the tranzaction
    private Double getSumFromLines(String[] line, String type) {
        int column = type.equals("Income") ? INCOME_COLUMN : PAYMENT_COLUMN;

        String value;

        if (line[column].length() > 6) {
            value = line[column].replace(".", "");
        }
        else {
            value = line[column];
        }

        return !value.isEmpty() ? Double.parseDouble(value.replace(",", ".")) : 0.0;

    }

    private Date getTranzactionDate(String line) {
        //get default date placed at the beggining of the first line
        String[] parsedLines = line.split("\\s+");
        if (parsedLines.length == 1) {
            System.out.println("");

        }

        String dateString = parsedLines[0] + "-" + parsedLines[1] + "-" + parsedLines[2];

        Date date = new Date();

        SimpleDateFormat formatter2=new SimpleDateFormat("dd-MMMM-yyyy",  new Locale("ro"));
        try {
            date = formatter2.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
}
