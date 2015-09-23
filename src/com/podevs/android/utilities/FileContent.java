package com.podevs.android.utilities;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * Gets data from file
 */

public class FileContent {
    public static String getFileContent(Context context, String filename) {
        try {
            FileInputStream str = context.openFileInput(filename);

            int available = str.available();
            byte data[] = new byte[available];
            str.read(data, 0, available);
            str.close();

            return new String(data);

        }catch (IOException e) {
            return "";
        }
    }

    public static void setFileContent(Context context, String filename, String content) {
        try {
            FileOutputStream str = context.openFileOutput(filename, Context.MODE_PRIVATE);

            str.write(content.getBytes());
            str.close();
        } catch (IOException e) {

        }
    }

    public static void removeFile(Context context, String filename) {
        context.deleteFile(filename);
    }
}
