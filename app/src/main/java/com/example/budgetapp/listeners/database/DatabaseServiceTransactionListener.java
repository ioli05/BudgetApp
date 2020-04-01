package com.example.budgetapp.listeners.database;

import com.example.budgetapp.model.TranzactionModel;

import java.util.List;

public interface DatabaseServiceTransactionListener {

    void onObjectReady(List<TranzactionModel> transactionList,
                       List<TranzactionModel> filteredTranzactionList);

}
