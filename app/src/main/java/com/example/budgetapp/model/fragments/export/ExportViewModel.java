package com.example.budgetapp.model.fragments.export;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ExportViewModel extends ViewModel {
    private MutableLiveData<String> mText;

    public ExportViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Export fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
