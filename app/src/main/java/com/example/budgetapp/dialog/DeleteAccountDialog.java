package com.example.budgetapp.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.budgetapp.R;
import com.example.budgetapp.auth.LoginActivity;
import com.example.budgetapp.service.DatabaseService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

@RequiresApi(api = Build.VERSION_CODES.O)
public class DeleteAccountDialog extends AppCompatDialogFragment {

    Button save, cancel;

    DatabaseService databaseService;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.my_dialog);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_delete_account, null);

        builder.setView(view)
                .setTitle("Delete Account");

        initialiseFields(view);

        initialiseListeners();

        return builder.create();
    }

    private void initialiseListeners() {
        save.setOnClickListener(view -> {
            databaseService.deleteAccount();
            Intent i = new Intent(this.getActivity(), LoginActivity.class);
            startActivity(i);
        });

        cancel.setOnClickListener(view -> {
            ((BottomNavigationView)getActivity().findViewById(R.id.nav_view))
                    .setSelectedItemId(R.id.navigation_settings);
        });
    }


    private void initialiseFields(View view) {
        save = view.findViewById(R.id.save_delete_account);
        cancel = view.findViewById(R.id.cancel_delete_account);

        databaseService = DatabaseService.instance();
    }
}
