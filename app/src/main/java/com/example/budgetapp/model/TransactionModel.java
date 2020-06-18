package com.example.budgetapp.model;


import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class TransactionModel {

    String name;
    Double sum;
    String category;
    Date date;
    String documentId;
    String type;

}
