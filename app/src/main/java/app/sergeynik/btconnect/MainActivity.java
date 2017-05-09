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
        SurfaceHolder.Callback, PollDeviceTask.MyCallback, KeyboardFragment.KeyboardCallback,
        NumPadFragment.NumPadCallback{

    private static final String ENCODING_CP866 = "Cp866";
    private static final String ENCODING_1251 = "windows-1251";

    private static final int SCREEN_ROWS = 4;
    private static final int SCREEN_COLUMNS = 20;
    private static final int THROW_EXCESS = 5;

    private FragmentManager mFragmentManager;
    private RequestProducer mRequestProducer;
    private byte[] mReadyHexRequest;
    private Timer mPeriodScan;
    private PollDeviceTask mPollDeviceTask;

    private TextView textStatus;

    // Canvas___________________________________________
    private Paint fontPaintOne;
    private Paint fontPaintTwo;
    private Paint fontPaintThree;
    private Paint fontPaintFour;

    // 20 characters in row
    private static final String START_TEXT =  "    БИОИ ОЗНА       ";
    private static final String START_TEXT2 = " СПУТНИК МАССОМЕР   ";
    private static final String START_TEXT3 = "   ПОДКЛЮЧЕНИЕ      ";
    private static final String START_TEXT4 = "    ОТСУТСТВУЕТ     ";
    private static final String FONT_LUCIDA_DOS = "fonts/LucidaDOS.ttf";

    private static final int fontSize = 75;
    private float[] charWidth;
    private float textWidth;

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

        mRequestProducer = new RequestProducer();
        textStatus = (TextView) findViewById(R.id.text_status);

        // DRAW---------------------------------------------
        mSurface = (SurfaceView) findViewById(R.id.surface);
        mSurface.getHolder().addCallback(this);

        fontPaintOne = createPaint();
        fontPaintTwo = createPaint();
        fontPaintThree = createPaint();
        fontPaintFour = createPaint();

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
                    if(mReadyHexRequest != null && !Arrays.equals(data, mReadyHexRequest) && data[0] == 1 && data[1] == 3){
                        bytesInOrder(data);
                        tryDrawing(mSurface.getHolder());
                    }
                }
            }
        });

        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            public void onDeviceDisconnected() {
                if (mPeriodScan != null) {
                    mPeriodScan.cancel();
                    mPeriodScan = null;
                }
                if (textStatus != null) {
                    textStatus.setText(R.string.status_not);
                    menu.clear();
                    getMenuInflater().inflate(R.menu.menu_connection, menu);
                }
            }
            public void onDeviceConnectionFailed() {
                if (mPeriodScan != null) {
                    mPeriodScan.cancel();
                    mPeriodScan = null;
                }
                if (textStatus != null) {
                    textStatus.setText(R.string.status_failed);
                }
            }
            public void onDeviceConnected(String name, String address) {
                if (textStatus != null) {
                    textStatus.setText(String.format("%s%s", getString(R.string.status_connected), name));
                    menu.clear();
                    getMenuInflater().inflate(R.menu.menu_disconnection, menu);
                }
                mPeriodScan = new Timer();
                mPollDeviceTask = new PollDeviceTask();
                mPollDeviceTask.registerCallBack(MainActivity.this);
                mPeriodScan.schedule(mPollDeviceTask, 1000, 500);
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
        if (mPeriodScan != null) {
            mPeriodScan.cancel();
            mPeriodScan = null;
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
        canvas = holder.lockCanvas();
        if (canvas == null) {
            Log.e("TAG", "Cannot draw onto the canvas as it's null");
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
            canvas.drawText(START_TEXT, 0, 0, fontPaintOne);
            canvas.drawText(START_TEXT2, 0, 80, fontPaintTwo);
            canvas.drawText(START_TEXT3, 0, 160, fontPaintThree);
            canvas.drawText(START_TEXT4, 0, 240, fontPaintFour);
        }
    }
    //==============================================================================================


    private void bytesInOrder(byte[] data) {
        // Change the encoding
        ByteBuffer msgBuf = ByteBuffer.wrap(data);
        CharBuffer msgCharBuf = Charset.forName(ENCODING_CP866).decode(msgBuf);
        // Drop slaveId, function, number of bytes and CRC in the end
        char[] msgChArray = new char[msgCharBuf.length() - 5]; // allow only data
        for (int i = 0; i < msgChArray.length; i++) {
            msgChArray[i] = msgCharBuf.charAt(i + 3); // headless
        }

        // Swap bytes, because little/big endian
        for (int i = 1; i < msgChArray.length; i+=2) {
            if(i < msgChArray.length){
                char temp = msgChArray[i];
                msgChArray[i] = msgChArray[i - 1];
                msgChArray[i - 1] = temp;
            }
        }

        // Other cycle because appears empty character ???
        for (int symbol = 0; symbol < msgChArray.length; symbol++){
            if(msgChArray[symbol] == 'н'){
                // Russian н to Enghish H
                msgChArray[symbol] = 'H';
            }
        }

        int count = 0;
        for (int row = 0; row < SCREEN_ROWS; row++) {
            char[] titleScreen = new char[SCREEN_COLUMNS];
            for (int column = 0; column < SCREEN_COLUMNS; column++) {
                if(count < msgChArray.length){
                    titleScreen[column] = msgChArray[count++];
                }
            }
            String tempStr = String.valueOf(titleScreen);
            switch (row){
                case 0:
                    try {
                        firstRow = new String(tempStr.getBytes(ENCODING_CP866), ENCODING_1251);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    break;
                case 1:
                    try {
                        secondRow = new String(tempStr.getBytes(ENCODING_CP866), ENCODING_1251);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    break;
                case 2:
                    try {
                        thirdRow = new String(tempStr.getBytes(ENCODING_CP866), ENCODING_1251);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    break;
                case 3:
                    try {
                        fourthRow = new String(tempStr.getBytes(ENCODING_CP866), ENCODING_1251);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
        }
    }


    @Override
    public void sendRequest() {
        mReadyHexRequest = mRequestProducer.createRequest(Modbus.READ_MULTIPLE_REGISTERS);
        bt.send(mReadyHexRequest, false);
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
                mRequestProducer.setPressedKey(KeyboardRTU.ESCAPE);
                bt.send(mRequestProducer.createRequest(Modbus.WRITE_FILE_RECORD), false);
                break;
            case R.id.btn_enter:
                mRequestProducer.setPressedKey(KeyboardRTU.ENTER);
                bt.send(mRequestProducer.createRequest(Modbus.WRITE_FILE_RECORD), false);
                break;
            case R.id.btn_right:
                mRequestProducer.setPressedKey(KeyboardRTU.RIGHT);
                bt.send(mRequestProducer.createRequest(Modbus.WRITE_FILE_RECORD), false);
                break;
            case R.id.btn_left:
                mRequestProducer.setPressedKey(KeyboardRTU.LEFT);
                bt.send(mRequestProducer.createRequest(Modbus.WRITE_FILE_RECORD), false);
                break;
            case R.id.btn_up:
                mRequestProducer.setPressedKey(KeyboardRTU.UP);
                bt.send(mRequestProducer.createRequest(Modbus.WRITE_FILE_RECORD), false);
                break;
            case R.id.btn_down:
                mRequestProducer.setPressedKey(KeyboardRTU.DOWN);
                bt.send(mRequestProducer.createRequest(Modbus.WRITE_FILE_RECORD), false);
                break;
        }
    }

    @Override
    public void numPadReturn(int id) {
        switch (id) {
            case R.id.btn_one:
                mRequestProducer.setPressedKey(KeyboardRTU.ONE);
                break;
            case R.id.btn_two:
                mRequestProducer.setPressedKey(KeyboardRTU.TWO);
                break;
            case R.id.btn_three:
                mRequestProducer.setPressedKey(KeyboardRTU.THREE);
                break;
            case R.id.btn_four:
                mRequestProducer.setPressedKey(KeyboardRTU.FOUR);
                break;
            case R.id.btn_five:
                mRequestProducer.setPressedKey(KeyboardRTU.FIVE);
                break;
            case R.id.btn_six:
                mRequestProducer.setPressedKey(KeyboardRTU.SIX);
                break;
            case R.id.btn_seven:
                mRequestProducer.setPressedKey(KeyboardRTU.SEVEN);
                break;
            case R.id.btn_eight:
                mRequestProducer.setPressedKey(KeyboardRTU.EIGHT);
                break;
            case R.id.btn_nine:
                mRequestProducer.setPressedKey(KeyboardRTU.NINE);
                break;
            case R.id.btn_zero:
                mRequestProducer.setPressedKey(KeyboardRTU.ZERO);
                break;
        }
        bt.send(mRequestProducer.createRequest(Modbus.WRITE_FILE_RECORD), false);
    }

    private Paint createPaint() {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTypeface(Typeface.createFromAsset(
                getAssets(), FONT_LUCIDA_DOS));
        paint.setTextSize(fontSize);
        //fontPaint.setStyle(Paint.Style.STROKE);
        // ширина текста
        textWidth = paint.measureText(START_TEXT);
        // посимвольная ширина
        charWidth = new float[START_TEXT.length()];
        paint.getTextWidths(START_TEXT, charWidth);
        paint.setColor(Color.BLACK);
        return paint;
    }
}




