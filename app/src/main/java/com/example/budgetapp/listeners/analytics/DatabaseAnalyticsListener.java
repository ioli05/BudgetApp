package com.example.budgetapp.listeners.analytics;

import com.example.budgetapp.model.AnalyticModel;

import java.util.List;
import java.util.Map;

public interface DatabaseAnalyticsListener {

    void onAnalyticsFetched(Map<String, List<AnalyticModel>> category);
}
