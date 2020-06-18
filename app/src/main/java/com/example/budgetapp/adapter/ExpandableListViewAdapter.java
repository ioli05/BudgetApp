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
        budget.setText(group.getBudget() == 0.0 ?  "Set a budget" : group.getBudget().toString());

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        TransactionModel transactionModel = (TransactionModel) getChild(groupPosition, childPosition);

        if (convertView == null){
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_category_row, null);
        }

        String pattern = "dd-MM-yyyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

        TextView name = convertView.findViewById(R.id.categoryName);
        TextView sum = convertView.findViewById(R.id.categorySum);
        TextView date = convertView.findViewById(R.id.categoryDate);


        name.setText(transactionModel.getName());
        sum.setText(transactionModel.getSum().toString());
        date.setText(simpleDateFormat.format(transactionModel.getDate()));

        if ("Income".equals(transactionModel.getType())){
            sum.setTextColor(Color.GREEN);
        }
        else {
            sum.setTextColor(Color.RED);
        }


        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
