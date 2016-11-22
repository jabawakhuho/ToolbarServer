package com.sds.study.toolbarserver;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.io.IOException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    BluetoothAdapter bluetoothAdapter;
    static final int REQUEST_BLUETOOTH_ENABLE=1;
    static  final  int REQUEST_DISCOVERABLE=2;
    TextView txt_status;
    /*클라이언트는 이 UUID를 통해서 나의 서버로 접속하면 된다..*/
    String UUID="7fad3246-017e-4949-8043-4512cb912d53";
    /*클라이언트의 접속을 받을수있는 서버(소켓서버와 상당히 비슷)*/
    BluetoothServerSocket server;
    String serviceName;
    Thread acceptThread;/*접속자를 받기 위한 쓰레드!*/
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txt_status =(TextView)findViewById(R.id.txt_status);
        handler = new Handler(){
            /*UI제어 가능 why? 메인 Thread에서 호출*/
            public void handleMessage(Message message) {
                Bundle bundle=message.getData();
                String msg =bundle.getString("msg");
                txt_status.append(msg);
            }
        };

        checkSupportBluetooth();
        requestActiveBluetooth();
        requsetDiscoverable();
        acceptDevice();
    }
    /*블루투스 기기 지원여부 확인*/
    public void checkSupportBluetooth(){
        bluetoothAdapter= BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter==null){
            showMsg("안내","해당 기기는 블루투스 미지원 기기입니다.");
            finish();/*현재 activity를 close함*/
        }
    }
    /*블루투스 활성화 요청*/
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case REQUEST_BLUETOOTH_ENABLE :
                if(resultCode == Activity.RESULT_CANCELED){
                    showMsg("경고", "앱 사용하기 위해서는 Bluetooth를 활성화 해야 합니다.");
                }
                break;
            case REQUEST_DISCOVERABLE :
                if(resultCode == Activity.RESULT_CANCELED){
                    showMsg("경고","클라이언트에게 검색되기 위해 검색 허용해주세요!");
                }
                break;
        }

    }
    public void requestActiveBluetooth(){
        Intent intent = new Intent();
        intent.setAction(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent,REQUEST_BLUETOOTH_ENABLE);
    }

    /*접속자 받을 준비*/
    public void acceptDevice(){
        serviceName=this.getPackageName();
        try {
            server=bluetoothAdapter.listenUsingRfcommWithServiceRecord(serviceName, java.util.UUID.fromString(UUID));
        } catch (IOException e) {
            e.printStackTrace();
        }
        acceptThread = new Thread(){
            public void run() {
                try {
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("msg","서버준비됨\n");
                    message.setData(bundle);
                    handler.sendMessage(message);

                    BluetoothSocket socket=server.accept();
                    Message message2 = new Message();
                    Bundle bundle2 = new Bundle();
                    bundle2.putString("msg","접속자 감지 성공!!!올레~\n");
                    message2.setData(bundle2);
                    handler.sendMessage(message2);

                    //ServerThread st = new ServerThread(socket);
                    //st.start();/*클라이언트의 말 청취 시작*/

                    /*더이상 덥속자 허용 방지!!!(why? bluetooth는 1:1통신이기 때문)
                    * 서버의 프로세스를 중단하는것이 아니라, 접속자의 접속을 원천 차단이 목적임!!
                    * */
                    server.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        acceptThread.start();

    }
    /*클라이언트가 서버인 나를 발견할수있도록 검색허용옵션 설정하자*/
    public void requsetDiscoverable(){
        Intent intent = new Intent();
        intent.setAction(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,60*1);
        startActivityForResult(intent,REQUEST_DISCOVERABLE);
    }

    /*대화 나누기*/



    public void showMsg(String title, String msg){

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(title).setMessage(msg).show();
    }
}
