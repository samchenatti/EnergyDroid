package com.example.just_me.testapp;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.eac.testapp.R;

public class MainActivity extends AppCompatActivity {
    private String serverIP;
    private int serverPort;
    private int requestDelay;
    private int requestsPerService;
    private String socketType;
    private String serverURL;
    private TextView textView;
    private PendingIntent pendingIntent;
    private AlarmManager alarmManager;
    private boolean alarmIsAlive;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set the default values
        serverIP     = "192.168.0.19";
        serverPort   = 49555;
        requestDelay = 1000;

        //Load configurations file (ip, server port, request delay)
        loadConfig();

        //Define switch action
        Switch toggle = (Switch) findViewById(R.id.switchAlarm);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startAlarm();
                } else {
                    stopAlarm();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    //Configuration menu
    public boolean onOptionsItemSelected(MenuItem item) {
        final EditText editTextServerIP;
        final EditText editTextServerPort;
        final EditText editTextRequestDelay;
        final EditText editTextPackagePerRequest;
        final EditText editTextServerURL;
        final AlertDialog dialog;
        final RadioGroup radioGroup;

        Button buttonApply;

        //Build an inflater and inflates the dialog
        LayoutInflater inflater = getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialoglayout);

        //Get the components
        buttonApply = (Button) dialoglayout.findViewById(R.id.buttonApply);
        editTextServerIP = (EditText) dialoglayout.findViewById(R.id.editServerIP);
        editTextServerPort = (EditText) dialoglayout.findViewById(R.id.editServerPort);
        editTextRequestDelay = (EditText) dialoglayout.findViewById(R.id.editTextRequestDelay);
        editTextPackagePerRequest = (EditText) dialoglayout.findViewById(R.id.editTextPackagePerRequest);
        editTextServerURL = (EditText) dialoglayout.findViewById(R.id.editTextServerURL);
        radioGroup = (RadioGroup) dialoglayout.findViewById(R.id.radioGroup);


        editTextRequestDelay.setText(Integer.toString(requestDelay), TextView.BufferType.EDITABLE);
        editTextPackagePerRequest.setText(Integer.toString(requestsPerService), TextView.BufferType.EDITABLE);
        editTextServerPort.setText(Integer.toString(serverPort), TextView.BufferType.EDITABLE);
        editTextServerIP.setText(serverIP, TextView.BufferType.EDITABLE);
        editTextServerURL.setText(serverURL, TextView.BufferType.EDITABLE);

        //Build the dialog
        dialog = builder.create();

        //Defines button apply action
        buttonApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Get the data
                serverIP = editTextServerIP.getText().toString();
                serverPort = Integer.parseInt(editTextServerPort.getText().toString());
                requestDelay = Integer.parseInt((editTextRequestDelay.getText().toString()));
                requestsPerService = Integer.parseInt((editTextPackagePerRequest).getText().toString());
                serverURL = editTextServerURL.getText().toString();

                switch(radioGroup.getCheckedRadioButtonId()){
                    case R.id.radioButtonEcono:
                        socketType = "econo";
                        Log.v("teste", "econo");
                        break;
                    case R.id.radioButtonStand:
                        Log.v("teste", "stand");
                        socketType = "stand";
                        break;
                }

                editTextRequestDelay.setText(Integer.toString(requestDelay), TextView.BufferType.EDITABLE);
                editTextServerPort.setText(Integer.toString(serverPort), TextView.BufferType.EDITABLE);
                editTextServerIP.setText(serverIP, TextView.BufferType.EDITABLE);
                editTextPackagePerRequest.setText(Integer.toString(requestsPerService), TextView.BufferType.EDITABLE);
                editTextServerURL.setText(serverURL, TextView.BufferType.EDITABLE);

                dialog.hide();

                //Save the data to config file
                saveConfig();
            }
        });


        dialog.show();

        return true;
    }

    //Load config from sharedPreferences
    private void loadConfig(){
        SharedPreferences settings = getSharedPreferences("config", 0);
        boolean silent = settings.getBoolean("silentMode", false);

        serverIP           = settings.getString("serverIP", "192.168.0.19");
        serverURL          = settings.getString("serverURL", "http://www.google.com.br");
        serverPort         = settings.getInt("serverPort", 49555);
        requestDelay       = settings.getInt("requestDelay", 5);
        requestsPerService = settings.getInt("requestsPerService", 1);

    }

    //Save config on sharedPreferences
    private void saveConfig(){
        SharedPreferences settings = getSharedPreferences("config", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("serverIP", serverIP);
        editor.putString("serverURL", serverURL);
        editor.putString("socketType", socketType);
        editor.putInt("serverPort", serverPort);
        editor.putInt("requestDelay", requestDelay);
        editor.putInt("requestsPerService", requestsPerService);


        editor.commit();
    }


    //Schedule an inexact alarm. The subsequent alarms are schedule through app service
    private void startAlarm(){
        Toast.makeText(this, "STARTED", Toast.LENGTH_SHORT).show();
        Intent alarmIntent = new Intent(MainActivity.this, Receiver.class);

        pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, alarmIntent, 0);

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        //alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 100000, pendingIntent);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 10000, pendingIntent);

    }

    /*Cancel the alarm, if the service is running*/
    private void stopAlarm() {
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}
