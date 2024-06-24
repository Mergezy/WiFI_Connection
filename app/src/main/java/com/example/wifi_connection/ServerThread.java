package com.example.wifi_connection;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerThread extends Thread {
    private Context context;
    private volatile boolean running = true;

    public ServerThread(Context context) {
        this.context = context;
    }

    public void stopServer() {
        running = false;
        interrupt();
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(12345);
            while (running) {
                Socket clientSocket = serverSocket.accept();
                InputStream inputStream = clientSocket.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                // Чтение метаданных (название файла и тип)
                String fileName = bufferedReader.readLine();
                if (fileName == null) {
                    Log.e("ServerThread", "Не удалось прочитать имя файла.");
                    continue;
                }

                // Определение пути к файлу
                String filePath = context.getExternalFilesDir(null) + "/" + fileName;
                FileOutputStream fileOutputStream = new FileOutputStream(filePath);

                // Чтение и запись данных файла
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                }

                fileOutputStream.close();
                inputStream.close();
                clientSocket.close();

                // Логирование информации о принятом файле и его пути
                String logMessage = "Файл успешно принят и сохранен: " + filePath;
                Log.d("ServerThread", logMessage);
            }
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
