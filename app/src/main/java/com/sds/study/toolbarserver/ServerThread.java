package com.sds.study.toolbarserver;

import android.bluetooth.BluetoothSocket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 소켓 프로그램과 마찬가지로 클라이언트의 메세지를 지속적으로
 받고 보낼 무한 루프를 실행할 쓰레드를 정의하자!!
 */

public class ServerThread extends Thread {
    boolean flag=true;
    BluetoothSocket socket;
    BufferedReader buffr;
    BufferedWriter buffw;

    public ServerThread(BluetoothSocket socket) {
        this.socket = socket;
        try {
            buffr = new BufferedReader(new InputStreamReader(socket.getInputStream(),"utf-8"));
            buffw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"utf-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*듣기*/
    public void listen(){
        while(flag){
            try {
                String msg=buffr.readLine();
                send(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /*보내기*/
    public void send(String msg){
        try {
            buffw.write(msg);
            buffw.write("\n");
            buffw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        listen();
    }
}
