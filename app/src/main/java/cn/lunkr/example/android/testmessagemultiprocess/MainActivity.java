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
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private static final int MSG_SUM = 0x110;
    private TextView mTvState;

    private Messenger mService;
    private boolean isConn;


    private Messenger mMainMessenger = new Messenger(new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case MSG_SUM:
                    Bundle bundle = (Bundle) msg.obj;
                    mTvState.setText("=>" + bundle.getInt("result"));
                    break;
            }
            super.handleMessage(msg);
        }
    });


    private ServiceConnection mConn = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            mService = new Messenger(service);
            isConn = true;
            Message inactiveMessage = Message.obtain(null, MSG_SUM);
            inactiveMessage.replyTo = mMainMessenger;
            if (isConn) {
                //往服务端发激活消息
                try {
                    mService.send(inactiveMessage);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            mService = null;
            isConn = false;
            mTvState.setText("disconnected!");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTvState = (TextView) findViewById(R.id.id_tv_callback);
        //开始绑定服务
        bindService();

    }

    private void bindService()
    {
        Intent intent = new Intent(MainActivity.this, PushService.class);
        bindService(intent, mConn, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        unbindService(mConn);
    }

}
