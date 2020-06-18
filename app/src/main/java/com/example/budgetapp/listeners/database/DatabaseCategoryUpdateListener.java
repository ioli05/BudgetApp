package com.example.budgetapp.listeners.database;

import com.example.budgetapp.model.CategoryModel;

import java.util.ArrayList;

public interface DatabaseCategoryUpdateListener {

    void updateCategory(ArrayList<CategoryModel> mCategoryModelUserList);

}
