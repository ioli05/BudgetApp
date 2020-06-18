package com.example.budgetapp.fragments.home;

import android.app.DatePickerDialog;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.example.budgetapp.R;
import com.example.budgetapp.adapter.TranzactionAdapter;
import com.example.budgetapp.model.CategoryModel;
import com.example.budgetapp.model.TransactionModel;
import com.example.budgetapp.service.DatabaseService;
import com.example.budgetapp.utils.CustomPieChartRenderer;
import com.example.budgetapp.utils.IconDrawable;
import com.example.budgetapp.utils.PieChartColor;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@RequiresApi(api = Build.VERSION_CODES.O)
public class HomeFragment extends Fragment implements OnChartValueSelectedListener {

    PieChart mPieChart;
    ListView mListView;

    FirebaseUser mCurrentUser;
    FirebaseFirestore db;

    DatabaseService mDatabaseService;

    int[] colors;

    TranzactionAdapter mTranzactionAdapter;

    ArrayList<TransactionModel> mTransactionModelList = new ArrayList<>();
    ArrayList<TransactionModel> mTransactionModelListFiltered = new ArrayList<>();

    ArrayList<CategoryModel> mCategoryList = new ArrayList<>();
    ArrayList<CategoryModel> mCategoryListUser = new ArrayList<>();

    String selectedPieValue;

    TextView mStartDate, mEndDate;
    DatePickerDialog.OnDateSetListener mStartDateListener, mEndDateListener;

    private PieDataSet mPieDataSet = new PieDataSet(Collections.emptyList(), "");

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeFields();
        initializeListeners();

        addToPieChart();
        addToListViewAdapterElements();

        fetchData();
    }

    private void fetchData() {
        mDatabaseService.refreshCurrentUser();
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
                    getContext(),
                    android.R.style.Theme_Holo_Light_Dialog,
                    mStartDateListener,
                    year, month, day);
            startDate.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            startDate.show();
        });
        mStartDateListener = (view, year, month, dayOfMonth) -> {
            String date = (month + 1) + "/" + dayOfMonth + "/" + year;
            mStartDate.setText(date);

            this.mDatabaseService.fetchTransaction(
                    mStartDate.getText().toString(),
                    mEndDate.getText().toString());
        };

        mEndDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);


            DatePickerDialog endDate = new DatePickerDialog(
                    getContext(),
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

        this.mDatabaseService.setDatabaseTranzactionAddedListener(tranzactionModel -> {
            //if is between dates => add filtered list
            Date end = null, start = null;
            try {
                end = new SimpleDateFormat("MM/dd/yyyy").parse(mEndDate.getText().toString());
                start = new SimpleDateFormat("MM/dd/yyyy").parse(mStartDate.getText().toString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (tranzactionModel.getDate().compareTo(end) <= 0 && tranzactionModel.getDate().compareTo(start) >= 0) {
                mTransactionModelListFiltered.add(tranzactionModel);
                refreshPieChartData();
                mPieChart.invalidate();
                mTranzactionAdapter.notifyDataSetChanged();
            }
            else {
                //mTranzactionAdapter.cat
                mTransactionModelList.add(tranzactionModel);
            }
        });
    }

    private void refreshData() {
        mTransactionModelListFiltered.clear();
        mTransactionModelListFiltered.addAll(mTransactionModelList);
    }

    private void refreshData(List<TransactionModel> transactionModels,
                             List<TransactionModel> filtredTransactionModels) {

        this.mTransactionModelList.clear();
        this.mTransactionModelList.addAll(transactionModels);

        this.mTransactionModelListFiltered.clear();
        this.mTransactionModelListFiltered.addAll(filtredTransactionModels);
    }

    private void refreshPieChartData() {
        this.mPieDataSet.setValues(getPieData());
    }

    private void addToPieChart() {

        if (!isNull(this.mPieDataSet)) {

            mPieChart.setExtraOffsets(40f, 0f, 40f, 0f);
            mPieChart.setRenderer(new CustomPieChartRenderer(mPieChart, 10f));
            mPieChart.setOnChartValueSelectedListener(this);
            mPieChart.setHighlightPerTapEnabled(true);

            mPieDataSet.setValueLinePart1Length(0.6f);
            mPieDataSet.setValueLinePart2Length(0.3f);
            mPieDataSet.setValueLineWidth(2f);
            mPieDataSet.setValueLinePart1OffsetPercentage(115f);  // Line starts outside of chart
            mPieDataSet.setUsingSliceColorAsValueLineColor(true);

            mPieDataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
            mPieDataSet.setValueTextSize(16f);
            mPieDataSet.setValueTypeface(Typeface.MONOSPACE);
            mPieDataSet.setColors(colors);
            // Value formatting
            mPieChart.setUsePercentValues(true);
            mPieDataSet.setSelectionShift(3f);
            mPieChart.setDrawHoleEnabled(true);
            mPieChart.setHoleRadius(50f);
            mPieChart.getLegend().setEnabled(false);
            mPieChart.setData(new PieData(mPieDataSet));
            mPieDataSet.setValueFormatter(new ValueFormatter() {
                DecimalFormat df = new DecimalFormat("#.#");

                @Override
                public String getFormattedValue(float value) {
                    if (Float.valueOf(df.format(value)) < 7){
                        return "";
                    }
                    return super.getFormattedValue(Float.valueOf(df.format(value))) ;
                }
            });

            mPieChart.getDescription().setEnabled(false);
            mPieChart.setEntryLabelTextSize(13f);
            mPieChart.setEntryLabelColor(0 );
            mPieChart.setEntryLabelTypeface(Typeface.MONOSPACE);
            mPieChart.getData().getColors();
        }
    }

    private void addToListViewAdapterElements() {

        mTranzactionAdapter = new TranzactionAdapter(getActivity(), mTransactionModelListFiltered, db, mCurrentUser, mPieChart);
        mListView = this.getView().findViewById(R.id.tranzactionList);
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
        colors = PieChartColor.getColors();

        mPieChart = this.getView().findViewById(R.id.tranzactionPieChart);

        mListView = this.getView().findViewById(R.id.tranzactionList);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        mDatabaseService = DatabaseService.instance();

        mStartDate = this.getView().findViewById(R.id.startDate);
        mEndDate = this.getView().findViewById(R.id.endDate);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/YYYY");

        mEndDate.setText(formatter.format(LocalDate.now()));
        mStartDate.setText(formatter.format(LocalDate.now().minusMonths(1)));
    }

    private ArrayList<PieEntry> getPieData() {

        List<String> categories;

        categories = mTransactionModelList.stream().map(TransactionModel::getCategory).distinct()
                .collect(Collectors.toList());
        ArrayList<PieEntry>  mPieData = new ArrayList<>();

        for (String current : categories) {
            double categorysumPayment = mTransactionModelList.stream()
                    .filter(tranzaction -> tranzaction.getCategory().equals(current))
                    .filter(tranzaction -> tranzaction.getType().equals("Payment"))
                    .map(TransactionModel::getSum).reduce(0.0, Double::sum);
            float sum = (float) Math.abs(categorysumPayment);

            mPieData.add(new PieEntry(sum, current));
        }
        return mPieData;
    }

    //Click on pie selection and get the transaction having the selected category
    @Override
    public void onValueSelected(Entry e, Highlight h) {

        PieEntry pe = (PieEntry) e;
        selectedPieValue = pe.getLabel();

        mTranzactionAdapter.clear();

        for (TransactionModel tranzaction : mTransactionModelList) {
            if (tranzaction.getCategory().equals(selectedPieValue)) {
                mTransactionModelListFiltered.add(tranzaction);
            }
        }
        mTranzactionAdapter.notifyDataSetChanged();
    }

    //Click outside the pie and get the entire list of transactions
    @Override
    public void onNothingSelected() {

        mTranzactionAdapter.clear();
        refreshData();
        mTranzactionAdapter.notifyDataSetChanged();

    }

    //TODO Add date to user
    //TODO If no date => end date = current date, start date = current_date - 1 month

}
