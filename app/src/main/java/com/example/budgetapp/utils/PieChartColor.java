package com.example.budgetapp.utils;

import android.graphics.Color;

public class PieChartColor {

    private static int[] colors = new int[]{Color.parseColor("#827397"), Color.parseColor("#f2ed6f"),
                                            Color.parseColor("#aeefec"), Color.parseColor("#e7d39f"),
                                            Color.parseColor("#d8b9c3"), Color.parseColor("#d9bf77"),
                                            Color.parseColor("#ffd1bd"), Color.parseColor("#ffebd9"),
                                            Color.parseColor("#a8d3da"), Color.parseColor("#a8d3da"),
                                            Color.parseColor("#fae7cb")};

    public static int[] getColors(){
        return colors;
    }
}
