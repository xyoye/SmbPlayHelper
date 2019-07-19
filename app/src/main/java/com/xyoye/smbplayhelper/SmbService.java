package com.xyoye.smbplayhelper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.xyoye.smbplayhelper.smb.SmbServer;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by xyoye on 2018/11/22.
 */

public class SmbService extends Service {
    private int NOTIFICATION_ID = 2;

    private SmbServer smbServer = null;
    private NotificationManager notificationManager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //创建NotificationChannel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("com.xyoye.smbjdemo.smbservice.playchannel", "SMB服务", NotificationManager.IMPORTANCE_LOW);
            channel.enableVibration(false);
            channel.setVibrationPattern(new long[]{0});
            channel.enableLights(false);
            channel.setSound(null, null);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
        startForeground(NOTIFICATION_ID, buildNotification(intent));
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
        smbServer = new SmbServer();
        smbServer.start();

    }

    private Notification buildNotification(Intent oldIntent) {
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("SmbjDemo")
                .setContentText("已开启SMB服务")
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setContentIntent(null)
                .setWhen(System.currentTimeMillis())
                .setDefaults(NotificationCompat.FLAG_ONLY_ALERT_ONCE)
                .setVibrate(new long[]{0})
                .setSound(null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId("com.xyoye.smbjdemo.smbservice.playchannel");
        }
        Notification notify = builder.build();
        notify.flags = Notification.FLAG_FOREGROUND_SERVICE;
        return notify;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        notificationManager.cancel(NOTIFICATION_ID);
        if (smbServer != null){
            smbServer.stopSmbServer();
        }
    }
}