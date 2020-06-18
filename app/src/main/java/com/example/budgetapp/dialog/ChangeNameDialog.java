package com.example.budgetapp.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.budgetapp.R;
import com.example.budgetapp.service.DatabaseService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

@RequiresApi(api = Build.VERSION_CODES.O)
public class ChangeNameDialog  extends AppCompatDialogFragment {

    EditText newName;
    Button save, cancel;

    DatabaseService databaseService;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.my_dialog);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_change_name, null);

        builder.setView(view)
                .setTitle("Import");

        initialiseFields(view);

        initialiseListeners();

        return builder.create();
    }

    private void initialiseListeners() {
        save.setOnClickListener(view ->
                {
                    String name = newName.getText().toString();
                    databaseService.updateName(name);
                    ((BottomNavigationView)getActivity().findViewById(R.id.nav_view))
                            .setSelectedItemId(R.id.navigation_settings);
                });

        cancel.setOnClickListener(view -> {
            ((BottomNavigationView)getActivity().findViewById(R.id.nav_view))
                    .setSelectedItemId(R.id.navigation_settings);
        });
    }

    private void initialiseFields(View view) {
        newName = view.findViewById(R.id.new_name);

        save = view.findViewById(R.id.save_new_name);
        cancel = view.findViewById(R.id.cancel_new_name);

        databaseService = DatabaseService.instance();

    }
}
