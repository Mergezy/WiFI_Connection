package com.example.wifi_connection;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int PICK_FILE_REQUEST = 1;
    private ServerThread serverThread;
    private TextView statusTextView;
    private String ipInputStr = "";
    private MyToast mainToast = new MyToast(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView ipView = findViewById(R.id.ipView);
        statusTextView = findViewById(R.id.statusTextView);
        EditText ipInput = findViewById(R.id.ipInput);
        ipInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                ipInputStr = s.toString();
            }
        });

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
                if(ipInputStr == ""){
                    mainToast.showText("ip не может быть пустой");
                    return;
                }
                boolean bol = Pattern.matches("^(((0)?|1(\\d?\\d)?|2([0-4]?\\d|5[0-5]))\\.){3}((0)?|1(\\d?\\d)?|2([0-4]?\\d|5[0-5]))$",ipInputStr);
                if(bol){
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");
                    startActivityForResult(intent, PICK_FILE_REQUEST);
                }else {
                    mainToast.showText("ip некорректный");
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == PICK_FILE_REQUEST && data != null) {
            Uri uri = data.getData();
            String selectedFilePath = getFileFromUri(this, uri);

            if (selectedFilePath != null) {
                updateStatusTextView("Фактический путь к файлу: " + selectedFilePath);
                sendFile(selectedFilePath);
            } else {
                mainToast.showText("Ошибка: Не удалось получить путь к файлу");
            }
        }
    }

    private String getFileFromUri(Context context, Uri uri) {
        File tempFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "tempFile");
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             OutputStream outputStream = Files.newOutputStream(tempFile.toPath())) {

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
                    Socket socket = new Socket(ipInputStr, 12345);
                    OutputStream outputStream = socket.getOutputStream();
                    File file = new File(filePath);
                    InputStream fileInputStream = Files.newInputStream(file.toPath());
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