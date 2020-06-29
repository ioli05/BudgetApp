package com.example.budgetapp.fragments.importt;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.budgetapp.R;
import com.example.budgetapp.model.fragments.importt.CSVTranzactionFileParser;
import com.example.budgetapp.service.DatabaseService;
import com.example.budgetapp.utils.ImportDialog;

import java.io.FileNotFoundException;
import java.io.InputStream;

@RequiresApi(api = Build.VERSION_CODES.O)
public class ImportFragment extends Fragment {

    private static final int PERMISION_REQUEST_STORAGE = 1000;
    private static final int READ_REQUEST_CODE = 42;

    private Button importDevice, importManually;
    private ProgressBar bar;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_import, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeFields();
        DatabaseService.instance().refreshCurrentUser();
        if (ContextCompat.checkSelfPermission(this.getContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                PERMISION_REQUEST_STORAGE);
        }

        importDevice.setOnClickListener(v -> {
            performFileSearch();
        });
        importManually.setOnClickListener(v -> {
            openDialog();
        });
    }

    private void openDialog() {
        ImportDialog importDialog = new ImportDialog();
        importDialog.show(getChildFragmentManager(), "import dialog");
    }


    private void initializeFields() {
        importDevice = this.getView().findViewById(R.id.import_device);
        importManually = this.getView().findViewById(R.id.import_manually);
        bar = this.getView().findViewById(R.id.importBar);
    }

    private String readText(Uri uri) throws FileNotFoundException {

        CSVTranzactionFileParser csvTranzactionFileParser = new CSVTranzactionFileParser();

        InputStream inputStream = this.getContext().getContentResolver().openInputStream(uri);
        csvTranzactionFileParser.parseFile(inputStream);

        bar.setVisibility(View.GONE);
        return "";
    }

    private void performFileSearch() {
        bar.setVisibility(View.VISIBLE);
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                String path = uri.getPath();
                getContext().getContentResolver().getType(uri);

                Toast.makeText(this.getContext(), "" + path, Toast.LENGTH_SHORT).show();
                try {
                    readText(uri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISION_REQUEST_STORAGE) {
            if (grantResults[0]  == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this.getContext(), "Permision granted", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this.getContext(), "Permission not granted", Toast.LENGTH_SHORT).show();
            }
        }

    }
}
