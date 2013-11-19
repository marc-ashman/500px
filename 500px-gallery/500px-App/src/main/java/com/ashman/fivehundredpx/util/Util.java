package com.ashman.fivehundredpx.util;


import android.graphics.Bitmap;

import com.ashman.fivehundredpx.BaseFragmentActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

public class Util {
    //Email regex taken from http://www.regular-expressions.info/email.html
    public static final String EMAIL_REGEX = "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:" +
            "\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0" +
            "-9])?\\.)+(?:[A-Z]{2}|com|org|net|edu|gov|mil|biz|info|mobi|nam" +
            "e|aero|asia|jobs|museum)\\b";

    public static File saveBitmapToLocalFile(String directory,
                                 String filename, Bitmap bitmap) {
        try {
            int size = bitmap.getRowBytes() * bitmap.getHeight();
            ByteBuffer buffer = ByteBuffer.allocate(size);
            bitmap.copyPixelsToBuffer(buffer);
            buffer.rewind();
            byte[] bytes = new byte[size];
            buffer.get(bytes, 0, bytes.length);

            File directoryFile = new File(directory);
            File tempFile = File.createTempFile(filename, null, directoryFile);

            FileOutputStream stream = new FileOutputStream(tempFile);
            stream.write(bytes, 0, bytes.length);
            stream.close();

            return tempFile;
        } catch (Exception e) {
            return null;
        }
    }

    public static Bitmap readBitmapFromLocalFile(String directory, int width, int height,
                                                 String bitmapConfigString, String filename) {
        try {
            File directoryFile = new File(directory);
            File file = new File(directoryFile, filename);
            FileInputStream stream = new FileInputStream(file);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int bytesRead;
            while ((bytesRead = stream.read(b)) != -1) {
                bos.write(b, 0, bytesRead);
            }
            byte[] bytes = bos.toByteArray();
            stream.close();
            bos.close();

            Bitmap.Config bitmapConfig = Bitmap.Config.valueOf(bitmapConfigString);
            Bitmap bitmap = Bitmap.createBitmap(width, height, bitmapConfig);
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            bitmap.copyPixelsFromBuffer(buffer);

            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }

    public static File getLocalFile(BaseFragmentActivity activity, String filename) {
        try {
            File file = new File(activity.getBaseContext().getFilesDir(), filename);
            return file;
        } catch (Exception e) {
            return null;
        }

    }
}
