package com.example.budgetapp.home;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.budgetapp.R;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import model.TranzactionModel;

public class TranzactionAdapter extends ArrayAdapter<TranzactionModel> {

    Activity context;
    List<Double> mTranzactionSum;
    List<String> mTranzactionName;
    List<String> mTranzactionCategory;

    ArrayList<TranzactionModel> mTranzactionModelList;

    public TranzactionAdapter(Activity context, ArrayList<TranzactionModel> tranzactionList){

        super(context, R.layout.activity_tranzaction, tranzactionList);

        this.context = context;
        this.mTranzactionModelList = tranzactionList;

    }
    public View getView(int position, View view, ViewGroup parent) {

        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.activity_tranzaction, null,true);

        TextView TranzactionSum = rowView.findViewById(R.id.TranzactionSum);
        TextView TranzactionName = rowView.findViewById(R.id.TranzactionName);
        Button TranzactionCategory = rowView.findViewById(R.id.TranzactionCategory);

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
}
