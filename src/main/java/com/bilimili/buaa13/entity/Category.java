package com.bilimili.buaa13.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Category {
    private String mainClassId;
    private String subClassId;
    private String mainClassName;
    private String subClassName;
    private String description;
    private String rcmTag;


    public Category Category_initial(){
        Category tempCategory = new Category();
        tempCategory.setMainClassId("0");
        tempCategory.setSubClassId("1");
        tempCategory.setMainClassName("unknown");
        tempCategory.setSubClassName("unknown");
        tempCategory.setDescription("Category Not Defined");
        return tempCategory;
    }
}
