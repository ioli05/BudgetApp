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

import static android.text.TextUtils.isEmpty;

@RequiresApi(api = Build.VERSION_CODES.O)
public class ChangePasswordDialog extends AppCompatDialogFragment {
    EditText oldPassword, newPassword, retypePassword;
    Button save, cancel;

    DatabaseService databaseService;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.my_dialog);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_change_password, null);

        builder.setView(view)
                .setTitle("Change password");

        initialiseFields(view);

        initialiseListeners();

        return builder.create();
    }

    private void initialiseListeners() {
        save.setOnClickListener(view ->
        {

            if (!validateFields()){
                return;
            }
            databaseService.updatePassword(oldPassword.getText().toString(),
                    newPassword.getText().toString());

            ((BottomNavigationView)getActivity().findViewById(R.id.nav_view))
                    .setSelectedItemId(R.id.navigation_settings);
        });

        cancel.setOnClickListener(view -> {
            ((BottomNavigationView)getActivity().findViewById(R.id.nav_view))
                    .setSelectedItemId(R.id.navigation_settings);
        });
    }

    private boolean validateFields() {

        if (isEmpty(oldPassword.getText().toString())){
            oldPassword.setError("Current Password field is empty");
            return false;
        }

        if (isEmpty(newPassword.getText())){
            oldPassword.setError("New Password field is empty");
            return false;
        }

        if (isEmpty(retypePassword.getText())){
            oldPassword.setError("Retype Password field is empty");
            return false;
        }

        if (!retypePassword.getText().toString().equals(newPassword.getText().toString())) {
            retypePassword.setError("Passwords are not the same");
            return false;
        }
        return true;
    }


    private void initialiseFields(View view) {
        oldPassword = view.findViewById(R.id.current_password);
        newPassword= view.findViewById(R.id.new_password);
        retypePassword = view.findViewById(R.id.retype_new_password);

        save = view.findViewById(R.id.save_new_password);
        cancel = view.findViewById(R.id.cancel_new_password);

        databaseService = DatabaseService.instance();

    }
}
