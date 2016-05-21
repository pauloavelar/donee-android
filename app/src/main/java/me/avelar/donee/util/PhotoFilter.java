package me.avelar.donee.util;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Created by pauloavelar on 8/18/15.
 */
public class PhotoFilter implements FilenameFilter {

    private String filter;

    public PhotoFilter(String filter) {
        this.filter = filter;
    }

    @Override
    public boolean accept(File dir, String filename) {
        return filename.equals(filter);
    }
}
