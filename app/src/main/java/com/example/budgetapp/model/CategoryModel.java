package com.example.budgetapp.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CategoryModel {

    private String icon;
    private List<String> stores;
    private String name;

}
