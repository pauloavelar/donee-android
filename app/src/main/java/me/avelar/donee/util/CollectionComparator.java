package me.avelar.donee.util;

import java.util.Comparator;

import me.avelar.donee.model.Collection;
import me.avelar.donee.model.Form;

public class CollectionComparator implements Comparator<Collection> {

    @Override
    public int compare(Collection c1, Collection c2) {
        // check the form name (ListView category)
        Form form1 = c1.getRelatedForm();
        Form form2 = c2.getRelatedForm();

        // check for nulls (should not happen, but
        // if it does the collection goes to the end)
        if (form1 == null) return  1;
        if (form2 == null) return -1;

        if (form1.getName().equals(form2.getName())) {
            // if the forms are the same, compares submitted dates
            // the -1 in the end makes it a descending order (newest to oldest)
            return c1.getSubmittedTime().compareTo(c2.getSubmittedTime()) * -1;
        } else {
            // if the forms are NOT the same, compares the form names
            return form1.getName().compareToIgnoreCase(form2.getName());
        }
    }

}
