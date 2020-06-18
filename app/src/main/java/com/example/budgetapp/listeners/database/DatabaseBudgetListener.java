package com.example.budgetapp.listeners.database;

import com.example.budgetapp.model.BudgetModel;
import com.example.budgetapp.model.ExpandableModel;

import java.util.List;

public interface DatabaseBudgetListener {
        void onBudgetFetch(List<BudgetModel> expandableModelList);
}
