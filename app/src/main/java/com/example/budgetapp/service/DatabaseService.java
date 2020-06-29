package com.example.budgetapp.service;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.budgetapp.listeners.analytics.DatabaseAnalyticsListener;
import com.example.budgetapp.listeners.analytics.FetchUserDetailsListener;
import com.example.budgetapp.listeners.database.DatabaseBudgetListener;
import com.example.budgetapp.listeners.database.DatabaseCategoryFetchListener;
import com.example.budgetapp.listeners.database.DatabaseServiceTransactionListener;
import com.example.budgetapp.listeners.database.DatabaseTranzactionAddedListener;
import com.example.budgetapp.model.AnalyticModel;
import com.example.budgetapp.model.BudgetModel;
import com.example.budgetapp.model.CategoryModel;
import com.example.budgetapp.model.TransactionModel;
import com.example.budgetapp.model.UserDetailsModel;
import com.google.common.collect.Lists;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.collect.Iterables.isEmpty;
import static java.util.Objects.isNull;

@RequiresApi(api = Build.VERSION_CODES.O)
public class DatabaseService {

    FirebaseFirestore mDb;
    FirebaseUser mCurrentUser;
    FirebaseAuth auth;

    private List<CategoryModel> categoryUserList = new ArrayList<>();
    private List<CategoryModel> categoryList = new ArrayList<>();

    private DatabaseServiceTransactionListener databaseServiceTransactionListener;

    private DatabaseCategoryFetchListener databaseCategoryFetchListener;

    private DatabaseBudgetListener databaseBudgetListener;

    private DatabaseTranzactionAddedListener databaseTranzactionAddedListener;

    private DatabaseAnalyticsListener databaseAnalyticsListener;

    private FetchUserDetailsListener fetchUserDetailsListener;

    private static DatabaseService databaseService = null;

    public static DatabaseService instance() {
        if (isNull(databaseService)) {
            databaseService = new DatabaseService();
        }
        return databaseService;
    }

    private DatabaseService() {
        this.mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        this.mDb = FirebaseFirestore.getInstance();
        this.databaseServiceTransactionListener = null;
        this.auth = FirebaseAuth.getInstance();
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

                        List<TransactionModel> transactionList = task.getResult().getDocuments()
                                .stream().map(document -> new TransactionModel((String) document.get("name"),
                                        document.getDouble("sum"),
                                        (String) document.get("category"),
                                        document.getDate("date"),
                                        document.getId(),
                                        (String) document.get("type"))).collect(Collectors.toList());

                        List<TransactionModel> filteredTranzactionList = Lists.newArrayList(transactionList);

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
                    this.categoryList.clear();
                    this.categoryList.addAll(defaultCategoryList);

                    if (!isNull(this.databaseCategoryFetchListener)) {
                        this.databaseCategoryFetchListener.onCategoriesFetched(defaultCategoryList, false);
                    }
                });
    }

    public void fetchUserCategory() {

        auth = FirebaseAuth.getInstance();
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
                    this.categoryUserList.clear();
                    this.categoryUserList.addAll(userCategoryList);
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

    public void fetchAnalytics(String category) {

        mDb.collection("users").document(mCurrentUser.getUid())
                .collection("analytics")
                .document(category)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            List<String> dates = (List<String>) task.getResult().get("dates");
                            List<Number> values = (List<Number>) task.getResult().get("values");
                            List<AnalyticModel> analyticModelList = new ArrayList<>();
                            for (int i = 0; i < dates.size(); i++) {
                                analyticModelList.add(new AnalyticModel(document.getId(),
                                        values.get(i).doubleValue(), dates.get(i)));
                            }

                            if (!isNull(this.databaseAnalyticsListener)) {
                                this.databaseAnalyticsListener.onAnalyticsFetched(analyticModelList);
                            }
                        }
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

    public void updateTranzactionCategory() {
    }

    public void updateCategoryManual(String storeName, String category) {

        //1. check if it is default
        if (!categoryList.stream().filter(categoryModel -> category.equals(categoryModel.getName())
                && categoryModel.getStores().contains(storeName)).collect(Collectors.toList()).isEmpty()) {
            return; //category and store default
        }

        if (!categoryUserList.stream().filter(categoryModel -> category.equals(categoryModel.getName())
                && categoryModel.getStores().contains(storeName)).collect(Collectors.toList()).isEmpty()) {
            return; //category not default but store exists
        }

        DocumentReference doc = mDb.collection("users").document(mCurrentUser.getUid())
                .collection("categories")
                .document(category);

        doc.get().addOnCompleteListener(task -> {
            if (task.getResult().exists()) {
                doc.update("stores", FieldValue.arrayUnion(storeName));
                return;
            } else {
                doc.set(new CategoryModel("", new ArrayList<>(Arrays.asList(storeName)),
                        category));
                return;
            }
        });

    }

    public void addAllTranzactions(List<TransactionModel> tranzactions) {
        for (TransactionModel tranzaction : tranzactions) {
            addTranzaction(tranzaction);
        }
    }

    public void addTranzaction(TransactionModel tranzaction) {

        String storeName = tranzaction.getName().toLowerCase();
        storeName = storeName.substring(0, 1).toUpperCase() + storeName;

        updateCategoryManual(storeName, tranzaction.getCategory());

        String id = mDb.collection("users")
                .document(mCurrentUser.getUid())
                .collection("tranzactions")
                .document().getId();
        tranzaction.setDocumentId(id);

        mDb.collection("users").document(mCurrentUser.getUid())
                .collection("tranzactions")
                .document(id)
                .set(tranzaction)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        if (!isNull(this.databaseTranzactionAddedListener)) {
                            this.databaseTranzactionAddedListener.onTranzactionAdded(tranzaction);
                        }
                    }
                });

    }

//    public void updateCategory(ArrayList<CategoryModel> mCategoryModelUserList, String category,
//                               String storeName) {
//        List<String> stores = new ArrayList<>();
//        //Update extracted List
//        List<CategoryModel> filteredUserCategories = mCategoryModelUserList.stream()
//                .filter(categoryModel -> category.equals(categoryModel.getName()))
//                .collect(Collectors.toList());
//
//        if (!filteredUserCategories.isEmpty()) {
//            stores.addAll(filteredUserCategories.get(0).getStores());
//        }
//
//        stores.add(storeName);
//        CategoryModel categoryAdded = new CategoryModel("", stores, category);
//        mCategoryModelUserList.add(categoryAdded);
//
//        if (!isNull(this.databaseCategoryUpdateListener)) {
//            this.databaseCategoryUpdateListener.updateCategory(mCategoryModelUserList);
//        }
//
//        mDb.collection("users").document(mCurrentUser.getUid())
//                .collection("categories")
//                .document(category)
//                .set(categoryAdded);
//    }

    public void updateName(String name) {
        mDb.collection("users").document(mCurrentUser.getUid())
                .update("name", name);
    }

    public void updatePassword(String oldPassword, String newPassword) {
        String email = auth.getCurrentUser().getEmail();
        auth.signInWithEmailAndPassword(email, oldPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        auth.getCurrentUser().updatePassword(newPassword);
                    }
                });
    }

    public void updateAge(Integer age) {
        mDb.collection("users").document(mCurrentUser.getUid())
                .update("age", age);
    }

    public void updateData(boolean value) {
        mDb.collection("users").document(mCurrentUser.getUid())
                .update("usageOfData", value);
    }

    public void deleteData() {
        String uid = auth.getCurrentUser().getUid();

        mDb.collection("users").document(uid)
                .delete();

        mDb.collection("users").document(uid);
    }

    public void deleteAccount() {
        auth.signOut();
        String uid = auth.getCurrentUser().getUid();
        auth.getCurrentUser().delete().addOnCompleteListener(task ->
        {
            if (task.isSuccessful()) {
                mDb.collection("users").document(uid).delete();
            }
        });
    }

    public List<String> getCategories() {
        Set<String> set = new LinkedHashSet<>(categoryUserList.stream().map(CategoryModel::getName)
                .collect(Collectors.toList()));
        set.addAll(categoryList.stream().map(CategoryModel::getName).collect(Collectors.toList()));
        return new ArrayList<>(set);

    }

    public void logOutUser() {
        auth.signOut();
        mCurrentUser = null;
    }

    public void refreshCurrentUser() {
        mCurrentUser = auth.getCurrentUser();
    }

    public void getUserDate() {
        mDb.collection("users").document(mCurrentUser.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        UserDetailsModel user = task.getResult().toObject(UserDetailsModel.class);

                        if (!isNull(fetchUserDetailsListener)) {
                            fetchUserDetailsListener.getUserDetails(user);
                        }
                    }
                });
    }

    public void addToAnalytic(List<AnalyticModel> list) {
        if (!isEmpty(list)) {
            for (AnalyticModel model : list) {
                mDb.collection("users").document(mCurrentUser.getUid())
                        .collection("analytics")
                        .document(model.getName())
                        .update("dates", FieldValue.arrayUnion(model.getPeriod()),
                                "values", FieldValue.arrayUnion(model.getSpent()));
            }
        }
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

    public void setDatabaseTranzactionAddedListener(DatabaseTranzactionAddedListener listener) {
        this.databaseTranzactionAddedListener = listener;
    }

    public void setDatabaseAnalyticsListener(DatabaseAnalyticsListener listener) {
        this.databaseAnalyticsListener = listener;
    }

    public void setFetchUserDetailsListener(FetchUserDetailsListener listener) {
        this.fetchUserDetailsListener = listener;
    }

    public void addCategory(String category, CategoryModel categoryAdded) {

        categoryUserList.add(categoryAdded);

        mDb.collection("users").document(mCurrentUser.getUid())
                .collection("categories")
                .document(category)
                .set(categoryAdded);
    }

    public void emptyBudget() {

        mDb.collection("users").document(mCurrentUser.getUid())
                .collection("budget")
                .get()
                .addOnCompleteListener(task -> {
                    for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                        mDb.collection("users").document(mCurrentUser.getUid()).
                                collection("budget").document(document.getId()).delete();
                    }
                });
    }

    public void upgradePremium() {
        mDb.collection("users").document(mCurrentUser.getUid())
                .update("isPremium", true);
    }
}
