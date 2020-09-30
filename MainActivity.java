package com.example.timer;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import java.util.UUID;
import java.util.Set;
import android.widget.Toast;

public class MainActivity extends Activity {

    Button btnStart, btnReset, btnStop, btnConnect;
    TextView txtTimer;
    TextView textView,textView2;
    private final String DEVICE_ADDRESS="98:D3:11:FC:52:5E";
    Handler customHandler = new Handler();

    private BluetoothDevice device;
    private BluetoothSocket btSocket = null;
    Set<BluetoothDevice> pairedDevices;
    private final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    private OutputStream outputStream;
    private InputStream inputStream;

    boolean stopThread;
    byte buffer[];

    long startTime = 0, timeInMilliseconds = 0, timeSwapBuff = 0, updateTime = 0;

    Runnable updateTimerThread = new Runnable() {
        @Override
        public void run() {

            timeInMilliseconds = SystemClock.uptimeMillis()-startTime;
            updateTime = timeSwapBuff + timeInMilliseconds;
            int secs = (int)(updateTime/1000);
            int mins = secs/60;
            secs%=60;
            int milliseconds = (int)(updateTime%1000);
            txtTimer.setText(""+mins+":"+String.format("%02d", secs)+":"+String.format("%03d", milliseconds));
            customHandler.postDelayed(this , 0);
        }
    };

    Runnable resetTimerThread = new Runnable() {
        @Override
        public void run() {
            timeInMilliseconds = 0;
            updateTime = 0;
            timeSwapBuff = 0;
            txtTimer.setText(""+0+":"+String.format("00")+":"+String.format("000"));
            customHandler.postDelayed(this , 0);
        }
    };

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStart = (Button) findViewById(R.id.btnStart);
        btnReset = (Button) findViewById(R.id.btnReset);
        btnStop = (Button) findViewById(R.id.btnStop);
        btnConnect = (Button) findViewById(R.id.btnConnect);
        txtTimer = (TextView) findViewById(R.id.Running_Timer);
        textView = (TextView) findViewById(R.id.textView1);
        textView2 = (TextView) findViewById(R.id.textView2);


        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTime = SystemClock.uptimeMillis();
                customHandler.postDelayed(updateTimerThread, 0);
                beginListenForData();
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timeSwapBuff += timeInMilliseconds;
                customHandler.removeCallbacks(updateTimerThread);
                customHandler.removeCallbacks(resetTimerThread);
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTime = 0;
                customHandler.postDelayed(resetTimerThread, 0);
            }
        });

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onStart(view);
                checkAlignment();
            }
        });



    }
    public boolean BTinit()
    {
        boolean found=false;
        BluetoothAdapter bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(),"Device doesnt Support Bluetooth",Toast.LENGTH_SHORT).show();
        }
        if(!bluetoothAdapter.isEnabled())
        {
            Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableAdapter, 0);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        if(bondedDevices.isEmpty())
        {
            Toast.makeText(getApplicationContext(),"Please Pair the Device first",Toast.LENGTH_SHORT).show();
        }
        else
        {
            for (BluetoothDevice iterator : bondedDevices)
            {
                if(iterator.getAddress().equals(DEVICE_ADDRESS))
                {
                    device=iterator;
                    found = true;
                    break;
                }
            }
        }
        return found;
    }

    public boolean BTconnect() {
        boolean connected = true;
        try {
            btSocket = device.createRfcommSocketToServiceRecord(myUUID);
            btSocket.connect();
        } catch (IOException e) {
            e.printStackTrace();
            connected = false;
        }
        if (connected) {
            try {
                outputStream = btSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                inputStream = btSocket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return connected;
    }

    void beginListenForData()
    {
        final Handler handler = new Handler();
        stopThread = false;
        buffer = new byte[1024];
        Thread thread  = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopThread)
                {
                    try
                    {
                        int byteCount = inputStream.available();
                        if(byteCount > 0)
                        {
                            byte[] rawBytes = new byte[byteCount];
                            inputStream.read(rawBytes);
                            final String string=new String(rawBytes,"UTF-8");
                            if (string.contains("s"))
                            {
                                timeSwapBuff += timeInMilliseconds;
                                customHandler.removeCallbacks(updateTimerThread);
                                customHandler.removeCallbacks(resetTimerThread);
                            }
                                handler.post(new Runnable() {
                                    public void run()
                                    {
                                        return;
                                    }
                            });

                        }
                    }
                    catch (IOException ex)
                    {
                        stopThread = true;
                    }
                }
            }
        });

        thread.start();
    }

    void checkAlignment() //not working
    {
        final Handler handler = new Handler();
        stopThread = false;
        buffer = new byte[1024];

        Thread thread  = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopThread)
                {
                    try
                    {
                        int byteCount = inputStream.available();
                        if(byteCount > 0)
                        {
                            byte[] rawBytes = new byte[byteCount];
                            inputStream.read(rawBytes);
                            final String string=new String(rawBytes,"UTF-8");
                            if (string.contains("A"))
                            {
                                handler.post(new Runnable() {
                                    public void run()
                                    {
                                        int numberofAligned = 1;
                                        if (numberofAligned == 1)
                                        {
                                            textView2.setText("Aligned"); //second thread for alignment
                                            numberofAligned++;
                                        }
                                        return;
                                    }
                                });
                            }
                            else
                            {
                                handler.post(new Runnable() {
                                    public void run()
                                    {
                                        int numberofNotAligned = 1;
                                        if (numberofNotAligned == 1)
                                        {
                                            textView2.setText("Not Aligned");
                                            numberofNotAligned++;
                                        }
                                        return;
                                    }
                                });
                            }

                        }
                    }
                    catch (IOException ex)
                    {
                        stopThread = true;
                    }
                }
            }
        });

        thread.start();
    }

    public void onStart(View view) {
        if(BTinit())
        {
            if(BTconnect())
            {

                textView.append("\nConnection Opened!\n");
            }
        }
    }


}
