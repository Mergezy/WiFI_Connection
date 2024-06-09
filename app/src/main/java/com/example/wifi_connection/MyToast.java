package com.example.wifi_connection;

import android.content.Context;
import android.widget.Toast;

public class MyToast {
    private Context context;
    public MyToast(Context context){
        this.context = context;
    }
    public void showText(String msg) {
        Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show();
    }
}
