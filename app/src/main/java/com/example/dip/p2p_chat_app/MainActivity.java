package com.example.dip.p2p_chat_app;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    int tempmargin = 400;
    Button btnOnOff, btnListen, btnSend, btnSave, btnChange, btnChoose, btnSendFile,btnCancel,btnConnect;
    ListView listView;
    String read_msg = "",trPortStr,trIpStr;
    EditText writeMsg, rcvPort, trPort, trIp, clrCode,userName;
    WifiManager wifiManager;
    //WifiP2pManager mManager;
   // WifiP2pManager.Channel mChannel;
    //BroadcastReceiver mReceiver;
   // IntentFilter mIntentFilter;
    static final int MESSAGE_READ = 1;
    ServerClass serverClass;
    ClientClass clientClass;
    Receive receive;
    String myIp, FILE_NAME, textFileData,userNameStr;
    private static final int EXTERNAL_STORAGE_CODE = 1;
    private static final int READ_CODE = 2;
    //private static final int READ_EXTERNAL_STORAGE_CODE = 3;
    LinearLayout chatWindow, messageContainer;
    int flag,serverFlag=0,connceted=0;
    TextView fileName,ip;
    ConstraintLayout wallpaper;
    Intent intent;
    ScrollView sv;
    MediaPlayer mp;
    int soundflg=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialWork();
        exqListener();

        //Permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                    || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
                requestPermissions(permissions, EXTERNAL_STORAGE_CODE);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case EXTERNAL_STORAGE_CODE: {
                if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(this, "Storage permission is required to store data", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menus,menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(connceted == 0){
            menu.getItem(0).setEnabled(true);
            menu.getItem(1).setEnabled(false);
            menu.getItem(2).setEnabled(true);
            menu.getItem(3).setEnabled(false);
            menu.getItem(4).setEnabled(true);
            menu.getItem(5).setEnabled(true);
            menu.getItem(6).setEnabled(true);
        }
        else{
            menu.getItem(0).setEnabled(true);
            menu.getItem(1).setEnabled(true);
            menu.getItem(2).setEnabled(true);
            menu.getItem(3).setEnabled(true);
            menu.getItem(4).setEnabled(true);
            menu.getItem(5).setEnabled(true);
            menu.getItem(6).setEnabled(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.onOff:
                if (wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(false);
                } else {
                wifiManager.setWifiEnabled(true);
                }
                return true;

            case R.id.saveMsg:
                String timeStamp = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(System.currentTimeMillis());
                save("(" + trIp.getText() + "--" + timeStamp + ")", read_msg);
                return true;

            case R.id.myIp:
                myIp = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
                Toast.makeText(this,myIp,Toast.LENGTH_LONG).show();
                return true;

            case R.id.choose_File:
                showFiles();
                return true;

            case R.id.resetbg:
                wallpaper.setBackgroundResource(R.drawable.backgroundgradient);
                return true;

            case R.id.onoffsound:
                if(soundflg==1){soundflg=0;
                Toast.makeText(this,"Notification Sound is off",Toast.LENGTH_LONG).show();}
                else {
                    soundflg = 1;
                    Toast.makeText(this,"Notification Sound is on", Toast.LENGTH_LONG).show();
                }
                return true;


            case R.id.mainPage:
                //wifiManager.setWifiEnabled(false);
                finish();
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                //startActivity(intent);
        }
        return true;
    }


    private void exqListener() {

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = writeMsg.getText().toString();
                flag = 1;
                send(trIp.getText().toString(), trPort.getText().toString(), msg);
            }
        });
        btnListen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listen(rcvPort.getText().toString());
            }
        });
        btnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = clrCode.getText().toString();
                flag = 2;
                send(trIp.getText().toString(), trPort.getText().toString(), msg);
            }
        });
        btnSendFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = fileName.getText().toString();
                flag = 3;
                send(trIp.getText().toString(), trPort.getText().toString(), msg);
                fileName.setVisibility(View.GONE);
                btnSendFile.setVisibility(View.GONE);
                btnCancel.setVisibility(View.GONE);
                btnChange.setVisibility(View.VISIBLE);
                btnSend.setVisibility(View.VISIBLE);
                writeMsg.setVisibility(View.VISIBLE);
                clrCode.setVisibility(View.VISIBLE);
            }
        });
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkIpAndPort(trIp.getText().toString(),trPort.getText().toString());
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textFileData="";
               fileName.setText("");
               fileName.setVisibility(View.GONE);
               btnSendFile.setVisibility(View.GONE);
               btnCancel.setVisibility(View.GONE);
                btnChange.setVisibility(View.VISIBLE);
                btnSend.setVisibility(View.VISIBLE);
                writeMsg.setVisibility(View.VISIBLE);
                clrCode.setVisibility(View.VISIBLE);
            }
        });
    }

    private void initialWork() {
        mp = MediaPlayer.create(this, R.raw.notification);
        sv = (ScrollView) findViewById(R.id.sv);
        userName = (EditText) findViewById(R.id.userName);
        intent = new Intent(this,MainActivity.class);
        wallpaper = (ConstraintLayout) findViewById(R.id.wallpaper);
        btnCancel = (Button) findViewById(R.id.cancel);
        btnConnect  = (Button) findViewById(R.id.connect);
        btnSendFile = (Button) findViewById(R.id.sendFile);
        fileName = (TextView) findViewById(R.id.fileName);
        messageContainer = (LinearLayout) findViewById(R.id.lr2);
        chatWindow = (LinearLayout) findViewById(R.id.chatWindow);
        btnChange = (Button) findViewById(R.id.change);
        btnListen = (Button) findViewById(R.id.listen);
        btnSend = (Button) findViewById(R.id.sendButton);
        writeMsg = (EditText) findViewById(R.id.writeMsg);
        clrCode = (EditText) findViewById(R.id.colorcode);
        trIp = (EditText) findViewById(R.id.targetIp);
        trPort = (EditText) findViewById(R.id.targetPort);
        rcvPort = (EditText) findViewById(R.id.receivePort);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
       // mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
       // mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);
       // mIntentFilter = new IntentFilter();
       // mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
    }

    void listen(String rcvPort) {
        if (rcvPort.isEmpty()) {
            Toast.makeText(this, "Give Receiving Port", Toast.LENGTH_SHORT).show();
            return;
        }
        if (userName.getText().toString().isEmpty()) {
            Toast.makeText(this, "Give a name", Toast.LENGTH_SHORT).show();
            return;
        }
        {
            serverClass = new ServerClass(Integer.parseInt(rcvPort));
            serverClass.start();
        }
    }

    void send(String trIp, String trPort, String msg) {
        if (msg.isEmpty()) {
            if (flag == 1)
                Toast.makeText(this, "Write something on the msg field", Toast.LENGTH_SHORT).show();
            if (flag == 2) {
                Toast.makeText(this, "Color code field is Empty/Invalid", Toast.LENGTH_SHORT).show();
            }
            if (flag == 3) {
                Toast.makeText(this, "Choose a Text file", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        //To keep track that this msg is for color change
        if (flag == 2) {
            msg = "ITSACOLORCHNGREQ" + msg;
            //Toast.makeText(this,msg,Toast.LENGTH_LONG).show();

        }
        if (flag == 3) {
            msg = "ITSATEXTFILE" + textFileData + "LATERISFILENAME" + msg;

        }
        {
            clientClass = new ClientClass(trIp, Integer.parseInt(trPort), msg);
            clientClass.start();
        }
    }

    void checkIpAndPort(String Ip, String Port) {
        if (serverFlag == 0) {
            Toast.makeText(this, "Start Server to Receive Messages", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (Ip.isEmpty()) {
            Toast.makeText(this, "Give Target Ip", Toast.LENGTH_SHORT).show();
            return;
        }
        if (Port.isEmpty()) {
            Toast.makeText(this, "Give Target Port", Toast.LENGTH_SHORT).show();
            return;
        }

        CheckConnection ck = new CheckConnection(Ip, Integer.parseInt(Port));
        ck.start();

    }

/*    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }*/

/*    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }*/


    private class Receive extends Thread {
        private Socket socket;
        private InputStream inputStream;

        public Receive(Socket skt) {
            socket = skt;
            try {
                inputStream = socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (socket != null) {
                try {
                    bytes = inputStream.read(buffer);
                    if (bytes > 0) {
                        byte[] readBuff = (byte[]) buffer;
                        final String msg = new String(readBuff, 0, bytes);

                        // To check if the msg is for color change(if the format is valid)
                        final String Code;
                        if (msg.indexOf("ITSACOLORCHNGREQ") != -1) {
                            Code = msg.substring(16);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(soundflg==1)
                                    {mp.start();}
                                    try {
                                        wallpaper.setBackgroundColor(Color.parseColor(Code));
                                    } catch (Exception e) {
                                        Toast.makeText(MainActivity.this, "Invalid Color code received", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else if (msg.indexOf("ITSATEXTFILE") != -1) {
                            final String name, remoteip;
                            final String data;
                            final String timeStamp = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(System.currentTimeMillis());
                            name = msg.substring(msg.indexOf("LATERISFILENAME") + 15, msg.indexOf(".txt"));
                            data = msg.substring(msg.indexOf("ITSATEXTFILE") + 12, msg.indexOf("LATERISFILENAME"));
                            remoteip = msg.substring(0, msg.indexOf("ITSATEXTFILE"));

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(soundflg==1)
                                    {mp.start();}
                                    TextView tv = createTrcv();
                                    tv.setText(remoteip  + "File reveived as : " + name + ".txt" + "\n"
                                            + "Saved as : " + name + "(" +trIp.getText() + "--" + timeStamp + ").txt");
                                    messageContainer.addView(tv);
                                    tv = createTimeTrcv();
                                    messageContainer.addView(tv);
                                    //sv.scrollTo(0, sv.getBottom());
                                    sv.fullScroll(View.FOCUS_DOWN);
                                    saveTotxtFile(name + "(" + trIp.getText() + "--" + timeStamp + ")", data);
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(soundflg==1)
                                    {mp.start();}
                                    TextView tv = createTrcv();
                                    tv.setText(msg);
                                    messageContainer.addView(tv);
                                    tv = createTimeTrcv();
                                    messageContainer.addView(tv);
                                    //sv.scrollTo(0, sv.getBottom());
                                    sv.fullScroll(View.FOCUS_DOWN);
                                }
                            });
                            //handler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                            Message message = Message.obtain();
                            message.obj =(msg);
                            handler1.sendMessage(message);
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Don't know what happened", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }
    }


    public class ServerClass extends Thread {
        Socket socket;
        ServerSocket serverSocket;
        InputStream is;
        int port;
        MainActivity act;

        public ServerClass(int port) {
            this.port = port;
          //  this.act = act;
        }

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(port);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        serverFlag = 1;
                        userNameStr=userName.getText().toString();
                        myIp = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
                        Toast.makeText(MainActivity.this, "Listening on Port : " + Integer.toString(port), Toast.LENGTH_SHORT).show();
                    }
                });

               while ((socket = serverSocket.accept()) != null)
                {

                    receive = new Receive(socket);
                    receive.start();
                }
            } catch (IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                            Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                });
               // e.printStackTrace();
            }
        }
    }


    public class ClientClass extends Thread {
        OutputStream outputStream;
        Socket socket;
        String trIp, msg;
        int trPort;
        MainActivity act;

        public ClientClass(String trIp, int trPort, String msg) {
            this.trIp = trIp;
            this.trPort = trPort;
            this.msg = msg;
       //     this.act = act;
            socket = new Socket();
        }

        @Override
        public void run() {
            try {
                socket.connect(new InetSocketAddress(trIp, trPort));
                outputStream = socket.getOutputStream();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Msg Sent to : " + trIp + ":" + Integer.toString(trPort), Toast.LENGTH_SHORT).show();
                    }
                });
                final String Code;
                if (msg.indexOf("ITSACOLORCHNGREQ") != -1) {

                    Code = msg.substring(16);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            clrCode.setText("");
                            try {
                                wallpaper.setBackgroundColor(Color.parseColor(Code));
                            } catch (Exception e) {
                                Toast.makeText(MainActivity.this, "Invalid Color code", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    outputStream.write((msg).getBytes());
                    socket.close();
                } else if (msg.indexOf("ITSATEXTFILE") != -1) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView tv = createTsend();
                            tv.setText("(You)<" + myIp + ">" + "\n\n" + "File sent : " + fileName.getText().toString());
                            fileName.setText("");
                            textFileData = "";
                            messageContainer.addView(tv);
                            tv = createTimeTsend();
                            messageContainer.addView(tv);
                            //sv.scrollTo(0, sv.getBottom());
                            sv.fullScroll(View.FOCUS_DOWN);
                        }
                    });
                    outputStream.write(("("+userNameStr+")<" + myIp + ">" + "\n\n" + msg).getBytes());
                    socket.close();

                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            writeMsg.setText("");
                            TextView tv = createTsend();
                            tv.setText("(You)<" + myIp + ">" + "\n\n" + msg);
                            messageContainer.addView(tv);
                            tv = createTimeTsend();
                            messageContainer.addView(tv);
                            //sv.scrollTo(0, sv.getBottom());
                            sv.fullScroll(View.FOCUS_DOWN);

                        }
                    });
                    Message message = Message.obtain();
                    message.obj = ("(You)<" + myIp + ">" + "\n\n" + msg);
                    handler1.sendMessage(message);
                    outputStream.write(("("+userNameStr+")<" + myIp + ">" + "\n\n" + msg).getBytes());
                    socket.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                            Toast.makeText(MainActivity.this, "Don't know what happened", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            return ;
        }

    }

    public class CheckConnection extends Thread {
        Socket socket;
        String Ip;
        int Port;
        MainActivity act;

        public CheckConnection(String trIp, int trPort) {
            this.Ip = trIp;
            this.Port = trPort;
            socket = new Socket();
        }

        @Override
        public void run() {
            try {

                socket.connect(new InetSocketAddress(Ip, Port));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                        rcvPort.setVisibility(View.GONE);
                        trIp.setVisibility(View.GONE);
                        userName.setVisibility(View.GONE);
                        trPort.setVisibility(View.GONE);
                        btnConnect.setVisibility(View.GONE);
                        btnListen.setVisibility(View.GONE);
                        chatWindow.setVisibility(View.VISIBLE);
                        btnChange.setVisibility(View.VISIBLE);
                        btnSend.setVisibility(View.VISIBLE);
                        writeMsg.setVisibility(View.VISIBLE);
                        clrCode.setVisibility(View.VISIBLE);
                        connceted = 1;
                    }
                });
                socket.close();
            } catch (IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Target Ip/Port is not valid", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            return ;
        }

    }
    // Log.d("What is this", "Waiting for client...");

    //Handler is used as a anchor point to two different thread (Can be handled in 2 ways)

 /*   Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_READ:
                    byte[] readBuff = (byte[]) msg.obj;
                    String tempMsg = new String(readBuff, 0, msg.arg1);
                    read_msg = read_msg + "\n "+ tempMsg;
                    break;
            }
            return true;
        }
    });*/

    Handler handler1 = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            read_msg = read_msg + "\n" + msg.obj.toString();
            return true;
        }
    });

    /*
         Save and SaveTotxtFile methods are used to save msgs into mobile storage
     */

    void save(String name, String data) {
        if (data.isEmpty()) {
            Toast.makeText(this, "There is no Text to save", Toast.LENGTH_SHORT).show();
            return;
        }
        saveTotxtFile(name, data);
    }

    void saveTotxtFile(String name, String data) {
        //FILE_NAME = myIp + trIp.getText();
        // String text = read_msg;
        try {
            File path = Environment.getExternalStorageDirectory();
            File dir = new File(path + "/P2p/");
            dir.mkdirs();
            FILE_NAME = name + ".txt";
            File file = new File(dir, FILE_NAME);
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(data);
            bw.close();
            Toast.makeText(this, FILE_NAME + " is saved in \n" + dir, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {

        }
    }


    String readText(String path) {
        File file = new File(Environment.getExternalStorageDirectory() + "/" + path);
        StringBuilder data = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                data.append(line);
                data.append("\n");
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data.toString();
    }


    TextView createTsend() {
        TextView tv = new TextView(this);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextColor(Color.WHITE);
        // tv.setTextColor(Color.BLUE);
        //tv.setBackgroundColor(Color.BLUE);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        params.setMargins(0,tempmargin,0,20);
        tempmargin=0;
        //params.weight = 1.0f;
        params.gravity = android.view.Gravity.RIGHT;
        tv.setLayoutParams(params);

        tv.setPadding(30,30,30,30);
        tv.setBackgroundResource(R.drawable.tv);
        return tv;
    }

    TextView createTimeTsend() {
        TextView tv = new TextView(this);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextColor(Color.BLACK);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        params.gravity = android.view.Gravity.RIGHT;
        tv.setLayoutParams(params);
        final String timeStamp = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(System.currentTimeMillis());
        tv.setText(timeStamp);
        //tv.setBackgroundResource(R.drawable.tv);
        return tv;
    }

    TextView createTimeTrcv() {
        TextView tv = new TextView(this);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextColor(Color.BLACK);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        params.gravity = android.view.Gravity.LEFT;
        tv.setLayoutParams(params);
        final String timeStamp = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(System.currentTimeMillis());
        tv.setText(timeStamp);
        //tv.setBackgroundResource(R.drawable.tv);
        return tv;
    }

    TextView createTrcv() {
        TextView tv = new TextView(this);
        tv.setTypeface(null, Typeface.BOLD);
        // tv.setTextColor(Color.WHITE);
        //tv.setBackgroundColor(Color.GREEN);
        //tv.setPadding(10,0,300,0);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        params.setMargins(0,tempmargin,0,20);
        tempmargin=0;
        //params.weight = 1.0f;
        params.gravity = android.view.Gravity.LEFT;
        tv.setLayoutParams(params);
        tv.setLayoutParams(params);
        tv.setTextColor(Color.BLACK);
        tv.setPadding(30,30,30,30);
        tv.setBackgroundResource(R.drawable.tv1);
        return tv;
    }
/*    void showFiles(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                requestPermissions(permissions, READ_EXTERNAL_STORAGE_CODE);
            } else {
                show();
            }
        } else {
            show();
        }
    }*/

    public void showFiles() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/*");
        startActivityForResult(intent, READ_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == READ_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                String path = uri.getPath();
                Toast.makeText(this, path, Toast.LENGTH_LONG).show();


                textFileData = readText(path.substring(path.indexOf(":") + 1));

                fileName.setText(path.substring(path.lastIndexOf("/") + 1));
                fileName.setVisibility(View.VISIBLE);
                btnSendFile.setVisibility(View.VISIBLE);
                btnCancel.setVisibility(View.VISIBLE);
                btnChange.setVisibility(View.GONE);
                btnSend.setVisibility(View.GONE);
                writeMsg.setVisibility(View.GONE);
                clrCode.setVisibility(View.GONE);
                // writeMsg.setText(textFileData);
            }
        }
    }

}

