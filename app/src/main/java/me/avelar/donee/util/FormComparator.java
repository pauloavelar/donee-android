package me.avelar.donee.util;

import java.util.Comparator;

import me.avelar.donee.model.Form;

public class FormComparator implements Comparator<Form> {

    @Override
    public int compare(Form form1, Form form2) {
        String category1 = form1.getCategory();
        String category2 = form2.getCategory();
        if (category1 == null) return  1;
        if (category2 == null) return -1;

        if (category1.equalsIgnoreCase(category2)) {
            return form1.getName().compareToIgnoreCase(form2.getName());
        } else {
            return category1.compareToIgnoreCase(category2);
        }
    }

}
