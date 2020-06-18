package com.example.budgetapp.model;


import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailsModel {

    private String userEmail;
    private Date date;
    private Integer age;
    private boolean usageOfData;
    private boolean isPremium;
    private Date createdAt;

}
