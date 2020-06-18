package com.example.budgetapp.listeners.database;

import com.example.budgetapp.model.TransactionModel;

public interface DatabaseTranzactionAddedListener {
    void onTranzactionAdded(TransactionModel transactionModel);
}
