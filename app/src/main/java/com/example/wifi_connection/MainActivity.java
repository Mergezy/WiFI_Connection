package com.example.wifi_connection;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int PICK_FILE_REQUEST = 1;
    private ServerThread serverThread;
    private String selectedFilePath;
    private TextView statusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView ipView = findViewById(R.id.ipView);
        statusTextView = findViewById(R.id.statusTextView);

        Button hostBtn = findViewById(R.id.hostBtn);
        hostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ipAddress = NetworkUtils.getIPAddress(v.getContext());
                ipView.setText(ipAddress);
                serverThread = new ServerThread(MainActivity.this);
                serverThread.start();
                updateStatusTextView("Сервер запущен. IP адрес: " + ipAddress);
            }
        });

        Button clientBtn = findViewById(R.id.clientBtn);
        clientBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent, PICK_FILE_REQUEST);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == PICK_FILE_REQUEST && data != null) {
            Uri uri = data.getData();
            selectedFilePath = getFileFromUri(this, uri);

            if (selectedFilePath != null) {
                updateStatusTextView("Фактический путь к файлу: " + selectedFilePath);
                sendFile(selectedFilePath);
            } else {
                updateStatusTextView("Ошибка: Не удалось получить путь к файлу");
            }
        }
    }

    private String getFileFromUri(Context context, Uri uri) {
        File tempFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "tempFile");
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             OutputStream outputStream = new FileOutputStream(tempFile)) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            return tempFile.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "Ошибка при получении файла из URI", e);
            return null;
        }
    }

    private void sendFile(String filePath) {
        updateStatusTextView("Отправка файла...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket("192.168.43.92", 12345);
                    OutputStream outputStream = socket.getOutputStream();
                    File file = new File(filePath);
                    InputStream fileInputStream = new FileInputStream(file);
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    outputStream.flush();
                    fileInputStream.close();
                    socket.close();
                    updateStatusTextView("Файл успешно отправлен");
                } catch (IOException e) {
                    Log.e(TAG, "Ошибка при отправке файла", e);
                    updateStatusTextView("Ошибка: Не удалось отправить файл");
                }
            }
        }).start();
    }

    private void updateStatusTextView(final String status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statusTextView.append("\n" + status);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serverThread != null) {
            serverThread.stopServer();
        }
    }
}
