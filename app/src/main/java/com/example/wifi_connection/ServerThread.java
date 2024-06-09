package com.example.wifi_connection;

import android.content.Context;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerThread extends Thread {
    private Context context;
    private volatile boolean running = true;
    private static final String FILE_PATH = "received_file";  // Adjust the path as needed

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
                FileOutputStream fileOutputStream = new FileOutputStream(context.getExternalFilesDir(null) + "/" + FILE_PATH);
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                }
                fileOutputStream.close();
                inputStream.close();
                clientSocket.close();

                // Логируем информацию о полученном файле и его пути
                String receivedFilePath = context.getExternalFilesDir(null) + "/" + FILE_PATH;
                String logMessage = "Файл успешно принят и сохранен: " + receivedFilePath;
                Log.d("ServerThread", logMessage);
            }
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
