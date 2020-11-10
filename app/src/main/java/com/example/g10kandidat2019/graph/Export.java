package com.example.g10kandidat2019.graph;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.util.Base64;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import static androidx.constraintlayout.widget.Constraints.TAG;

class Export {

    String filename;
    Context context;

    public Export(String filename, Context context) {
        this.filename = filename.substring(filename.lastIndexOf(File.separator)+1) + ".txt";
        this.context = context;
    }

    public String createStringFromList(DataPointList inputList){
        StringBuilder OutDataWriter = new StringBuilder();
        for (DataPoint dp : inputList.getDataPointList()) {
            OutDataWriter.append(String.valueOf(dp.getX()));
            OutDataWriter.append(",");
            OutDataWriter.append(String.valueOf(dp.getY()));
            OutDataWriter.append(",");
            OutDataWriter.append(String.valueOf(dp.getUs()));
            //String bitmap = BitmapToString(dp.getImg());
            //if(bitmap != null) {
            //    OutDataWriter.append(",");
            //    OutDataWriter.append(bitmap);
            //}
            OutDataWriter.append("\n");
        }
        return OutDataWriter.toString();
    }

    /**
     * Saves a String  into a text file on the SD-card so that it can be moved to other places.
     * @param data The string to save save
     * @return the URI to the file if successful,null otherwise
     */
    public Uri save(String data) {
        if (isExternalStorageWritable()) {
            try {

                File parentDirectory = new File(Environment.getExternalStorageDirectory(), "TrackApp");
                if (!parentDirectory.mkdirs() && !parentDirectory.exists()) {
                    Log.e("Error", "Directory not created");
                }

                File exportedFile = new File(parentDirectory,filename);
                int counter = 2;
                while (exportedFile.exists()){
                    int secondIndex = !filename.contains("(") ? 0:filename.lastIndexOf(".")-filename.lastIndexOf("(");
                    filename = filename.substring(0,filename.lastIndexOf(".")-secondIndex) + "(" + counter + ").txt";
                    exportedFile = new File(parentDirectory,filename);
                    counter += 1;
                }
                FileOutputStream fos = new FileOutputStream(exportedFile.getAbsolutePath());
                OutputStreamWriter OutDataWriter = new OutputStreamWriter(fos);
                OutDataWriter.write("");

                OutDataWriter.append(data);

                OutDataWriter.close();

                fos.flush();
                fos.close();
                System.out.println(exportedFile.getAbsolutePath());
                System.out.println(exportedFile.getPath());
                Uri uri;
                try {
                    uri = FileProvider.getUriForFile(context,
                            "com.example.g10kandidat2019.fileprovider",
                            exportedFile);
                } catch (Exception e) {
                    Log.d(TAG,"file exception,",e);
                    uri = Uri.fromFile(exportedFile);
                }
                return uri;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Checks whether or not the external storage is available for read and write.
     * @return true if the external storage is both writable and readable, false otherwise
     */
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * Takes a Bitmap and converts it into a string.
     * @param bitmap the bitmap to convert
     * @return The resulting String
     */
    private static String BitmapToString(Bitmap bitmap) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] b = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(b, Base64.DEFAULT);

        } catch (NullPointerException e) {
            return null;
        } catch (OutOfMemoryError e) {
            return null;
        }
    }

    /**
     * Takes a String and attempts to convert it into a bitmap.
     * @param encodedString the String to convert
     * @return the resulting bitmap if successful otherwise null
     */
    public static Bitmap StringToBitmap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
        } catch (NullPointerException e) {
            e.getMessage();
            return null;
        } catch (OutOfMemoryError e) {
            return null;
        }
    }
}
