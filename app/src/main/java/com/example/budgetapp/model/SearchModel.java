package com.example.budgetapp.model;

import com.example.budgetapp.utils.IconDrawable;

import ir.mirrajabi.searchdialog.core.Searchable;

public class SearchModel implements Searchable {
    private String mName;
    private int mIcon;

    public SearchModel (String name){
        this(name, IconDrawable.getIconForCategory(name));
    }

    public SearchModel (String name, int icon){
        this.mName = name;
        this.mIcon = icon;
    }
    @Override
    public String getTitle() {
        return mName;
    }

    public String getName() {
        return mName;
    }

    public SearchModel setName(String name) {
        mName = name;
        return this;
    }

    public Integer getImageUrl() {
        return mIcon;
    }

    public SearchModel setImageIcon(Integer imageIcon) {
        mIcon = imageIcon;
        return this;
    }
}
