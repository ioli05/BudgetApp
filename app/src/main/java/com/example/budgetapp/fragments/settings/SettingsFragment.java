package com.example.budgetapp.fragments.settings;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.example.budgetapp.R;
import com.example.budgetapp.auth.LoginActivity;
import com.example.budgetapp.dialog.ChangeNameDialog;
import com.example.budgetapp.dialog.ChangePasswordDialog;
import com.example.budgetapp.dialog.DeleteAccountDialog;
import com.example.budgetapp.dialog.DeleteDataDialog;
import com.example.budgetapp.service.DatabaseService;

@RequiresApi(api = Build.VERSION_CODES.O)
public class SettingsFragment extends Fragment {

    ImageView changeName, changePassword, changeAge, upgradePremium, deleteAccount, deleteData, logOut;
    Switch useData;

    DatabaseService databaseService;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeFields();
        initializeListeners();
    }

    private void initializeFields() {

        changeName = this.getView().findViewById(R.id.change_name);
        changePassword = this.getView().findViewById(R.id.change_password);
        changeAge = this.getView().findViewById(R.id.change_age);
        upgradePremium = this.getView().findViewById(R.id.change_premium);
        deleteAccount = this.getView().findViewById(R.id.delete_account);
        deleteData = this.getView().findViewById(R.id.delete_data);
        logOut = this.getView().findViewById(R.id.log_out);

        useData = this.getView().findViewById(R.id.switch_age);

        databaseService = DatabaseService.instance();
    }

    private void initializeListeners() {
        changeName.setOnClickListener(view -> {
            openChangeNameDialog();
        });

        changePassword.setOnClickListener(view -> {
            openChangePasswordDialog();
        });

        changeAge.setOnClickListener(view -> {
            openChangeAgeDialog();
        });

        useData.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                databaseService.updateData(true);
            }
            else {
                databaseService.updateData(false);
            }
        });

        deleteAccount.setOnClickListener(view -> {
            openDeleteAcoountDialog();
        });

        deleteData.setOnClickListener(view -> {
            openDeleteDataDialog();
        });

        logOut.setOnClickListener(view -> {
            databaseService.logOutUser();
            Intent i = new Intent(this.getActivity(), LoginActivity.class);
            startActivity(i);
        });
    }

    private void openDeleteDataDialog() {
        DeleteDataDialog dialog = new DeleteDataDialog();
        dialog.show(getChildFragmentManager(), "Delete data");
    }

    private void openDeleteAcoountDialog() {
        DeleteAccountDialog dialog = new DeleteAccountDialog();
        dialog.show(getChildFragmentManager(), "Delete account");
    }

    private void openChangeAgeDialog() {
        ChangePasswordDialog dialog = new ChangePasswordDialog();
        dialog.show(getChildFragmentManager(), "Change age dialog");
    }


    private void openChangeNameDialog() {
        ChangeNameDialog dialog = new ChangeNameDialog();
        dialog.show(getChildFragmentManager(), "Change name dialog");
    }

    private void openChangePasswordDialog() {
        ChangePasswordDialog dialog = new ChangePasswordDialog();
        dialog.show(getChildFragmentManager(), "Change password dialog");
    }
}
