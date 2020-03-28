package com.example.budgetapp.listeners.database;

import java.util.List;

import model.TranzactionModel;

public interface DatabaseServiceTransactionListener {

    void onObjectReady(List<TranzactionModel> transactionList,
                       List<TranzactionModel> filteredTranzactionList);

}
