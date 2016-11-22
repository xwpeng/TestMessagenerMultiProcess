package cn.lunkr.example.android.testmessagemultiprocess;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private static final int MSG_SUM = 0x110;
    private TextView mTvState;
    private Messenger mService;//服务端的Messenger,拥有发消息给服务端能力
    private ServiceConnection mConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = new Messenger(service);
            Message inactiveMessage = Message.obtain(null, MSG_SUM);
            inactiveMessage.replyTo = new Messenger(new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case MSG_SUM:
                            Bundle bundle = (Bundle) msg.obj;
                            mTvState.setText("=>" + bundle.getInt("result"));
                            break;
                    }
                    super.handleMessage(msg);
                }
            });
            //往服务端发激活消息(建立服务端到客户端的通道)
            try {
                mService.send(inactiveMessage);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mTvState.setText("disconnected!");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTvState = (TextView) findViewById(R.id.id_tv_callback);
        findViewById(R.id.id_button_startReply).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startReply();
            }
        });
        //开始绑定服务
        bindService();

    }

    private void startReply() {
        Message message = Message.obtain(null, PushService.MSG_REPLY);
        try {
            mService.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    private void bindService() {
        Intent intent = new Intent(MainActivity.this, PushService.class);
        bindService(intent, mConn, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConn);
    }

}
