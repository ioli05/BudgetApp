package com.example.budgetapp.fragments.budget;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.example.budgetapp.R;
import com.example.budgetapp.adapter.ExpandableListViewAdapter;
import com.example.budgetapp.model.AnalyticModel;
import com.example.budgetapp.model.BudgetModel;
import com.example.budgetapp.model.CategoryModel;
import com.example.budgetapp.model.ExpandableModel;
import com.example.budgetapp.model.TransactionModel;
import com.example.budgetapp.model.UserDetailsModel;
import com.example.budgetapp.service.DatabaseService;
import com.example.budgetapp.utils.IconDrawable;
import com.example.budgetapp.utils.PieChartColor;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

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
    FloatingActionButton button;

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

        listGroup.addAll(getExpandableList());

        for (ExpandableModel current : listGroup) {
            updateChildList(current);
        }

        expandableListViewAdapter.notifyDataSetChanged();
    }

    private void updateChildList(ExpandableModel current) {
        listChild.put(current.getName(), mTransactionModelList.stream()
                .filter(tranzaction -> tranzaction.getCategory().equals(current.getName()))
                .collect(Collectors.toList()));
    }

    private List<ExpandableModel> getExpandableList() {
        List<ExpandableModel> list = new ArrayList<>();

        for (BudgetModel b : budgetModelList) {
            list.add(new ExpandableModel(b.getName(), IconDrawable.getIconForCategory(b.getName()),
                    b.getBudget()));
        }
        return list;
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
            mDatabaseService.fetchAgeAverage(user.getAge());
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
            } else {
                this.mCategoryList.clear();
                this.mCategoryList.addAll(categoriesList);
            }
            addToExpandableListView();
        });

        this.mDatabaseService.setDatabaseBudgetListener(this::refreshData);

        expandableListView.setOnGroupClickListener((parent, v, groupPosition, id) -> {
            if (expandableListView.isGroupExpanded(groupPosition)) {
                expandableListView.collapseGroup(groupPosition);
                previousGroup = -1;
            } else {
                expandableListView.expandGroup(groupPosition);
                if (previousGroup != -1){
                    expandableListView.collapseGroup(previousGroup);
                }
                previousGroup = groupPosition;
            }
            return true;
        });
        expandableListView.setOnItemLongClickListener((parent, view, position, id) -> {

            long packedPosition = expandableListView.getExpandableListPosition(position);

            int itemType = ExpandableListView.getPackedPositionType(packedPosition);
            int groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition);

            /*  if group item clicked */
            if (itemType == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                //  ...
                onGroupLongClick(groupPosition);
            }

            return false;
        });

        button.setOnClickListener(v -> {
            openChooseCategoryDialog();
        });

    }

    private void openChooseCategoryDialog() {

        Dialog dialog = new Dialog(this.getContext());
        dialog.setContentView(R.layout.dialog_categorylist);

        String[] categories = mDatabaseService.getCategories().toArray(new String[0]);

        ListView lv = dialog.findViewById(R.id.cat_list);
        ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(this.getContext(),
                android.R.layout.simple_list_item_1,
                mDatabaseService.getCategories().toArray(new String[0])) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // Cast the list view each item as text view
                TextView item = (TextView) super.getView(position, convertView, parent);

                // Set the typeface/font for the current item
                item.setTypeface(Typeface.MONOSPACE);

                // return the view
                return item;
            }
        };

        lv.setAdapter(mAdapter);
        lv.setOnItemClickListener((parent, view, position, id) -> {
            mDatabaseService.updateBudget(categories[position], 0.0);
            ExpandableModel e = new ExpandableModel(categories[position],
                    IconDrawable.getIconForCategory(categories[position]),
                    0.0);
            listGroup.add(e);
            updateChildList(e);
            expandableListViewAdapter.notifyDataSetChanged();
            dialog.cancel();

        });
        dialog.setCancelable(true);
        dialog.show();

    }

    private void checkIfToReset() {
        //check if the day is the same to add to analytics
        Date currentDate = new Date();
        if (user.getDate().getDay() == currentDate.getDay()) {
            //if is the same date as the createdAt => continue
            if (!user.getCreatedAt().equals(user.getDate())) {
                addToAnalytics();
                mDatabaseService.emptyBudget();
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

        if (user.isUsageOfData()) {
            mDatabaseService.addToAge(list, user.getAge());
        }
    }

    private String getPeriodForBudget(Date date) {
        return Month.of(date.getMonth()).getDisplayName(TextStyle.NARROW_STANDALONE, Locale.getDefault());
    }

    private Double getSumForBudgetCategory(String name) {
        return listChild.get(name).stream().map(TransactionModel::getSum).reduce(0.0, Double::sum);
    }

    private void setDate() {
        int day = refreshDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getDayOfMonth();

        LocalDate currentDate = LocalDate.now();

        if (day > currentDate.lengthOfMonth()) {
            day = currentDate.lengthOfMonth();
        }

        LocalDate localDate = LocalDate.of(currentDate.getYear(), currentDate.getMonth().getValue(),
                day);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

        mStartDate.setText(localDate.format(formatter));
        localDate = LocalDate.of(currentDate.getYear(), currentDate.getMonth().getValue() + 1, day);

        mEndDate.setText(localDate.format(formatter));

        mDatabaseService.fetchTransaction(mStartDate.getText().toString(), mEndDate.getText().toString());
    }

    private void onGroupLongClick(int groupPosition) {

        EditText text = new EditText(this.getContext());
        text.setSingleLine();

        AlertDialog.Builder alert = new AlertDialog.Builder(this.getContext());
        alert.setTitle("Set a budget:");
        alert.setView(text);
        alert.setPositiveButton("Ok", (dialog, whichButton) -> {
            if (!text.getText().toString().equals("")) {
                Double value = Double.parseDouble(text.getText().toString());
                Log.d("", "Pin Value : " + value);
                updateBudget(listGroup.get(groupPosition), value);
            }
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
        expandableListViewAdapter.notifyDataSetChanged();
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
        button = this.getView().findViewById(R.id.fab);

    }
}
