package com.bilimili.buaa13.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Category {
    private String mcId;
    private String scId;
    private String mcName;
    private String scName;
    private String descr;
    private String rcmTag;


    public Category Category_initial(){
        Category tempCategory = new Category();
        tempCategory.setMcId(0);
        tempCategory.setScId(1);
        tempCategory.setMcName("unknown");
        tempCategory.setScName("unknown");
        tempCategory.setDescr("Category Not Defined");
        return tempCategory;
    }
}
