package eac.energylib;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Messenger;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

//It is like a queue, but with a cooler name (imo)
public class SocketHolder {
    private static final           SocketHolder INSTANCE = new SocketHolder();
    private static final Queue<EnergySocket> socketQueue = new LinkedList();
    private static final             int count = 0;

    private static int         maxSockets;

    /** Messenger for communicating with the service. */
    Messenger mService = null;
    /** Flag indicating whether we have called bind on the service. */
    boolean mBound;

    private SocketHolder(){
        Log.v("LogSocketHolder", "We have a SocketHolder");
        maxSockets  = 5;
    }

    //Registers a socket in the queue
    public static void registerSocket(EnergySocket s){
        socketQueue.add(s);
        Log.v("LogSocketHolder", "An EnergySocket was registered in the queue[" + socketQueue.size()
                + "] Socket: " + s.toString());
        //verifyQueue();
    }

    //Apply the polices. For now it only counts a max number of threads and releases then all
    private static void verifyQueue(){
        if(socketQueue.size() == maxSockets) releaseAll();
    }

    //Release all threads. Only called by the verfyQueue method
    public static void releaseAll(){
        Log.e("LogSocketHolder", "Policy was reached. Releasing all sockets");
        Log.v("SocketHolder", "Releasing: " + socketQueue.size());
         //It is kind of ugly, but it is the only way to remove the itens while iterating over the queue
         Iterator<EnergySocket> iterator = socketQueue.iterator();
         while (iterator.hasNext()){
            EnergySocket s = iterator.next();
             s.unlock();
            //Remove it from the queue
            iterator.remove();
         }

        Log.v("LogSocketHolder", "All EnergySockets released");
    }

    public static synchronized SocketHolder getInstance(){ return INSTANCE; }


    public static class Receiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            SocketHolder.getInstance().releaseAll();

        }
    }
}
