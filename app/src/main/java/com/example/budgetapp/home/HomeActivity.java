package com.example.budgetapp.home;

import android.app.DatePickerDialog;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.budgetapp.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import model.CategoryModel;
import model.TranzactionModel;

import static java.util.Objects.isNull;

@RequiresApi(api = Build.VERSION_CODES.O)
public class HomeActivity extends AppCompatActivity implements OnChartValueSelectedListener, ButtonClickNotify {

    PieChart mPieChart;
    ListView mListView;

    FirebaseUser mCurrentUser;
    FirebaseFirestore db;

    DatabaseService mDatabaseService;

    int[] colors;

    TranzactionAdapter mTranzactionAdapter;

    ArrayList<TranzactionModel> mTranzactionModelList = new ArrayList<>();
    ArrayList<TranzactionModel> mTranzactionModelListFiltered = new ArrayList<>();

    ArrayList<CategoryModel> mCategoryList = new ArrayList<>();
    ArrayList<CategoryModel> mCategoryListUser = new ArrayList<>();

    String selectedPieValue;

    TextView mStartDate, mEndDate;
    DatePickerDialog.OnDateSetListener mStartDateListener, mEndDateListener;

    private PieDataSet mPieDataSet = new PieDataSet(Collections.emptyList(), "");

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initializeFields();
        initializeListeners();

        addToPieChart();
        addToListViewAdapterElements();

        fetchData();
    }

    private void fetchData() {
        mDatabaseService.fetchTransaction(mStartDate.getText().toString(),
                mEndDate.getText().toString());

        mDatabaseService.fetchDefaultCategories();

        mDatabaseService.fetchUserCategory();
    }

    private void initializeListeners() {

        mStartDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog startDate = new DatePickerDialog(
                    HomeActivity.this,
                    android.R.style.Theme_Holo_Light_Dialog,
                    mStartDateListener,
                    year, month, day);
            startDate.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            startDate.show();
        });
        mStartDateListener = (view, year, month, dayOfMonth) -> {
            String date = (month + 1) + "/" + dayOfMonth + "/" + year;
            mStartDate.setText(date);
        };

        mEndDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);


            DatePickerDialog endDate = new DatePickerDialog(
                    HomeActivity.this,
                    android.R.style.Theme_Holo_Light_Dialog,
                    mEndDateListener,
                    year, month, day);
            endDate.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            endDate.show();
        });
        mEndDateListener = (view, year, month, dayOfMonth) -> {
            String date = (month + 1) + "/" + dayOfMonth + "/" + year;
            mEndDate.setText(date);


            this.mDatabaseService.fetchTransaction(
                    mStartDate.getText().toString(),
                    mEndDate.getText().toString());
        };

        this.mDatabaseService.setDatabaseServiceTransactionListener((transactionModels, filTransactionModelList) -> {
            refreshData(transactionModels, filTransactionModelList);
            refreshPieChartData();
            mPieChart.notifyDataSetChanged();
            mPieChart.invalidate();
            mTranzactionAdapter.notifyDataSetChanged();
        });

        this.mDatabaseService.setDatabaseCategoryFetchListener((categoriesList, isUserCustomCategories) -> {

            if (isUserCustomCategories) {
                this.mCategoryListUser.clear();
                this.mCategoryListUser.addAll(categoriesList);
            } else {
                this.mCategoryList.clear();
                this.mCategoryList.addAll(categoriesList);
            }
        });
    }

    private void refreshData() {
        mTranzactionModelListFiltered.clear();
        mTranzactionModelListFiltered.addAll(mTranzactionModelList);
    }

    private void refreshData(List<TranzactionModel> transactionModels,
                             List<TranzactionModel> filtredTransactionModels) {

        this.mTranzactionModelList.clear();
        this.mTranzactionModelList.addAll(transactionModels);

        this.mTranzactionModelListFiltered.clear();
        this.mTranzactionModelListFiltered.addAll(filtredTransactionModels);
    }

    private void refreshPieChartData() {
        this.mPieDataSet.setValues(getPieData());
    }

    private void addToPieChart() {

        if (!isNull(this.mPieDataSet)) {

            mPieDataSet.setColors(colors);

            PieData mPieData = new PieData(mPieDataSet);

            mPieChart.setData(mPieData);
            mPieChart.invalidate();
            mPieChart.setHighlightPerTapEnabled(true);

            mPieChart.setOnChartValueSelectedListener(this);
        }
    }

    private void addToListViewAdapterElements() {

        mTranzactionAdapter = new TranzactionAdapter(this, mTranzactionModelListFiltered, db, mCurrentUser, mPieChart);
        mListView = findViewById(R.id.tranzactionList);
        mListView.setAdapter(mTranzactionAdapter);
        mTranzactionAdapter.setmCategoryModelList(mCategoryList);
        mTranzactionAdapter.setmCategoryModelUserList(mCategoryListUser);
        mTranzactionAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                refreshPieChartData();
                mPieChart.invalidate();
                mPieChart.notifyDataSetChanged();
            }
        });

    }


    private void initializeFields() {
        colors = new int[]{Color.BLACK, Color.CYAN, Color.GREEN, Color.MAGENTA};

        mPieChart = findViewById(R.id.tranzactionPieChart);
        mListView = findViewById(R.id.tranzactionList);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        mDatabaseService = DatabaseService.instance();

        mStartDate = findViewById(R.id.startDate);
        mEndDate = findViewById(R.id.endDate);
    }

    private ArrayList<PieEntry> getPieData() {

        List<String> categories;

        categories = mTranzactionModelList.stream().map(TranzactionModel::getCategory).distinct().collect(Collectors.toList());
        ArrayList<PieEntry> mPieData = new ArrayList<>();

        for (String current : categories) {
            double categorysum = mTranzactionModelList.stream().filter(tranzaction -> tranzaction.getCategory().equals(current)).map(TranzactionModel::getSum).reduce(0.0, Double::sum);
            mPieData.add(new PieEntry((float) categorysum, current));
        }
        return mPieData;
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

        PieEntry pe = (PieEntry) e;
        selectedPieValue = pe.getLabel();

        mTranzactionAdapter.clear();

        for (TranzactionModel tranzaction : mTranzactionModelList) {
            if (tranzaction.getCategory().equals(selectedPieValue)) {
                mTranzactionModelListFiltered.add(tranzaction);
            }
        }
        mTranzactionAdapter.notifyDataSetChanged();
    }

    @Override
    public void onNothingSelected() {

        mTranzactionAdapter.clear();
        refreshData();
        mTranzactionAdapter.notifyDataSetChanged();

    }

    @Override
    public void onButtonClick(int position) {

    }
}