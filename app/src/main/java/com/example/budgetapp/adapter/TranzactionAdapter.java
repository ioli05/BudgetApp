package com.example.budgetapp.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.budgetapp.R;
import com.example.budgetapp.model.CategoryModel;
import com.example.budgetapp.model.SearchModel;
import com.example.budgetapp.model.TranzactionModel;
import com.example.budgetapp.utils.ContactSearchDialogCompat;
import com.example.budgetapp.utils.IconDrawable;
import com.github.mikephil.charting.charts.PieChart;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TranzactionAdapter extends ArrayAdapter<TranzactionModel> {

    Activity context;

    List<Double> mTranzactionSum;
    List<String> mTranzactionName;
    List<String> mTranzactionCategory;

    ArrayList<TranzactionModel> mTranzactionModelList;
    ArrayList<CategoryModel> mCategoryModelList;
    ArrayList<CategoryModel> mCategoryModelUserList;

    FirebaseFirestore mDb;
    FirebaseUser mCurrentUser;

    PieChart mPieChart;

    public TranzactionAdapter(Activity context, ArrayList<TranzactionModel> tranzactionList, FirebaseFirestore mDb,
                              FirebaseUser mCurrentUser, PieChart mPieChart) {

        super(context, R.layout.activity_tranzaction, tranzactionList);

        this.context = context;
        this.mTranzactionModelList = tranzactionList;
        this.mDb = mDb;
        this.mCurrentUser = mCurrentUser;
        this.mPieChart = mPieChart;

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

        mTranzactionSum.addAll(mTranzactionModelList.stream().map(TranzactionModel::getSum).collect(Collectors.toList()));
        mTranzactionName.addAll(mTranzactionModelList.stream().map(TranzactionModel::getName).collect(Collectors.toList()));
        mTranzactionCategory.addAll(mTranzactionModelList.stream().map(TranzactionModel::getCategory).collect(Collectors.toList()));

        TranzactionSum.setText(mTranzactionSum.get(position).toString());
        TranzactionName.setText(mTranzactionName.get(position));
        TranzactionCategory.setText(mTranzactionCategory.get(position));

        return rowView;

    }

    private void showPopupEventTest(int tranzactionPosition, ArrayList<String> userList) {

        ArrayList<SearchModel> sampleSearch = createSampleSearch(userList);
        new ContactSearchDialogCompat<SearchModel>(context, "",
                "What are you looking for...?", null, sampleSearch,
                (dialog, item, position) -> {
                    updateTranzaction(tranzactionPosition, item.getName());
                    Toast.makeText(context, item.getTitle(),
                            Toast.LENGTH_SHORT
                    ).show();
                    dialog.dismiss();
                },
                category -> checkCategoryOfStore(tranzactionPosition, category)
        ).show();
    }

    private void checkCategoryOfStore(int tranzactionPosition, String category) {
        String storeName = mTranzactionModelList.get(tranzactionPosition).getName();
        //If there is no default specified category with this store
        if (mCategoryModelList.stream().filter(categoryModel -> category.equals(categoryModel.getName()) && categoryModel.getStores().contains(storeName)).collect(Collectors.toList()).isEmpty()) {
            if (mCategoryModelList.stream().filter(categoryModel -> category.equals(categoryModel.getName()) && categoryModel.getStores().contains(storeName)).collect(Collectors.toList()).isEmpty()) {
                updateUserCategory(category, storeName);
            }
        }
    }

    private void updateUserCategory(String category, String storeName) {
        List<String> stores = new ArrayList<>();
        //Update extracted List
        List<CategoryModel> filteredUserCategories = mCategoryModelUserList.stream().filter(categoryModel -> category.equals(categoryModel.getName())).collect(Collectors.toList());
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
        TranzactionModel updatedTranzaction = mTranzactionModelList.get(tranzactionPosition);
        updatedTranzaction.setCategory(category);

        //2.Second, update tranzaction in database
        mDb.collection("users").document(mCurrentUser.getUid())
                .collection("tranzactions")
                .document(updatedTranzaction.getDocumentId())
                .update("category", category);

        //3.Refresh Listview and PieChart
        this.notifyDataSetChanged();
    }

    public ArrayList<CategoryModel> getmCategoryModelList() {
        return mCategoryModelList;
    }

    public void setmCategoryModelList(ArrayList<CategoryModel> mCategoryModelList) {
        this.mCategoryModelList = mCategoryModelList;
    }

    public ArrayList<CategoryModel> getmCategoryModelUserList() {
        return mCategoryModelUserList;
    }

    public void setmCategoryModelUserList(ArrayList<CategoryModel> mCategoryModelUserList) {
        this.mCategoryModelUserList = mCategoryModelUserList;
    }

}
