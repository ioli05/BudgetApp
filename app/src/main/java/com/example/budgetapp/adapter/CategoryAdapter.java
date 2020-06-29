package com.example.budgetapp.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.budgetapp.R;
import com.example.budgetapp.model.CategoryModel;

import java.util.ArrayList;

public class CategoryAdapter extends ArrayAdapter<CategoryModel> {

    public CategoryAdapter(@NonNull Context context, ArrayList<CategoryModel> categoriesList) {
        super(context, 0, categoriesList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    private View initView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_dialogrow,
                    parent, false);
        }
        TextView categoryName = convertView.findViewById(R.id.title);
        categoryName.setTypeface(Typeface.MONOSPACE);

        CategoryModel category = getItem(position);

        if (category != null) {
            categoryName.setText(category.getName());
        }

        return convertView;
    }
}
