package com.example.budgetapp.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.budgetapp.R;
import com.example.budgetapp.adapter.CategoryAdapter;
import com.example.budgetapp.model.CategoryModel;
import com.example.budgetapp.model.TransactionModel;
import com.example.budgetapp.service.DatabaseService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

@RequiresApi(api = Build.VERSION_CODES.O)
public class ImportDialog extends AppCompatDialogFragment {
    EditText name, sum, date;
    Spinner type, spinner;
    Button save, cancel;
    DatabaseService databaseService;

    String selectedCategory;
    CategoryAdapter categoryAdapter;
    ArrayList<CategoryModel> categoryList;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.my_dialog);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.import_manually, null);

        builder.setView(view)
                .setTitle("Import");

        initialiseFields(view);

        initialiseListeners();

        return builder.create();
    }

    private void initialiseListeners() {
        save.setOnClickListener(v -> {
            TransactionModel tranzaction = getTranzaction();
            if (!validateData(tranzaction)) {
                return;
            }
            databaseService.addTranzaction(tranzaction);
            ((BottomNavigationView) getActivity().findViewById(R.id.nav_view)).setSelectedItemId(R.id.navigation_home);
        });

        cancel.setOnClickListener(v -> {
            ((BottomNavigationView) getActivity().findViewById(R.id.nav_view)).setSelectedItemId(R.id.navigation_home);
        });

        this.databaseService.setDatabaseCategoryFetchListener((categoriesList, isUserCustomCategories) -> {

            this.categoryList.addAll(categoriesList);
            categoryAdapter.notifyDataSetChanged();
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                CategoryModel selected = (CategoryModel) parent.getItemAtPosition(position);
                selectedCategory = selected.getName();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private TransactionModel getTranzaction() {
        String tranzactionName = name.getText().toString();
        String tranzactionCategory = selectedCategory;
        Double tranzactionSum = 0.0;
        if (!sum.getText().toString().equals("")) {
            tranzactionSum = Double.parseDouble(sum.getText().toString());
        }
        Date tranzactionDate = null;

        if (!date.getText().toString().equals("")) {
            String sDate1 = date.getText().toString();
            try {
                tranzactionDate = new SimpleDateFormat("MM/dd/yyyy").parse(sDate1);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        String tranzactionType = type.getSelectedItem().toString();
        return new TransactionModel(tranzactionName, tranzactionSum, tranzactionCategory,
                tranzactionDate, "", tranzactionType);
    }

    private boolean validateData(TransactionModel tranzaction) {

        if (TextUtils.isEmpty(tranzaction.getName())) {
            name.setError("Please enter transaction name");
            return false;
        }

        if (tranzaction.getSum() == 0.0) {
            sum.setError("Please enter sum value");
            return false;
        }

        //check sum is number
        if (TextUtils.isDigitsOnly(tranzaction.getSum().toString())) {
            sum.setError("Sum value should be number");
            return false;
        }

        if (tranzaction.getDate() == null) {
            date.setError("Please enter mm/dd/yyyy date");
            return false;
        }

        return true;
    }

    private void initialiseFields(View view) {
        databaseService = DatabaseService.instance();

        databaseService.fetchUserCategory();
        databaseService.fetchDefaultCategories();

        name = view.findViewById(R.id.name);
        sum = view.findViewById(R.id.sum);
        date = view.findViewById(R.id.date);
        type = view.findViewById(R.id.type_spinner);
        spinner = view.findViewById(R.id.spinner_category);

        categoryList = new ArrayList<>();

        categoryAdapter = new CategoryAdapter(this.getContext(), categoryList);
        spinner.setAdapter(categoryAdapter);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getContext(),
                R.array.type_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        type.setAdapter(adapter);

        save = view.findViewById(R.id.save_import);
        cancel = view.findViewById(R.id.cancel_import);
    }
}
