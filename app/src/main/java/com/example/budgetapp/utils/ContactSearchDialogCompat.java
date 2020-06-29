package com.example.budgetapp.utils;

import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;

import com.example.budgetapp.R;
import com.example.budgetapp.adapter.ContactModelAdapter;
import com.example.budgetapp.listeners.category.AddCategorySearchListener;
import com.example.budgetapp.model.SearchModel;

import java.util.ArrayList;
import java.util.List;

import ir.mirrajabi.searchdialog.SimpleSearchFilter;
import ir.mirrajabi.searchdialog.core.BaseSearchDialogCompat;
import ir.mirrajabi.searchdialog.core.SearchResultListener;
import ir.mirrajabi.searchdialog.core.Searchable;


public class ContactSearchDialogCompat<T extends Searchable> extends BaseSearchDialogCompat<T> {
    private String mTitle;
    private String mSearchHint;
    private SearchResultListener<T> mSearchResultListener;
    private AddCategorySearchListener mAddCategorySearchListener;
    List<SearchModel> unfilteredCategories;

    public ContactSearchDialogCompat(
            Context context, String title, String searchHint,
            @Nullable Filter filter, ArrayList<T> items,
            SearchResultListener<T> searchResultListener,
            AddCategorySearchListener mAddCategorySearchListener
    ) {
        super(context, items, filter, null, null);
        init(title, searchHint, searchResultListener, mAddCategorySearchListener);
        unfilteredCategories = (List<SearchModel>)items.clone();
    }

    private void init(
            String title, String searchHint,
            SearchResultListener<T> searchResultListener,
            AddCategorySearchListener addCategorySearchListener
    ) {
        mTitle = title;
        mSearchHint = searchHint;
        mSearchResultListener = searchResultListener;
        mAddCategorySearchListener = addCategorySearchListener;
    }

    @Override
    protected void getView(View view) {
        setContentView(view);

        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        setCancelable(true);

        TextView txtTitle = (TextView) view.findViewById(R.id.txt_title);
            ImageView addButton = view.findViewById(R.id.add_button);
        final EditText searchBox = (EditText) view.findViewById(getSearchBoxId());

        txtTitle.setText(mTitle);
        searchBox.setHint(mSearchHint);
        view.findViewById(R.id.dummy_background)
                .setOnClickListener(view1 -> dismiss());
        final ContactModelAdapter adapter = new ContactModelAdapter<>(getContext(),
                R.layout.image_adapter_item, getItems()
        );


        addButton.setOnClickListener(v ->{
            String searchedCategory = searchBox.getText().toString();
            String customCategory = searchedCategory.substring(0, 1).toUpperCase() + searchedCategory.substring(1);

            ((ArrayList<SearchModel>)this.getItems()).add(new SearchModel(customCategory));
            this.mAddCategorySearchListener.addCategory(customCategory);
            adapter.setItems(this.getItems());
            adapter.notifyDataSetChanged();
        });

        adapter.setSearchResultListener(mSearchResultListener);
        adapter.setSearchDialog(this);

        setFilterResultListener(items -> ((ContactModelAdapter) ContactSearchDialogCompat.this.getAdapter())
                .setSearchTag(searchBox.getText().toString())
                .setItems(items));

        setFilter(new SimpleSearchFilter<>(this.getItems(), this.getFilterResultListener(), false, 0.33f));

        setAdapter(adapter);
    }

    @LayoutRes
    @Override
    protected int getLayoutResId() {
        return R.layout.activity_dialog;
    }

    @IdRes
    @Override
    protected int getSearchBoxId() {
        return R.id.txt_search;
    }

    @IdRes
    @Override
    protected int getRecyclerViewId() {
        return R.id.rv_items;
    }
}