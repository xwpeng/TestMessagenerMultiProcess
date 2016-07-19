package cn.lunkr.example.android.testmessagemultiprocess;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;

/**
 * Created by xwpeng on 16-7-14.
 */
public class PushService extends Service {

    private static final int MSG_SUM = 0x110;
    private Messenger mToMainMessenger;

    //最好换成HandlerThread的形式
    private Messenger mMessenger = new Messenger(new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                //msg 客户端传来的消息
                case MSG_SUM:
                    mToMainMessenger = msg.replyTo;
                    break;
            }

            super.handleMessage(msg);
        }
    });

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        reply();
        return mMessenger.getBinder();
    }

    void reply() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 30; i++) {
                    if (mToMainMessenger != null) {
                        Message m = new Message();
                        m.what = MSG_SUM;
                        Bundle bundle = new Bundle();
                        bundle.putInt("result", i);
                        m.obj = bundle;
                        try {
                            mToMainMessenger.send(m);
                        }  catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        //模拟耗时
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }


}
