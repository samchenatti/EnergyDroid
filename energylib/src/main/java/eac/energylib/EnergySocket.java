package eac.energylib;

import android.util.Log;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;


public class EnergySocket extends Socket {
    private String  label;
    private boolean lock = true;

    //Create an unconnected socket implementation
    public EnergySocket(String l) throws IOException {
        Log.v("LogEnergySocket", "EnergySocket created");
        label = l;
        SocketHolder.registerSocket(this);

        this.lock();
    }

    //Lock this socket
    private synchronized void lock(){
        Log.v("LogEnergySocket", "EnergySocket locked");
        //Uses the Object class's native lock method
        while(lock){
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //Unlock this socket
    public synchronized void unlock(){
        lock = false;
        notify();
        Log.v("LogEnergySocket", "EnergySocket released");
    }

    public String toString(){
        return new String("EnergySocket-> " + label);
    }
}


