package com.example.budgetapp.home;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.budgetapp.listeners.database.DatabaseCategoryFetchListener;
import com.example.budgetapp.listeners.database.DatabaseServiceTransactionListener;
import com.google.common.collect.Lists;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import model.CategoryModel;
import model.TranzactionModel;

import static java.util.Objects.isNull;


public class DatabaseService {

    FirebaseFirestore mDb;
    FirebaseUser mCurrentUser;

    private DatabaseServiceTransactionListener databaseServiceTransactionListener;

    private DatabaseCategoryFetchListener databaseCategoryFetchListener;

    private static DatabaseService databaseService = null;

    public static DatabaseService instance() {
        if (isNull(databaseService)) {
            return new DatabaseService();
        }

        return databaseService;
    }

    private DatabaseService() {
        this.mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        this.mDb = FirebaseFirestore.getInstance();
        this.databaseServiceTransactionListener = null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void fetchTransaction(String startDateString, String endDateString) {

        Date startDate = getCustomDate(startDateString);
        Date endDate = getCustomDate(endDateString);

        mDb.collection("users").document(mCurrentUser.getUid())
                .collection("tranzactions")
                .whereLessThanOrEqualTo("date", endDate)
                .whereGreaterThanOrEqualTo("date", startDate)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        List<TranzactionModel> transactionList = task.getResult().getDocuments().stream().map(document -> new TranzactionModel((String) document.get("name"),
                                document.getDouble("sum"),
                                (String) document.get("category"),
                                document.getDate("date"))).collect(Collectors.toList());

                        List<TranzactionModel> filteredTranzactionList = Lists.newArrayList(transactionList);

                        if (!isNull(this.databaseServiceTransactionListener)) {
                            this.databaseServiceTransactionListener.onObjectReady(transactionList, filteredTranzactionList);
                        }

                    }
                });
    }

    public void fetchDefaultCategories() {

        mDb.collection("categories")
                .get()
                .addOnCompleteListener(task -> {
                    List<CategoryModel> defaultCategoryList = task.getResult()
                            .getDocuments()
                            .stream()
                            .map(document -> new CategoryModel((String) document.get("icon"),
                                    (List<String>) document.get("stores"),
                                    document.getId())).collect(Collectors.toList());

                    if (!isNull(this.databaseCategoryFetchListener)) {
                        this.databaseCategoryFetchListener.onCategoriesFetched(defaultCategoryList, false);
                    }
                });
    }

    public void fetchUserCategory() {

        mDb.collection("users").document(mCurrentUser.getUid())
                .collection("categories")
                .get()
                .addOnCompleteListener(task -> {
                    List<CategoryModel> userCategoryList = task.getResult()
                            .getDocuments()
                            .stream()
                            .map(document -> new CategoryModel((String) document.get("icon"),
                                    (List<String>) document.get("stores"),
                                    document.getId())).collect(Collectors.toList());

                    if (!isNull(this.databaseCategoryFetchListener)) {
                        this.databaseCategoryFetchListener.onCategoriesFetched(userCategoryList, true);
                    }
                });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Date getCustomDate(String date) {
        String[] values = date.split("/");
        int month = Integer.parseInt(values[0]);
        int day = Integer.parseInt(values[1]);
        int year = Integer.parseInt(values[2]);

        return Date.from(LocalDate.of(year, month, day).atStartOfDay()
                .atZone(ZoneId.systemDefault()).toInstant());
    }

//    Setters & Getters


    // Assign the listener implementing events interface that will receive the events
    public void setDatabaseServiceTransactionListener(DatabaseServiceTransactionListener listener) {
        this.databaseServiceTransactionListener = listener;
    }

    public DatabaseCategoryFetchListener getDatabaseCategoryFetchListener() {
        return databaseCategoryFetchListener;
    }

    public void setDatabaseCategoryFetchListener(DatabaseCategoryFetchListener databaseCategoryFetchListener) {
        this.databaseCategoryFetchListener = databaseCategoryFetchListener;
    }
}
