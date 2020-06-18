package com.example.budgetapp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor

public class ExpandableModel {

    private String name;
    private Integer icon;
    private Double budget;
}
