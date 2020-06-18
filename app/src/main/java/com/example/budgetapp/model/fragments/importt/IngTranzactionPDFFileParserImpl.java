package com.example.budgetapp.model.fragments.importt;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.budgetapp.interfaces.TranzactionPDFFileParser;
import com.example.budgetapp.listeners.database.DatabaseCategoryFetchListener;
import com.example.budgetapp.model.CategoryModel;
import com.example.budgetapp.model.TransactionModel;
import com.example.budgetapp.service.DatabaseService;
import com.example.budgetapp.utils.IngTranzactionHelper;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;

import org.apache.commons.lang3.StringUtils;


import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RequiresApi(api = Build.VERSION_CODES.O)
public class IngTranzactionPDFFileParserImpl implements TranzactionPDFFileParser {

    private static final int PAYMENT_COLUMN = 4;
    List<TransactionModel> transactionModelList;

    DatabaseService mDatabaseService;

    List<CategoryModel> userCategoryModelList = new ArrayList<>();
    List<CategoryModel> defaultCategoryModelList = new ArrayList<>();

    DatabaseCategoryFetchListener databaseCategoryFetchListener;

    public IngTranzactionPDFFileParserImpl() {
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

    private void skipHeaderLines(List<String> lines){
        Iterator<String> it = lines.iterator();
        while(it.hasNext()){
            String line = it.next();

            if (IngTranzactionHelper.isHeader(line)){
                break;
            }
            else {
                it.remove();
            }
        }
        it.remove();
    }

    private void skipFooterLines(List<String> lines){
        //delete last 4 lines which are footer
        for (int i = 0; i <  4; i++) {
            lines.remove(lines.size() - 1);
        }
    }

    private List<String> fetchPDFLines(File file){
        List<String> tranzactionLines = new ArrayList<>();

        try {
            PDDocument document = PDDocument.load(file);
            PDFTextStripper pdfTextStripper = new PDFTextStripper();
            pdfTextStripper.setSortByPosition(true);

            for (int page = 1; page <= document.getNumberOfPages(); page++){
                pdfTextStripper.setStartPage(page);
                pdfTextStripper.setEndPage(page);

                String currentPage =  pdfTextStripper.getText(document);
                List<String> lines = new ArrayList<>(Arrays.asList(currentPage.split("\r\n|\r|\n")));

                skipHeaderLines(lines);
                skipFooterLines(lines);

                tranzactionLines.addAll(lines);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tranzactionLines;
    }

    public List<TransactionModel> parseFile(File file) {

        List<String> tranzactionListLine = fetchPDFLines(file);
        List<String> tranzaction = new ArrayList<>();

        for (String line : tranzactionListLine) {

            if(IngTranzactionHelper.beginsWithNumber(line) && tranzaction.size() != 0){
                try {
                    transactionModelList.add(getTranzactionModel(tranzaction));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                tranzaction.clear();
            }
            tranzaction.add(line);
        }
        try {
            transactionModelList.add(getTranzactionModel(tranzaction));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return transactionModelList;
    }

    private TransactionModel getTranzactionModel(List<String> tranzaction) throws ParseException {
        TransactionModel transactionModel = new TransactionModel("", 0.0, "", null, null, "");

        String firstLine = tranzaction.get(0);
        String[] splitFirstLine = firstLine.split("\\s+");

        //get type for tranzaction model
        String type = getTypeFromLines(splitFirstLine);
        //get sum for tranzaction model
        Double sum = getSumFromLines(splitFirstLine);

        switch(getTranzactionDetailsType(splitFirstLine))
        {
            case ("Cumparare POS") :
            case ("Retragere numerar") :
                transactionModel =  getTranzactionDetailsBuyingPOS(tranzaction, sum, type);
                break;

            case ("Incasare") :
            case ("Transfer Home'Bank") : {
                transactionModel = getTranzactionDetailsTransfer(tranzaction, sum, type);
                break;
            }
            case ("Incasare Porteaza Banii") :
                transactionModel = getTranzactionDetailsIncoming(tranzaction, sum, type);
                break;
                
            case ("Plata debit direct") :
                transactionModel = getTranzactionDetailsDirectDebit(tranzaction, sum, type);
                break;
            default: break;
        }

        return transactionModel;

    }

    //case Plata debit direct
    private TransactionModel getTranzactionDetailsDirectDebit(List<String> tranzaction, Double sum, String type) {
        String name;
        String[] secondLine = tranzaction.get(1).split(":");

        name = secondLine[1].trim();
        name = name.concat("-" + getDetails(tranzaction));

        String[] firstLine = tranzaction.get(0).split("\\s+");
        Date date = getTranzactionDefaultDate(firstLine);

        String category = getCategoryModel(name);
        return new TransactionModel(name, sum, category, date, "", type);
    }

    private String getCategoryModel(String name) {
        if (userCategoryModelList.stream().anyMatch(store -> store.getStores().contains(name))){
            return userCategoryModelList.stream().map(CategoryModel::getStores).filter(store -> store.contains(name)).collect(Collectors.toList()).get(0).toString();
        }
        if (defaultCategoryModelList.stream().anyMatch(store -> store.getStores().contains(name))){
            return defaultCategoryModelList.stream().map(CategoryModel::getStores).filter(store -> store.contains(name)).collect(Collectors.toList()).get(0).toString();
        }
        else {
            return "UNKNOWN";
        }
    }

    //case Incasare
    private TransactionModel getTranzactionDetailsIncoming(List<String> tranzaction, Double sum, String type) {
        String[] secondLine = tranzaction.get(1).split("\\s+");

        String name = getOrdonatorName(tranzaction);

        String[] thirdLine = tranzaction.get(3).split("\\s+");

        Date date = getTranzactionDate(thirdLine);

        return new TransactionModel(name, sum, "", date, "", type);
    }

    //geT Ordonator name for incasare

    private String getOrdonatorName(List<String> tranzaction) {
        List<String> name = null;

        for (String line : tranzaction){
            if (IngTranzactionHelper.isOrdonator(line)){
                name = new ArrayList<>(Arrays.asList(line.split("\\s+")));
                name.remove(0);
                break;
            }
        }
        return String.join(" ", name);
    }

    //case Transfer Home'Bank
    private TransactionModel getTranzactionDetailsTransfer(List<String> tranzaction, Double sum, String type) {

        String[] firstLine = tranzaction.get(0).split("\\s+");
        Date date = getTranzactionDefaultDate(firstLine);

        //get transfered money person
        //get details if are any

        String name = getOrdonatorName(tranzaction);

        name = name.concat(getDetails(tranzaction));

        return new TransactionModel(name, sum, "", date, "", type);
    }

    //get details row
    private String getDetails(List<String> tranzaction) {
        String details = new String();
        for (String line : tranzaction){
            if (IngTranzactionHelper.hasDetails(line)){
                String[] detailLine = line.split(":");
                details =  detailLine[1].trim();
                break;
            }
        }
        return details.length() != 0 ? "-" + details : "";
    }

    //get the date located at the beggining of the tranzaction
    private Date getTranzactionDefaultDate(String[] firstLine) {
        //get default date placed at the beggining of the first line
        String dateString = firstLine[0] + "-" + firstLine[1] + "-" + firstLine[2];

        Date date = new Date();

        SimpleDateFormat formatter2=new SimpleDateFormat("dd-MMMM-yyyy",  new Locale("ro"));
        try {
            date = formatter2.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    //case Cumparare POS
    private TransactionModel getTranzactionDetailsBuyingPOS(List<String> tranzaction, Double sum, String type) {
        //Name is on second line, sum and date on third line

        //second line => get name

        String[] secondLine = tranzaction.get(2).split("\\s+");
        String name = getTranzactionName(secondLine);

        //third line => get date

        String[] thirdLine = tranzaction.get(3).split("\\s+");

        Date date = getTranzactionDate(thirdLine);

        return new TransactionModel(name, sum, "", date, "", type);

    }

    //get the date located in the tranzaction details list
    private Date getTranzactionDate(String[] line) {
        SimpleDateFormat formatter2=new SimpleDateFormat("dd-MM-yyyy", new Locale("ro"));

        Date date = new Date();

        try {
            date = formatter2.parse(line[1]);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    //get the name of the tranzaction
    private String getTranzactionName(String[] line) {
        List<String> name = new ArrayList<>();

        for (int i = 1; i < line.length; i++){
            if (IngTranzactionHelper.endsWithSufix(line[i])){
                break;
            }
            name.add(line[i]);
        }

        return StringUtils.join(name, " ");
    }

    //get type of tranzaction (eg. "Cumparare POS",  "Retragere Numerar", etc.)
    private String getTranzactionDetailsType(String[] line){
        List<String> tranzactionType = new ArrayList<>();
        for (int i = 3; i < line.length; i++) {
            if (IngTranzactionHelper.beginsWithNumber(line[i])){
                return StringUtils.join(tranzactionType, " ");
            }
            tranzactionType.add(line[i]);
        }
        return null;
    }

    //get type : {Payment, Income}
    private String getTypeFromLines(String[] line) {
        return null;
    }

    //get the value of the tranzaction
    private Double getSumFromLines(String[] line) {
        for (int i = 3; i < line.length; i++){
            if (IngTranzactionHelper.beginsWithNumber(line[i])){
                return ((line[i].split(",")[0].length() >= 4) ? Double.parseDouble(line[i].split(",")[0].replace(",", ".")) :
                        Double.parseDouble(line[i].replace(",", ".")));
            }
        }
        return null;
    }
}
