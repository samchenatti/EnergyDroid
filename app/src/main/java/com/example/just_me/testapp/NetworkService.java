package com.example.just_me.testapp;

import eac.energylib.EnergySocket;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Calendar;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class NetworkService extends Service{
    private boolean eacSocket;
    private String serverIP;
    private String response;
    private int serverPort;
    private int requestsPerService;
    private int requestDelay;
    private String socketType;

    @Override
    public IBinder onBind(Intent arg0){
        return null;
    }

    @Override
    public void onCreate(){
        super.onCreate();
    }

    @SuppressWarnings("static-access")

    //First called by the receiver
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v("Log", "Service started");

        //Load the flags and addresses
        loadConfig();
        Log.v("Log", "Config loaded");

        //Executes the given number of connections
        for(int i = 0; i < requestsPerService; i++) {
            ServerRequest serverRequest = new ServerRequest();
            serverRequest.executeOnExecutor(new ThreadPoolExecutor(10, 128, 1, TimeUnit.SECONDS,
                    sPoolWorkQueue, sThreadFactory));
            Log.v("Log", "Criou e executou a thread");
        }

        /*A small hack that allows the app to schedule the next call to the service on an exact
         moment*/
        setNext();
        return START_NOT_STICKY;
    }

    /*This little piece of code allows the app to run more than the standard number of AsyncTasks*/
    private static final BlockingQueue<Runnable> sPoolWorkQueue = new LinkedBlockingQueue<Runnable>(128);
    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "AsyncTask #" + mCount.getAndIncrement());
        }
    };

    /*Schedule the next alarm*/
    private void setNext(){
        PendingIntent pendingIntent;
        AlarmManager alarmManager;

        Intent alarmIntent = new Intent(NetworkService.this, Receiver.class);
        pendingIntent = PendingIntent.getBroadcast(NetworkService.this, 0, alarmIntent, 0);

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + requestDelay,
                pendingIntent);

    }

    /*Load the config from sharedPreferences*/
    private void loadConfig(){
        SharedPreferences settings = getSharedPreferences("config", 0);
        boolean silent = settings.getBoolean("silentMode", false);
        eacSocket = settings.getBoolean("eacSocketMode", true);

        serverIP = settings.getString("serverIP", "192.168.0.19");
        serverPort = settings.getInt("serverPort", 49555);
        requestDelay = settings.getInt("requestDelay", 1000);
        requestsPerService = settings.getInt("requestsPerService", 1);
        socketType = settings.getString("socketType", "econo");

        Log.v("config", socketType);
    }

    public class ServerRequest extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... params) {
            Log.v("AsyncTaskLog", "On thread " + this.toString());
            try {
                //Creates the socket
                Socket s;


                if (socketType.equals("econo")){
                    s = new EnergySocket("App1");
                }else{
                    s = new Socket();
                }

                //Connects the socket
                s.connect((SocketAddress) new InetSocketAddress(serverIP, serverPort));
                Log.v("AsyncTaskLog", "Trying the connection to " + serverIP + ":" + serverPort);


                //Sends the request string (http protocol) to the remote server
                PrintWriter pw = new PrintWriter(s.getOutputStream());
                pw.print("APP1......................................");
                pw.flush();
                Log.v("AsyncTaskLog", "Sent the request");


                /*This was the request message before we began to use a dedicated server*/
                //Makes the request to a remote server
                /*
                PrintWriter pw = new PrintWriter(s.getOutputStream());
                pw.print("GET / HTTP/1.1\r\n");
                pw.print("Host: google.com\r\n\r\n");
                pw.flush();
                Log.v("AsyncTaskLog", "Sent the request");
                */

                //Creates a buffer to read data from socket
                BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
                char[] r = new char[513];
                br.read(r, 0, 512);
                System.out.println(r);

                //Stores the response to be written on the local log
                response = new String(r);

                br.close();
                s.close();
                pw.close();

            } catch (Exception e){
                Log.v("ThreadLog", "Can't connect " + e.toString());
                response = "Can't connect " + e.toString();
            }
            Log.v("AsyncTaskLog", "Thread morta " + this.toString());

            /*Uncomment if you want to use the local log service*/
            //logRegister();

            return null;
        }

        protected void onPostExecute(Long result) {
            Log.v("AsyncTaskLog", "Post");
        }

    }

    /*Adds info to the con_log.txt file
      Do not use it if you have a log system on the server side.*/
    private void logRegister(){
        String date = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());

        BufferedWriter out;

        try {

            FileWriter fileWriter= new FileWriter("/" + "sdcard/con_log.txt", true);

            out = new BufferedWriter(fileWriter);

            out.write("["+date+"] " + response + "\n");

            out.close();

        }catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}