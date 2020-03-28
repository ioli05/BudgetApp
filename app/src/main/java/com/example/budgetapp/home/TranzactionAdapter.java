package com.example.budgetapp.home;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.budgetapp.R;
import com.github.mikephil.charting.charts.PieChart;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import model.CategoryModel;
import model.TranzactionModel;

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

            List<String> categoryUserList = new ArrayList<>();
            categoryUserList = mCategoryModelUserList.stream().map(CategoryModel::getName).collect(Collectors.toList());
            categoryUserList.addAll(mCategoryModelList.stream().map(CategoryModel::getName).collect(Collectors.toList()));

            String[] categories = categoryUserList.toArray(new String[0]);

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Pick a category");
            builder.setItems(categories, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.d("TAG", "selected value" + categories[which]);
                    updateTranzaction(position, categories[which]);
                }
            });
            builder.show();
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

    private void updateTranzaction(int tranzactionPosition, String category) {

        //1.First, update tranzaction in list
        TranzactionModel updatedTranzaction = mTranzactionModelList.get(tranzactionPosition);
        updatedTranzaction.setCategory(category);

        //2.Second, update tranzaction in database
        mDb.collection("users").document(mCurrentUser.getUid())
                .collection("tranzactions")
                .document("tranzaction" + tranzactionPosition)
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
