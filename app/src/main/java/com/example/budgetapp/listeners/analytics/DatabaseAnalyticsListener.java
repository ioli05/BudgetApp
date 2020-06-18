package com.example.budgetapp.listeners.analytics;

import com.example.budgetapp.model.AnalyticModel;

import java.util.List;

public interface DatabaseAnalyticsListener {

    void onAnalyticsFetched(List<AnalyticModel> category);
}
