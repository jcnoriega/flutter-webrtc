package com.cloudwebrtc.webrtc.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;


public class RTCAudioBackgroundService extends Service {

    public static final String CHANNEL_ID = "RTCAudiofForegroundService";
    public static final String START_ACTION = "com.cloudwebrtc.webrtc.utils.RTCAudioBackgroundService.START_SERVICE";
    public static final String STOP_ACTION = "com.cloudwebrtc.webrtc.utils.RTCAudioBackgroundService.STOP_SERVICE";

    public static final String SET_SPEAKERPHONE_ON = "setSpeakerPhoneOn";
    public static final String SET_MICROPHONE_MUTE = "setMicrophoneMute";

    public static final String NOTIFICATION_TITLE = "notificationTitle" ;
    public static final String NOTIFICATION_CONTENT = "notificationContent";


    private RTCAudioManager rtcAudioManager;

    @Override
    public void onCreate() {
        super.onCreate();
        rtcAudioManager = RTCAudioManager.create(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction() != null) {
            switch (intent.getAction()) {
                case START_ACTION:
                    start(intent);
                    break;
                case STOP_ACTION:
                    stop();
                    break;
            }
        }

        if (intent.hasExtra(SET_MICROPHONE_MUTE)) {
            final boolean mute = intent.getBooleanExtra(SET_MICROPHONE_MUTE, false);
            rtcAudioManager.setMicrophoneMute(mute);
        } else if (intent.hasExtra(SET_SPEAKERPHONE_ON)) {
            final boolean enable = intent.getBooleanExtra(SET_SPEAKERPHONE_ON, false);
            rtcAudioManager.setSpeakerphoneOn(enable);
        }
        return START_STICKY;
    }

    private void start(final Intent intent) {
        final String title = intent.getStringExtra(NOTIFICATION_TITLE);
        final String content = intent.getStringExtra(NOTIFICATION_CONTENT);

        createNotificationChannel();

        final PackageManager pm = getApplicationContext().getPackageManager();
        final Intent notificationIntent = pm.getLaunchIntentForPackage(getApplicationContext().getPackageName());
        final PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        final Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setOngoing(true)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(android.R.drawable.ic_menu_call)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);

        rtcAudioManager.start((selectedAudioDevice, availableAudioDevices) -> {
            //do nothing
        });
    }

    private void stop() {
        rtcAudioManager.stop();
        stopForeground(true);
        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}
