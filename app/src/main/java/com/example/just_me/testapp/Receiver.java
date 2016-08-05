package com.example.just_me.testapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import eac.energylib.EnergySocket;


public class Receiver extends BroadcastReceiver {

    @Override
    public void onReceive (Context context, Intent intent){
        Log.v("Receiver", "Received");
        //Start the service
        Intent service = new Intent(context, NetworkService.class);
        context.startService(service);
        Log.v("Receiver", "Leaving receiver");

        //Agenda a proxima requisicao
        PendingIntent pendingIntent;
        AlarmManager alarmManager;

        Intent alarmIntent = new Intent(context, Receiver.class);
        pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);

        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 10000,
                pendingIntent);
    }
}
