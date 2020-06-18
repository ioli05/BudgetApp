package com.example.budgetapp.adapter;

import android.app.Activity;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.example.budgetapp.R;
import com.example.budgetapp.model.CategoryModel;
import com.example.budgetapp.model.SearchModel;
import com.example.budgetapp.model.TransactionModel;
import com.example.budgetapp.service.DatabaseService;
import com.example.budgetapp.utils.ContactSearchDialogCompat;
import com.example.budgetapp.utils.IconDrawable;
import com.github.mikephil.charting.charts.PieChart;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiresApi(api = Build.VERSION_CODES.O)
public class TranzactionAdapter extends ArrayAdapter<TransactionModel> {

    Activity context;

    List<Double> mTranzactionSum;
    List<String> mTranzactionName;
    List<String> mTranzactionCategory;

    ArrayList<TransactionModel> mTransactionModelList;
    ArrayList<CategoryModel> mCategoryModelList;
    ArrayList<CategoryModel> mCategoryModelUserList;

    FirebaseFirestore mDb;
    FirebaseUser mCurrentUser;

    DatabaseService databaseService;

    PieChart mPieChart;

    public TranzactionAdapter(Activity context, ArrayList<TransactionModel> tranzactionList, FirebaseFirestore mDb,
                              FirebaseUser mCurrentUser, PieChart mPieChart) {

        super(context, R.layout.activity_tranzaction, tranzactionList);

        this.context = context;
        this.mTransactionModelList = tranzactionList;
        this.mDb = mDb;
        this.mCurrentUser = mCurrentUser;
        this.mPieChart = mPieChart;

        databaseService = DatabaseService.instance();

    }

    public View getView(int position, View view, ViewGroup parent) {

        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.activity_tranzaction, null, true);

        TextView TranzactionSum = rowView.findViewById(R.id.TranzactionSum);
        TextView TranzactionName = rowView.findViewById(R.id.TranzactionName);
        Button TranzactionCategory = rowView.findViewById(R.id.TranzactionCategory);

        TranzactionCategory.setOnClickListener(v -> {

            List<String> categoryUserList;
            categoryUserList = mCategoryModelUserList.stream().map(CategoryModel::getName).collect(Collectors.toList());
            categoryUserList.addAll(mCategoryModelList.stream().map(CategoryModel::getName).collect(Collectors.toList()));

            showPopupEventTest(position, (ArrayList<String>) categoryUserList.stream().distinct().collect(Collectors.toList()));

        });

        mTranzactionSum = new ArrayList<>();
        mTranzactionName = new ArrayList<>();
        mTranzactionCategory = new ArrayList<>();

        mTranzactionSum.addAll(mTransactionModelList.stream().map(TransactionModel::getSum).collect(Collectors.toList()));
        mTranzactionName.addAll(mTransactionModelList.stream().map(TransactionModel::getName).collect(Collectors.toList()));
        mTranzactionCategory.addAll(mTransactionModelList.stream().map(TransactionModel::getCategory).collect(Collectors.toList()));

        TranzactionSum.setText(mTranzactionSum.get(position).toString());
        TranzactionName.setText(mTranzactionName.get(position));
        TranzactionCategory.setText(mTranzactionCategory.get(position));

        return rowView;

    }

    private void showPopupEventTest(int tranzactionPosition, ArrayList<String> userList) {

        ArrayList<SearchModel> sampleSearch = createSampleSearch(userList);
        new ContactSearchDialogCompat<>(context, "",
                "What are you looking for...?", null, sampleSearch,
                (dialog, item, position) -> {
                    updateTranzaction(tranzactionPosition, item.getName());
                    checkCategoryOfStore(tranzactionPosition, item.getName());
                    Toast.makeText(context, item.getTitle(),
                            Toast.LENGTH_SHORT
                    ).show();
                    dialog.dismiss();
                },
                category -> {
                    checkCategoryOfStore(tranzactionPosition, category);
                }

        ).show();
    }

    private void checkCategoryOfStore(int tranzactionPosition, String category) {
        String storeName = mTransactionModelList.get(tranzactionPosition).getName();
        //If there is no default specified category with this store
        if (mCategoryModelUserList.stream().
                filter(categoryModel -> category.equals(categoryModel.getName()) &&
                        categoryModel.getStores().contains(storeName))
                .collect(Collectors.toList()).isEmpty()) {
            if (mCategoryModelList.stream()
                    .filter(categoryModel -> category.equals(categoryModel.getName())
                            && categoryModel.getStores().contains(storeName))
                    .collect(Collectors.toList()).isEmpty()) {
                updateUserCategory(category, storeName);
            }
        }
    }

    private void updateUserCategory(String category, String storeName) {
        List<String> stores = new ArrayList<>();

        //Update extracted List
        List<CategoryModel> filteredUserCategories = mCategoryModelUserList.stream()
                .filter(categoryModel -> category.equals(categoryModel.getName()))
                .collect(Collectors.toList());

        if (!filteredUserCategories.isEmpty()) {
            stores.addAll(filteredUserCategories.get(0).getStores());
        }

        stores.add(storeName);
        CategoryModel categoryAdded = new CategoryModel("", stores, category);
        mCategoryModelUserList.add(categoryAdded);

        mDb.collection("users").document(mCurrentUser.getUid())
                .collection("categories")
                .document(category)
                .set(categoryAdded);
    }

    private ArrayList<SearchModel> createSampleSearch(ArrayList<String> userList) {
        ArrayList<SearchModel> customArrayList =
                new ArrayList<>();
        for (String category : userList
        ) {
            customArrayList.add(new SearchModel(category, IconDrawable.getIconForCategory(category)));
        }
        return customArrayList;
    }

    private void updateTranzaction(int tranzactionPosition, String category) {

        //1.First, update tranzaction in list
        TransactionModel updatedTranzaction = mTransactionModelList.get(tranzactionPosition);
        updatedTranzaction.setCategory(category);

        //2.Second, update tranzaction in database
        mDb.collection("users").document(mCurrentUser.getUid())
                .collection("tranzactions")
                .document(updatedTranzaction.getDocumentId())
                .update("category", category);

        //3.Refresh Listview and PieChart
        this.notifyDataSetChanged();
    }

    public void setmCategoryModelList(ArrayList<CategoryModel> categoryModelList) {
        this.mCategoryModelList = categoryModelList;
    }

    public void setmCategoryModelUserList(ArrayList<CategoryModel> categoryModelUserList) {
        this.mCategoryModelUserList = categoryModelUserList;
    }
}
