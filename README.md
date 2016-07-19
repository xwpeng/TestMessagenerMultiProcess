# TestMessagenerMultiProcess
# 作用
项目需现实推送消息的Service放入私有进程:push,找一种思路实现push来的消息由:push到:Main进程.这个demo将展示如何人初步实现项目这个需求
#准备工作
了解AIDL机制原理:https://github.com/xwpeng/TestAidl.git.
# 利用Messagener信使来通信
1. 新建一个Messagener mMessagener,Service#onBind()返回mMessagener.getBinder();
2. MainActivity在bindService的回调中将IBinder强转为Messagener,Main拥有Service的Messager,Main可以给Service发送指令了.
  
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

3. Service如何拥有Main的Messagener
   Main中新定义一个Messagener mMessagener

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

    在绑定Service回调中,将一个激活Message的replyTo设置为mMessgener,Main将这个激活Message发送到Service.

    这样Service就能获取到Main的Messagener,通过它就能给Main发送消息了.

4. 数据传递
   数据传递使用Message携带,Message#obtain()的第三个参数可以依靠Bunder传输基本类型,非基本类型需要序列化.
# Demo
  https://github.com/xwpeng/TestMessagenerMultiProcess.git
