package com.example.budgetapp.listeners.database;

import java.util.List;

import model.CategoryModel;

public interface DatabaseCategoryFetchListener {

    void onCategoriesFetched(List<CategoryModel> categoriesList, boolean isUserCustomCategories);

}
