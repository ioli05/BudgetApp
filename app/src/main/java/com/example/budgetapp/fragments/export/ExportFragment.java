package com.example.budgetapp.fragments.export;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.example.budgetapp.R;
import com.example.budgetapp.model.AnalyticModel;
import com.example.budgetapp.model.CategoryModel;
import com.example.budgetapp.model.UserDetailsModel;
import com.example.budgetapp.service.DatabaseService;
import com.example.budgetapp.utils.PieChartColor;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import in.goodiebag.carouselpicker.CarouselPicker;

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
    Map<String, Long> ages;

    String categorySelected;

    UserDetailsModel user;

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
        databaseService.getUserDate();
        databaseService.fetchUserCategory();
        databaseService.fetchDefaultCategories();
        databaseService.fetchAnalytics();
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
                putBarData(categorySelected);

            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        this.databaseService.setDatabaseAnalyticsListener(map -> {
            this.map = map;
            initCarousel();
            putBarData(categorySelected);
        });

        this.databaseService.setDatabaseAnalyticsAgeListener(map -> {
            ages = map;
            putBarData(categorySelected);
        });

        this.databaseService.setFetchUserDetailsListener(user -> {
            this.user = user;
            if (user.isUsageOfData()) {
                databaseService.fetchAgeAverage(user.getAge());
            }
        });

    }

    public void putBarData(String category) {

        BarDataSet bar = new BarDataSet(values(category), "");
        bar.setColors(PieChartColor.getColors());
        BarData barData = new BarData();
        barData.setBarWidth(0.6f);
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

            xAxis.setGranularity(1f);
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

            barChart.getAxisRight().setEnabled(false);
            barChart.getLegend().setEnabled(false);
            barChart.getDescription().setEnabled(false);
            barChart.setVisibleXRangeMaximum(4); // allow 20 values to be displayed at once on the x-axis, not more
            barChart.moveViewToX(3);

            YAxis leftAxis = barChart.getAxisLeft();
            leftAxis.setAxisMinimum(0);
            leftAxis.setAxisMaximum(barChart.getBarData().getYMax() + 30);

            if (!isNull(user) && user.isUsageOfData() && !isNull(ages.get(categorySelected))) {


//            if (user.isUsageOfData()) {
                LimitLine ll1 = new LimitLine(400f, "Average");
                ll1.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_TOP);
                ll1.setLineWidth(1f);
                ll1.setLineColor(Color.BLACK);
                ll1.setTextSize(10f);

                leftAxis = barChart.getAxisLeft();
                leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
                leftAxis.addLimitLine(ll1);
                leftAxis.setAxisMinimum(0);
                leftAxis.setAxisMaximum(Math.max(bar.getYMax(), ll1.getLimit()) + 30);
                leftAxis.setDrawZeroLine(false);
                leftAxis.setDrawLimitLinesBehindData(true);
            }
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
        List<String> result = new ArrayList<>();
        result.addAll(map.keySet());

        for (String s : result) {
            categories.add(new CarouselPicker.TextItem(s, 20));
        }
        CarouselPicker.CarouselViewAdapter textAdapter = new CarouselPicker.
                CarouselViewAdapter(this.getContext(), categories, 0);

        categorySelected = !categories.isEmpty() ? categories.get(0).getText() : "";
        carouselPicker.setAdapter(textAdapter);
        textAdapter.notifyDataSetChanged();
    }

    private void initializeFields() {
        carouselPicker = getView().findViewById(R.id.carousel_categories);
        barChart = getView().findViewById(R.id.bar_chart);

        databaseService = DatabaseService.instance();
        categories = new ArrayList<>();
        map = new HashMap<>();
        ages = new HashMap<>();

    }

}
