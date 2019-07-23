package com.xyoye.smbplayhelper.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.xyoye.smbplayhelper.R;
import com.xyoye.smbplayhelper.smb.SmbServer;

/**
 * Created by xyoye on 2019/7/23.
 */

public class SmbService extends Service {
    private int NOTIFICATION_ID = 1001;

    private SmbServer smbServer = null;
    private NotificationManager notificationManager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("com.xyoye.smbplayhelper.smbservice.playchannel", "SMB服务", NotificationManager.IMPORTANCE_LOW);
            channel.enableVibration(false);
            channel.setVibrationPattern(new long[]{0});
            channel.enableLights(false);
            channel.setSound(null, null);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
        startForeground(NOTIFICATION_ID, buildNotification());
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        smbServer = new SmbServer();
        smbServer.start();

    }

    private Notification buildNotification() {
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("SmbPlayHelper")
                .setContentText("已开启SMB服务")
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setContentIntent(null)
                .setWhen(System.currentTimeMillis())
                .setDefaults(NotificationCompat.FLAG_ONLY_ALERT_ONCE)
                .setVibrate(new long[]{0})
                .setSound(null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId("com.xyoye.smbplayhelper.smbservice.playchannel");
        }
        Notification notify = builder.build();
        notify.flags = Notification.FLAG_FOREGROUND_SERVICE;
        return notify;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        notificationManager.cancel(NOTIFICATION_ID);
        if (smbServer != null){
            smbServer.stopSmbServer();
        }
    }
}