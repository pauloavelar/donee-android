package me.avelar.donee.util;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.File;

public class FileManager {

    public static boolean fileExists(@NonNull Context context, String filename) {
        if (filename == null) return false;

        String[] fileList = context.fileList();
        for (String file : fileList) {
            if (file.equals(filename)) return true;
        }
        return false;
    }

    public static File findFile(@NonNull Context context, String photoName) {
        File[] photo = context.getFilesDir().listFiles(new PhotoFilter(photoName));
        return photo.length > 0 ? photo[0] : null;
    }

    public static String findFilePath(@NonNull Context context, String photoName) {
        File temp = findFile(context, photoName);
        return temp != null ? temp.getAbsolutePath() : null;
    }

}
