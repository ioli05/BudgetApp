package com.example.budgetapp.listeners.database;

import com.example.budgetapp.model.TransactionModel;

import java.util.List;

public interface DatabaseServiceTransactionListener {

    void onObjectReady(List<TransactionModel> transactionList,
                       List<TransactionModel> filteredTranzactionList);

}
