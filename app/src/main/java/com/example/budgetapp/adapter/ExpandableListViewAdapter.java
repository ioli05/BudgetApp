package com.example.budgetapp.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.budgetapp.R;
import com.example.budgetapp.model.ExpandableModel;
import com.example.budgetapp.model.TransactionModel;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;

public class ExpandableListViewAdapter extends BaseExpandableListAdapter {

    private Context context;

    //group
    private List<ExpandableModel> listGroup;
    //child data
    private HashMap<String, List<TransactionModel>> listChild;

    public ExpandableListViewAdapter (Context context, List<ExpandableModel> listGroup, HashMap<String, List<TransactionModel>> listChild){
        this.context = context;
        this.listChild = listChild;
        this.listGroup = listGroup;
    }
    @Override
    public int getGroupCount() {
        return this.listGroup.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.listChild.get(this.listGroup.get(groupPosition).getName()).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.listGroup.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.listChild.get(this.listGroup.get(groupPosition).getName()).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return  groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        ExpandableModel group = (ExpandableModel) getGroup(groupPosition);
        if (convertView == null){
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_category, null);
        }

        ImageView icon = convertView.findViewById(R.id.icon_category);
        TextView category = convertView.findViewById(R.id.listTitle);
        TextView budget = convertView.findViewById(R.id.categoryBudget);

        icon.setImageResource(group.getIcon());
        category.setText(group.getName());
        if (group.getBudget() != 0) {
            Double sum = group.getBudget() + getSumForChildren(group.getName());
            budget.setText(String.format("%s %s", sum.toString(), this.context.getString(R.string.currency)));
            if (sum > 0) {
                budget.setTextColor(Color.GREEN);
            } else {
                budget.setTextColor(Color.RED);
            }
        } else {
            budget.setText(String.format("%s %s", getSumForChildren(group.getName()).toString(),
                    this.context.getString(R.string.currency)));
            budget.setTextColor(Color.GRAY);
        }
        return convertView;
    }

    private Double getSumForChildren(String name) {
        double categorysumPayment = listChild.get(name).stream()
                .filter(tranzaction -> tranzaction.getCategory().equals(name))
                .filter(tranzaction -> tranzaction.getType().equals("Payment"))
                .map(TransactionModel::getSum).reduce(0.0, Double::sum);
        double categorysumIncome = listChild.get(name).stream()
                .filter(tranzaction -> tranzaction.getCategory().equals(name))
                .filter(tranzaction -> tranzaction.getType().equals("Income"))
                .map(TransactionModel::getSum).reduce(0.0, Double::sum);
        return categorysumIncome - categorysumPayment;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        TransactionModel transactionModel = (TransactionModel) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_category_row, null);
        }

        String pattern = "dd-MM-yyyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

        TextView name = convertView.findViewById(R.id.categoryName);
        TextView sum = convertView.findViewById(R.id.categorySum);
        TextView date = convertView.findViewById(R.id.categoryDate);


        name.setText(transactionModel.getName());
        String sign;
        if (transactionModel.getType().equals("Income")) {
            sign = "+";
        } else {
            sign = "-";
        }
        sum.setText(String.format("%s%s %s", sign, transactionModel.getSum().toString(),
                this.context.getString(R.string.currency)));
        date.setText(simpleDateFormat.format(transactionModel.getDate()));

        if ("Income".equals(transactionModel.getType())) {
            sum.setTextColor(Color.GREEN);
        } else {
            sum.setTextColor(Color.RED);
        }


        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
