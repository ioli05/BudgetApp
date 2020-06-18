package com.example.budgetapp.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AnalyticModel {

    private String name;
    private Double spent;
    private String period;

}
