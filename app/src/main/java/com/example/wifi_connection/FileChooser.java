package com.example.wifi_connection;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

public class FileChooser {

    public static String getFilePathFromUri(Context context, Uri uri) {
        String filePath = null;
        String[] projection = {OpenableColumns.DISPLAY_NAME};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int columnIndex = cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME);
            cursor.moveToFirst();
            filePath = cursor.getString(columnIndex);
            cursor.close();
        }
        return filePath;
    }
}

