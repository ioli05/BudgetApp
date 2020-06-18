package com.example.budgetapp.fragments.budget;

import android.app.AlertDialog;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.budgetapp.R;
import com.example.budgetapp.adapter.ExpandableListViewAdapter;
import com.example.budgetapp.model.AnalyticModel;
import com.example.budgetapp.model.BudgetModel;
import com.example.budgetapp.model.CategoryModel;
import com.example.budgetapp.model.ExpandableModel;
import com.example.budgetapp.model.TransactionModel;
import com.example.budgetapp.model.UserDetailsModel;
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
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@RequiresApi(api = Build.VERSION_CODES.O)

public class BudgetFragment extends Fragment {

    FirebaseUser mCurrentUser;
    FirebaseFirestore db;

    DatabaseService mDatabaseService;

    int[] colors;

    Date refreshDate;

    ExpandableListView expandableListView;
    ExpandableListViewAdapter expandableListViewAdapter;

    ArrayList<TransactionModel> mTransactionModelList = new ArrayList<>();
    ArrayList<TransactionModel> mTransactionModelListFiltered = new ArrayList<>();

    ArrayList<CategoryModel> mCategoryList = new ArrayList<>();
    ArrayList<CategoryModel> mCategoryListUser = new ArrayList<>();


    TextView mStartDate, mEndDate;

    List<ExpandableModel> listGroup;
    HashMap<String, List<TransactionModel>> listChild = new HashMap<>();

    ArrayList<BudgetModel> budgetModelList = new ArrayList<>();

    int previousGroup = -1;

    UserDetailsModel user;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_budget, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeFields();
        initializeListeners();
        addToExpandableListView();

        fetchData();
    }

    private void initExpandableData() {

        List<String> categories;

        categories = mTransactionModelList.stream().map(TransactionModel::getCategory).distinct()
                .collect(Collectors.toList());

        for (String category : categories) {
            listGroup.add(getExpandableForCategory(category));
        }


        for (ExpandableModel current : listGroup) {
            listChild.put(current.getName(), mTransactionModelList.stream()
                    .filter(tranzaction -> tranzaction.getCategory().equals(current.getName()))
                    .collect(Collectors.toList()));
        }

        expandableListViewAdapter.notifyDataSetChanged();
    }

    private ExpandableModel getExpandableForCategory(String category) {
        BudgetModel budgetModel= budgetModelList.stream().filter(budget -> budget.getName()
                .equals(category)).findAny().orElse(null);

        if (!isNull(budgetModel)) {
            return new ExpandableModel(category, IconDrawable.getIconForCategory(category),
                    budgetModel.getBudget());
        }

        return new ExpandableModel(category, IconDrawable.getIconForCategory(category), 0.0);
    }

    private void addToExpandableListView() {
        expandableListViewAdapter = new ExpandableListViewAdapter(getContext(), listGroup, listChild);
        expandableListView.setAdapter(expandableListViewAdapter);
        expandableListViewAdapter.notifyDataSetChanged();
    }

    private void fetchData() {

        mDatabaseService.refreshCurrentUser();

        mDatabaseService.getUserDate();

        mDatabaseService.fetchDefaultCategories();

        mDatabaseService.fetchUserCategory();

        mDatabaseService.fetchBudget();
    }

    private void initializeListeners() {

        this.mDatabaseService.setFetchUserDetailsListener(user -> {
            this.user = user;
            refreshDate = user.getDate();
            checkIfToReset();
        });

        this.mDatabaseService.setDatabaseServiceTransactionListener((transactionModels, filTransactionModelList) -> {
            refreshData(transactionModels, filTransactionModelList);
            initExpandableData();

        });

        this.mDatabaseService.setDatabaseCategoryFetchListener((categoriesList, isUserCustomCategories) -> {

            if (isUserCustomCategories) {
                this.mCategoryListUser.clear();
                this.mCategoryListUser.addAll(categoriesList);
            }
            else {
                this.mCategoryList.clear();
                this.mCategoryList.addAll(categoriesList);
            }
            addToExpandableListView();
        });

        this.mDatabaseService.setDatabaseBudgetListener((budgetList) -> {
            refreshData(budgetList);
        });

        expandableListView.setOnGroupClickListener((parent, v, groupPosition, id) -> {
            Toast.makeText(getActivity(), "Group Clicked " + listGroup.get(groupPosition), Toast.LENGTH_SHORT).show();
            if (expandableListView.isGroupExpanded(groupPosition)) {
                expandableListView.collapseGroup(groupPosition);
                previousGroup = -1;
            }
            else {
                expandableListView.expandGroup(groupPosition);
                if (previousGroup != -1){
                    expandableListView.collapseGroup(previousGroup);
                }
                previousGroup = groupPosition;
            }
            return true;
        });
        expandableListView.setOnGroupExpandListener(groupPosition -> Toast.makeText(getActivity(),
                listGroup.get(groupPosition) + " Expanded", Toast.LENGTH_SHORT).show());
        expandableListView.setOnGroupCollapseListener(groupPosition -> Toast.makeText(getActivity(),
                listGroup.get(groupPosition) + " Collapsed", Toast.LENGTH_SHORT).show());
        expandableListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            Toast.makeText(getActivity(), listGroup.get(groupPosition) + " : " + listChild.get( listGroup.get(groupPosition)).get( childPosition), Toast.LENGTH_SHORT) .show();
            return false;
        });
        expandableListView.setOnItemLongClickListener((parent, view, position, id) -> {

            long packedPosition = expandableListView.getExpandableListPosition(position);

            int itemType = ExpandableListView.getPackedPositionType(packedPosition);
            int groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition);
            int childPosition = ExpandableListView.getPackedPositionChild(packedPosition);


            /*  if group item clicked */
            if (itemType == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                //  ...
                onGroupLongClick(groupPosition);
            }

            return false;
        });

    }

    private void checkIfToReset() {
        //check if the day is the same to add to analytics
        Date currentDate = new Date();
        if (user.getDate().getDay() == currentDate.getDay()) {
            //if is the same date as the createdAt => continue
            if (!user.getCreatedAt().equals(user.getDate())) {
                addToAnalytics();
            }
        }
        setDate();
    }

    private void addToAnalytics() {
        List<AnalyticModel> list = new ArrayList<>();

        //for every budget get sum and save it in list
        for (BudgetModel budget : budgetModelList) {

            if (budget.getBudget() != 0) {
                Double value = getSumForBudgetCategory(budget.getName());
                String period = getPeriodForBudget(new Date());
                list.add(new AnalyticModel(budget.getName(), value, period));
            }
        }
        mDatabaseService.addToAnalytic(list);
    }

    private String getPeriodForBudget(Date date) {
        return Month.of(date.getMonth()).getDisplayName(TextStyle.NARROW_STANDALONE, Locale.getDefault());
    }

    private Double getSumForBudgetCategory(String name) {
        return listChild.get(name).stream().map(TransactionModel::getSum).reduce(0.0, Double::sum);
    }

    private void setDate() {
        int day = refreshDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getDayOfMonth();
        if (day > 30) {
            day = 30;
        }

        LocalDate currentDate = LocalDate.now();
        LocalDate localDate = LocalDate.of(currentDate.getYear(), currentDate.getMonth().getValue(),
                currentDate.getDayOfMonth());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

        mStartDate.setText(localDate.format(formatter));
        localDate = LocalDate.of(currentDate.getYear(), currentDate.getMonth().getValue() + 1, day);

        mEndDate.setText(localDate.format(formatter));

        mDatabaseService.fetchTransaction(mStartDate.getText().toString(), mEndDate.getText().toString());
    }

    private void onGroupLongClick(int groupPosition) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this.getContext());
        alert.setTitle("Budget");
        alert.setMessage("Set a budget :");

        // Set an EditText view to get user input
        final EditText input = new EditText(this.getContext());
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        alert.setView(input);

        alert.setPositiveButton("Ok", (dialog, whichButton) -> {
            Double value = Double.parseDouble(input.getText().toString());
            Log.d("", "Pin Value : " + value);

            updateBudget(listGroup.get(groupPosition), value);

            return;
        });

        alert.show();
    }

    private void updateBudget(ExpandableModel expandableModel, Double value) {
        //1. Update BudgetList with new budget Value
        for (BudgetModel model : budgetModelList) {
            if (model.getName().equals(expandableModel.getName())){
                model.setBudget(value);
                budgetModelList.set(budgetModelList.indexOf(model), model);
                break;
            }
        }
        //2. Update listGroup budget
        listGroup.stream().filter(element -> element.equals(expandableModel)).forEach(model -> model.setBudget(value));

        //3. Update Database
        mDatabaseService.updateBudget(expandableModel.getName(), value);
    }

    private void refreshData(List<BudgetModel> budgetModelList) {

        this.budgetModelList.clear();
        this.budgetModelList.addAll(budgetModelList);

    }

    private void refreshData(List<TransactionModel> transactionModels,
                             List<TransactionModel> filtredTransactionModels) {

        this.mTransactionModelList.clear();
        this.mTransactionModelList.addAll(transactionModels);

        this.mTransactionModelListFiltered.clear();
        this.mTransactionModelListFiltered.addAll(filtredTransactionModels);
    }

    private void initializeFields() {
        colors = PieChartColor.getColors();

        listGroup = new ArrayList<>();
        expandableListView = this.getView().findViewById(R.id.expandableListView);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        mDatabaseService = DatabaseService.instance();

        refreshDate = new Date();
        mStartDate = this.getView().findViewById(R.id.startDate);
        mEndDate = this.getView().findViewById(R.id.endDate);

    }
}
