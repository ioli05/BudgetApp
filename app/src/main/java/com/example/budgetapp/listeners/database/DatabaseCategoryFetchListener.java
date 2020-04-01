package com.example.budgetapp.listeners.database;

import com.example.budgetapp.model.CategoryModel;

import java.util.List;

public interface DatabaseCategoryFetchListener {

    void onCategoriesFetched(List<CategoryModel> categoriesList, boolean isUserCustomCategories);

}
