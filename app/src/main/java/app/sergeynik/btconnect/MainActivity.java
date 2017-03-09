package app.sergeynik.btconnect;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;

import com.ghgande.j2mod.modbus.Modbus;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Timer;

import app.sergeynik.library.BluetoothSPP;
import app.sergeynik.library.BluetoothState;
import app.sergeynik.library.DeviceList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        SurfaceHolder.Callback, MyTimerTask.MyCallback, KeyboardFragment.KeyboardCallback,
        NumPadFragment.NumPadCallback{

    private static final String TAG = "Svetlin SurfaceView";
    private static final String CP866 = "Cp866";
    private static final int ROWS = 4;
    private static final int COLUMNS = 20;
    private int mCount = 0;
    private FragmentManager mFragmentManager;

    private TransferControl mControl;
    private RequestCreator mCreator;
    private byte[] mBytes;
    private Timer mTimer;
    private MyTimerTask mMyTimerTask;

    private TextView textStatus;

    // Canvas___________________________________________
    private Paint fontPaintOne;
    private Paint fontPaintTwo;
    private Paint fontPaintThree;
    private Paint fontPaintFour;
    private Paint redPaint;
    private static final String text = "Text for example    ";
    private int fontSize = 75;
    private float[] widths;
    private float width;
    private String firstRow = null;
    private String secondRow = null;
    private String thirdRow = null;
    private String fourthRow = null;
    private Canvas canvas;
    //___________________________________________________

    private BluetoothSPP bt;
    private SurfaceView mSurface;
    private Menu menu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mControl = TransferControl.getInstance();
        mCreator = new RequestCreator();

        textStatus = (TextView) findViewById(R.id.text_status);

        // DRAW---------------------------------------------
        mSurface = (SurfaceView) findViewById(R.id.surface);
        mSurface.getHolder().addCallback(this);

        redPaint = new Paint();
        redPaint.setColor(Color.BLACK);

        fontPaintOne = new Paint(Paint.ANTI_ALIAS_FLAG);
        fontPaintOne.setTypeface(Typeface.createFromAsset(
                getAssets(), "fonts/LucidaDOS.ttf"));
        fontPaintOne.setTextSize(fontSize);
        //fontPaint.setStyle(Paint.Style.STROKE);
        // ширина текста
        width = fontPaintOne.measureText(text);
        // посимвольная ширина
        widths = new float[text.length()];
        fontPaintOne.getTextWidths(text, widths);
        fontPaintOne.setColor(Color.BLACK);

        fontPaintTwo = new Paint(Paint.ANTI_ALIAS_FLAG);
        fontPaintTwo.setTypeface(Typeface.createFromAsset(
                getAssets(), "fonts/LucidaDOS.ttf"));
        fontPaintTwo.setTextSize(fontSize);
        //fontPaint.setStyle(Paint.Style.STROKE);
        // ширина текста
        width = fontPaintTwo.measureText(text);
        // посимвольная ширина
        widths = new float[text.length()];
        fontPaintTwo.getTextWidths(text, widths);
        fontPaintTwo.setColor(Color.BLACK);

        fontPaintThree = new Paint(Paint.ANTI_ALIAS_FLAG);
        fontPaintThree.setTypeface(Typeface.createFromAsset(
                getAssets(), "fonts/LucidaDOS.ttf"));
        fontPaintThree.setTextSize(fontSize);
        //fontPaint.setStyle(Paint.Style.STROKE);
        // ширина текста
        width = fontPaintThree.measureText(text);
        // посимвольная ширина
        widths = new float[text.length()];
        fontPaintThree.getTextWidths(text, widths);
        fontPaintThree.setColor(Color.BLACK);

        fontPaintFour = new Paint(Paint.ANTI_ALIAS_FLAG);
        fontPaintFour.setTypeface(Typeface.createFromAsset(
                getAssets(), "fonts/LucidaDOS.ttf"));
        fontPaintFour.setTextSize(fontSize);
        //fontPaint.setStyle(Paint.Style.STROKE);
        // ширина текста
        width = fontPaintFour.measureText(text);
        // посимвольная ширина
        widths = new float[text.length()];
        fontPaintFour.getTextWidths(text, widths);
        fontPaintFour.setColor(Color.BLACK);
        // DRAW---------------------------------------------

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//--------------------------------------------------------------------------------------------------

        bt = new BluetoothSPP(this);

        // Support bluetooth on device?
        if (!bt.isBluetoothAvailable()) {
            Toast.makeText(this
                    , "Bluetooth is not available"
                    , Toast.LENGTH_SHORT).show();
            finish();
        }

        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {
                if (data != null) {
                    if(mBytes != null && !Arrays.equals(data, mBytes) && data[0] == 1 && data[1] == 3){
                        bytesInOrder(data);
                        tryDrawing(mSurface.getHolder());
                    }
                }
            }
        });

        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            public void onDeviceDisconnected() {
                if (mTimer != null) {
                    mTimer.cancel();
                    mTimer = null;
                }
                if (textStatus != null) {
                    textStatus.setText("Status : Not connect");
                    menu.clear();
                    getMenuInflater().inflate(R.menu.menu_connection, menu);
                }
            }
            public void onDeviceConnectionFailed() {
                if (mTimer != null) {
                    mTimer.cancel();
                    mTimer = null;
                }
                if (textStatus != null) {
                    textStatus.setText("Status : Connection failed");
                }
            }
            public void onDeviceConnected(String name, String address) {
                if (textStatus != null) {
                    textStatus.setText("Status : Connected to " + name);
                    menu.clear();
                    getMenuInflater().inflate(R.menu.menu_disconnection, menu);
                }
                Log.e(TAG, String.valueOf(mCount));
                mTimer = new Timer();
                mMyTimerTask = new MyTimerTask();
                mMyTimerTask.registerCallBack(MainActivity.this);
                mTimer.schedule(mMyTimerTask, 1000, 500);
            }
        });

        mFragmentManager = getSupportFragmentManager();
        Fragment fragment = mFragmentManager.findFragmentById(R.id.fragment_container);
        if (fragment == null) {
            fragment = new KeyboardFragment();
            mFragmentManager.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        menu.clear();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_connection, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Activity on bluetooth test
        if (!bt.isBluetoothEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if (!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_ANDROID);
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent intent;
        switch (id) {
            case R.id.nav_review:
                break;
            case R.id.nav_debug:
                break;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_android_connect) {
            bt.setDeviceTarget(BluetoothState.DEVICE_ANDROID);
			/*
			if(bt.getServiceState() == BluetoothState.STATE_CONNECTED)
    			bt.disconnect();*/
            Intent intent = new Intent(this, DeviceList.class);
            startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
        } else if (id == R.id.menu_device_connect) {
            bt.setDeviceTarget(BluetoothState.DEVICE_OTHER);
			/*
			if(bt.getServiceState() == BluetoothState.STATE_CONNECTED)
    			bt.disconnect();*/
            Intent intent = new Intent(this, DeviceList.class);
            startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
        } else if (id == R.id.menu_disconnect) {
            if (bt.getServiceState() == BluetoothState.STATE_CONNECTED)
                bt.disconnect();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        bt.disconnect();
        bt.stopService();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK)
                bt.connect(data);
        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_ANDROID);
            } else {
                Toast.makeText(getApplicationContext()
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        tryDrawing(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        tryDrawing(holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    // Visual text on the screen
    //==============================================================================================
    private void tryDrawing(SurfaceHolder holder) {
        Log.i(TAG, "Trying to draw...");
        canvas = holder.lockCanvas();
        if (canvas == null) {
            Log.e(TAG, "Cannot draw onto the canvas as it's null");
        } else {
            drawMyStuff(canvas);
            holder.unlockCanvasAndPost(canvas);
        }
    }

    private void drawMyStuff(final Canvas canvas) {
//        canvas.drawColor(Color.RED);
        canvas.drawRGB(175,192,215);
        canvas.translate(100, 100);
        // вывод текста
        if (firstRow != null && secondRow != null && thirdRow != null && fourthRow != null) {
            canvas.drawText(firstRow, 0, 0, fontPaintOne);
            canvas.drawText(secondRow, 0, 80, fontPaintTwo);
            canvas.drawText(thirdRow, 0, 160, fontPaintThree);
            canvas.drawText(fourthRow, 0, 240, fontPaintFour);
        } else {
            canvas.drawText(text, 0, 0, fontPaintOne);
        }
    }
    //==============================================================================================


    private void bytesInOrder(byte[] data) {
        // change the encoding
        ByteBuffer msgBuf = ByteBuffer.wrap(data);
        CharBuffer msgCharBuf = Charset.forName(CP866).decode(msgBuf);
        // Drop slaveId, function, number of bytes and CRC in the end
        char[] msgChArray = new char[msgCharBuf.length()-5];
        for (int i = 0; i < msgChArray.length; i++) {
            msgChArray[i] = msgCharBuf.charAt(i + 3);
            //Log.e(TAG, String.valueOf(msgChArray[i]));
        }
        for (int i = 1; i < msgChArray.length; i+=2) {
            if(i < msgChArray.length){
                char temp = msgChArray[i];
                msgChArray[i] = msgChArray[i - 1];
                msgChArray[i - 1] = temp;
            }
        }
        for (int i = 0; i < msgChArray.length; i++){
            if(msgChArray[i] == 'н'){
                msgChArray[i] = 'H';
            }
        }
//        for (char ch : msgChArray){
//            Log.e(TAG, String.valueOf(ch));
//        }
        int count = 0;
        for (int i = 0; i < ROWS; i++) {
            char[] titleScreen = new char[COLUMNS];
            for (int j = 0; j < COLUMNS; j++) {
                if(count < msgChArray.length){
                    titleScreen[j] = msgChArray[count++];
                }
            }
            String tempStr = String.valueOf(titleScreen);
            switch (i){
                case 0:
                    try {
                        firstRow = new String(tempStr.getBytes(CP866), "windows-1251");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    tempStr = null;
                    titleScreen = null;
                    break;
                case 1:
                    try {
                        secondRow = new String(tempStr.getBytes(CP866), "windows-1251");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    tempStr = null;
                    titleScreen = null;
                    break;
                case 2:
                    try {
                        thirdRow = new String(tempStr.getBytes(CP866), "windows-1251");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    tempStr = null;
                    titleScreen = null;
                    break;
                case 3:
                    try {
                        fourthRow = new String(tempStr.getBytes(CP866), "windows-1251");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    tempStr = null;
                    titleScreen = null;
                    break;
                default:
                    break;
            }
        }
    }


    @Override
    public void callBackReturn() {
        mBytes = mCreator.createRequest(Modbus.READ_MULTIPLE_REGISTERS);
        bt.send(mBytes, false);
        ++mCount;
        Log.e(TAG, String.valueOf(mCount));
    }

    @Override
    public void keyBoardReturn(int id) {
        switch (id){
            case R.id.btn_to_numpad:
                // Create new fragment and transaction
                Fragment numPadFragment = new NumPadFragment();
                FragmentTransaction transaction = mFragmentManager.beginTransaction();
                transaction.replace(R.id.fragment_container, numPadFragment);
                transaction.addToBackStack(null);
                // Commit the transaction
                transaction.commit();
                break;
            case R.id.btn_escape:
                mCreator.setPressedKey(KeyboardRTU.ESCAPE);
                bt.send(mCreator.createRequest(Modbus.WRITE_FILE_RECORD), false);
                break;
            case R.id.btn_enter:
                mCreator.setPressedKey(KeyboardRTU.ENTER);
                bt.send(mCreator.createRequest(Modbus.WRITE_FILE_RECORD), false);
                break;
            case R.id.btn_right:
                mCreator.setPressedKey(KeyboardRTU.RIGHT);
                bt.send(mCreator.createRequest(Modbus.WRITE_FILE_RECORD), false);
                break;
            case R.id.btn_left:
                mCreator.setPressedKey(KeyboardRTU.LEFT);
                bt.send(mCreator.createRequest(Modbus.WRITE_FILE_RECORD), false);
                break;
            case R.id.btn_up:
                mCreator.setPressedKey(KeyboardRTU.UP);
                bt.send(mCreator.createRequest(Modbus.WRITE_FILE_RECORD), false);
                break;
            case R.id.btn_down:
                mCreator.setPressedKey(KeyboardRTU.DOWN);
                bt.send(mCreator.createRequest(Modbus.WRITE_FILE_RECORD), false);
                break;
        }
    }

    @Override
    public void numPadReturn(int id) {
        switch (id) {
            case R.id.btn_one:
                mCreator.setPressedKey(KeyboardRTU.ONE);
                break;
            case R.id.btn_two:
                mCreator.setPressedKey(KeyboardRTU.TWO);
                break;
            case R.id.btn_three:
                mCreator.setPressedKey(KeyboardRTU.THREE);
                break;
            case R.id.btn_four:
                mCreator.setPressedKey(KeyboardRTU.FOUR);
                break;
            case R.id.btn_five:
                mCreator.setPressedKey(KeyboardRTU.FIVE);
                break;
            case R.id.btn_six:
                mCreator.setPressedKey(KeyboardRTU.SIX);
                break;
            case R.id.btn_seven:
                mCreator.setPressedKey(KeyboardRTU.SEVEN);
                break;
            case R.id.btn_eight:
                mCreator.setPressedKey(KeyboardRTU.EIGHT);
                break;
            case R.id.btn_nine:
                mCreator.setPressedKey(KeyboardRTU.NINE);
                break;
            case R.id.btn_zero:
                mCreator.setPressedKey(KeyboardRTU.ZERO);
                break;
        }
        bt.send(mCreator.createRequest(Modbus.WRITE_FILE_RECORD), false);
    }
}




