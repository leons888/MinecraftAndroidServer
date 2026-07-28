package com.bdscontrol.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

public final class ServerService extends Service {
    private static final String CHANNEL="bds_runtime";
    @Override public void onCreate(){super.onCreate();if(Build.VERSION.SDK_INT>=26){NotificationManager n=getSystemService(NotificationManager.class);n.createNotificationChannel(new NotificationChannel(CHANNEL,"BDS runtime",NotificationManager.IMPORTANCE_LOW));}}
    @Override public int onStartCommand(Intent intent,int flags,int id){Notification.Builder b=Build.VERSION.SDK_INT>=26?new Notification.Builder(this,CHANNEL):new Notification.Builder(this);b.setContentTitle("BDS Control").setContentText("Server runtime active").setSmallIcon(android.R.drawable.stat_sys_download_done).setOngoing(true);startForeground(12,b.build());return START_STICKY;}
    @Override public IBinder onBind(Intent intent){return null;}
}
