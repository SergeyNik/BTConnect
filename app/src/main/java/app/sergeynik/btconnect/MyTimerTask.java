package app.sergeynik.btconnect;

import java.util.TimerTask;

/**
 * Created by Sergey on 08.03.2017.
 */

class MyTimerTask extends TimerTask {

    public interface MyCallback{
        void callBackReturn();
    }

    MyCallback myCallback;

    public void registerCallBack(MyCallback callback){
        this.myCallback = callback;
    }
    @Override
    public void run() {
        myCallback.callBackReturn();
    }
}
