package com.example.budgetapp.model.fragments.budget;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class BudgetViewModel extends ViewModel {
    private MutableLiveData<String> mText;

    public BudgetViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Budget fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
