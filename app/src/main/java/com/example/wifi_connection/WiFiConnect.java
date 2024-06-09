package com.example.wifi_connection;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

public class WiFiConnect {
    private WifiManager wifiManager;

    public WifiManager WifiConnect(Context context) {
        return wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    public boolean connectToWifi(String ssid, String password) {
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }

        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = String.format("\"%s\"", ssid);
        wifiConfig.preSharedKey = String.format("\"%s\"", password);

        int netId = wifiManager.addNetwork(wifiConfig);
        if (netId == -1) {
            Log.e("WifiConnect", "Failed to add network configuration!");
            return false;
        }

        wifiManager.disconnect();
        boolean enabled = wifiManager.enableNetwork(netId, true);
        boolean reconnected = wifiManager.reconnect();

        return enabled && reconnected;
    }
}

