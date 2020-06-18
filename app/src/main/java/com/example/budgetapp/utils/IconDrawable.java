package com.example.budgetapp.utils;

import com.example.budgetapp.R;

import java.util.HashMap;
import java.util.List;

public class IconDrawable {
    private static HashMap<String, Integer> iconMapper = new HashMap<String, Integer>(){{
        put("food", R.drawable.food);
        put("beauty", R.drawable.beauty);
        put("market", R.drawable.market);
        put("supermarket", R.drawable.market);
        put("restaurant", R.drawable.restaurant);
        put("transportation", R.drawable.transportation);
        put("default", R.drawable.smile);
    }};



    public static Integer getIconForCategory(String category){
        return iconMapper.get(category.toLowerCase()) != null ? iconMapper.get(category.toLowerCase()) : iconMapper.get("default");
    }
}
