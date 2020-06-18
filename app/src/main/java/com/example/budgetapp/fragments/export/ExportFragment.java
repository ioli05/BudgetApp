package com.example.budgetapp.fragments.export;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.budgetapp.R;
import com.example.budgetapp.model.AnalyticModel;
import com.example.budgetapp.model.CategoryModel;
import com.example.budgetapp.service.DatabaseService;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import in.goodiebag.carouselpicker.CarouselPicker;

import static com.google.common.collect.Iterables.isEmpty;
import static java.util.Objects.isNull;

@RequiresApi(api = Build.VERSION_CODES.O)
public class ExportFragment extends Fragment {

    CarouselPicker carouselPicker;
    BarChart barChart;

    List<CarouselPicker.PickerItem> categories;

    DatabaseService databaseService;

    ArrayList<CategoryModel> mCategoryList = new ArrayList<>();
    ArrayList<CategoryModel> mCategoryListUser = new ArrayList<>();

    Map<String, List<AnalyticModel>> map;
    String categorySelected;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_export, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeFields();
        initializeListeners();
        fetchData();
    }

    private void fetchData() {
        databaseService.refreshCurrentUser();
        databaseService.fetchUserCategory();
        databaseService.fetchDefaultCategories();
    }

    private void initializeListeners() {

        this.databaseService.setDatabaseCategoryFetchListener((categoriesList, isUserCustomCategories) -> {

            if (isUserCustomCategories) {
                this.mCategoryListUser.clear();
                this.mCategoryListUser.addAll(categoriesList);
            }
            else {
                this.mCategoryList.clear();
                this.mCategoryList.addAll(categoriesList);
            }
            if (!isEmpty(mCategoryList) && !isEmpty(mCategoryListUser)) {
                initCarousel();
            }
        });

        this.carouselPicker.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Toast.makeText(getContext(), "Selected" + categories.get(position).getText(),
                        Toast.LENGTH_SHORT);

                barChart.clear();
                categorySelected = categories.get(position).getText();
                if (!map.containsKey(categorySelected)) {
                    databaseService.fetchAnalytics(categorySelected);
                }
                else {
                    putBarData(categorySelected);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        this.databaseService.setDatabaseAnalyticsListener(category -> {
            if (!isNull(category)) {
                map.put(categorySelected, category);
            }
            putBarData(categorySelected);
        });

    }

    public void putBarData(String category) {

        BarDataSet bar = new BarDataSet(values(category), "");
        BarData barData = new BarData();
        barData.setBarWidth(0.2f);
        barData.addDataSet(bar);

        if (bar.getEntryCount() != 0) {
            barChart.clear();
            barChart.setData(barData);
            barChart.invalidate();
            barChart.getAxisRight().setDrawGridLines(false);
            barChart.getAxisLeft().setDrawGridLines(false);
            barChart.getXAxis().setDrawGridLines(false);

            XAxis xAxis = barChart.getXAxis();
            xAxis.setValueFormatter(new IndexAxisValueFormatter(map.get(categorySelected).stream()
                    .map(AnalyticModel::getPeriod).collect(Collectors.toList())));

            xAxis.setLabelCount(1);
            xAxis.setGranularity(1f);
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

            barChart.getAxisRight().setEnabled(false);
            barChart.getLegend().setEnabled(false);
            barChart.getDescription().setEnabled(false);

        }
    }

    private ArrayList<BarEntry> values(String category) {

        ArrayList<BarEntry> entries = new ArrayList<>();

        if (map.containsKey(category)) {
            AnalyticModel model;

            for (int i = 0; i < map.get(category).size(); i++) {
                model = map.get(categorySelected).get(i);
                entries.add(new BarEntry(i, model.getSpent().floatValue()));
            }
        }
        return entries;
    }

    public void initCarousel() {
        List<String> result = databaseService.getCategories();

        for(String s : result) {
            categories.add(new CarouselPicker.TextItem(s, 20));
        }
        CarouselPicker.CarouselViewAdapter textAdapter = new CarouselPicker.
                CarouselViewAdapter(this.getContext(), categories, 0);

        carouselPicker.setAdapter(textAdapter);

    }

    private void initializeFields() {
        carouselPicker = getView().findViewById(R.id.carousel_categories);
        barChart = getView().findViewById(R.id.bar_chart);

        databaseService = DatabaseService.instance();
        categories = new ArrayList<>();
        map = new HashMap<>();

    }

}
