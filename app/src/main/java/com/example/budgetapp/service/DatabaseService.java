package com.example.budgetapp.service;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.budgetapp.listeners.database.DatabaseBudgetListener;
import com.example.budgetapp.listeners.database.DatabaseCategoryFetchListener;
import com.example.budgetapp.listeners.database.DatabaseServiceTransactionListener;
import com.example.budgetapp.model.BudgetModel;
import com.example.budgetapp.model.CategoryModel;
import com.example.budgetapp.model.TranzactionModel;
import com.google.common.collect.Lists;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@RequiresApi(api = Build.VERSION_CODES.O)
public class DatabaseService {

    FirebaseFirestore mDb;
    FirebaseUser mCurrentUser;

    private DatabaseServiceTransactionListener databaseServiceTransactionListener;

    private DatabaseCategoryFetchListener databaseCategoryFetchListener;

    private DatabaseBudgetListener databaseBudgetListener;

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
                                document.getDate("date"),
                                document.getId(),
                                (String) document.get("type"))).collect(Collectors.toList());

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

    public void fetchBudget() {
        mDb.collection("users").document(mCurrentUser.getUid())
                .collection("budget")
                .get()
                .addOnCompleteListener(task -> {
                    List<BudgetModel> exapandableList = task.getResult()
                            .getDocuments()
                            .stream()
                            .map(document -> new BudgetModel(document.getDouble("budget"),
                                    (String) document.get("name"))).collect(Collectors.toList());

                    if (!isNull(this.databaseBudgetListener)) {
                        this.databaseBudgetListener.onBudgetFetch(exapandableList);
                    }
                });
    }

    private Date getCustomDate(String date) {
        String[] values = date.split("/");
        int month = Integer.parseInt(values[0]);
        int day = Integer.parseInt(values[1]);
        int year = Integer.parseInt(values[2]);

        return Date.from(LocalDate.of(year, month, day).atStartOfDay()
                .atZone(ZoneId.systemDefault()).toInstant());
    }

    public void updateBudget(String category, Double budget) {

        mDb.collection("users").document(mCurrentUser.getUid())
                .collection("budget")
                .document(category)
                .set(new BudgetModel(budget, category));

    }


    // Assign the listener implementing events interface that will receive the events
    public void setDatabaseServiceTransactionListener(DatabaseServiceTransactionListener listener) {
        this.databaseServiceTransactionListener = listener;
    }

    public void setDatabaseBudgetListener(DatabaseBudgetListener listener) {
        this.databaseBudgetListener = listener;
    }

    public void setDatabaseCategoryFetchListener(DatabaseCategoryFetchListener databaseCategoryFetchListener) {
        this.databaseCategoryFetchListener = databaseCategoryFetchListener;
    }
}
