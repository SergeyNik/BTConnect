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
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersRequest;
import com.ghgande.j2mod.modbus.msg.WriteCoilRequest;
import com.ghgande.j2mod.modbus.msg.WriteSingleRegisterRequest;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;
import com.ghgande.j2mod.modbus.util.ModbusUtil;
import com.serotonin.util.queue.ByteQueue;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import app.sergeynik.library.BluetoothSPP;
import app.sergeynik.library.BluetoothState;
import app.sergeynik.library.DeviceList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SurfaceHolder.Callback {

    private static final String TAG = "Svetlin SurfaceView";
    private static final String CP866 = "Cp866";
    private static final int ROWS = 4;
    private static final int COLUMNS = 20;
    private TransferControl mControl;

    private Paint fontPaintOne;
    private Paint fontPaintTwo;
    private Paint fontPaintThree;
    private Paint fontPaintFour;
    private Paint redPaint;
    private static final String text = "Text for example    ";
    private int fontSize = 60;
    private float[] widths;
    private float width;
    private String firstRow = null;
    private String secondRow = null;
    private String thirdRow = null;
    private String fourthRow = null;
    private Canvas canvas;
    private Button btnSend;
    private BluetoothSPP bt;

    private SurfaceView mSurface;

    private Menu menu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mControl = TransferControl.getInstance();
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

        btnSend = (Button) findViewById(R.id.btn_send);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bt.send(createRequest(1, 3, 1956, 12, 2), false);
            }
        });

//--------------------------------------------------------------------------------------------------

        bt = new BluetoothSPP(this);

        // Support bluetooth on device?
        if (!bt.isBluetoothAvailable()) {
            Toast.makeText(this
                    , "Bluetooth is not available"
                    , Toast.LENGTH_SHORT).show();
            finish();
        }

        // Message from friend  ***done with fragments!!!***
        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {
                if (data != null) {
                    // calc CRC!!!
                    bytesInOrder(data);
                    tryDrawing(mSurface.getHolder());
                }
            }
        });

        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            public void onDeviceDisconnected() {

                Button textStatus = (Button) findViewById(R.id.btn_send);
                if (textStatus != null) {
                    textStatus.setText("Status : Not connect");
                    menu.clear();
                    getMenuInflater().inflate(R.menu.menu_connection, menu);
                }
            }
            public void onDeviceConnectionFailed() {

                Button textStatus = (Button) findViewById(R.id.btn_send);
                if (textStatus != null) {
                    textStatus.setText("Status : Connection failed");
                }
            }
            public void onDeviceConnected(String name, String address) {
                Button textStatus = (Button) findViewById(R.id.btn_send);
                if (textStatus != null) {
                    textStatus.setText("Status : Connected to " + name);
                    menu.clear();
                    getMenuInflater().inflate(R.menu.menu_disconnection, menu);
                }
            }
        });
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
        canvas.translate(50, 250);
        // вывод текста
        if (firstRow != null && secondRow != null && thirdRow != null && fourthRow != null) {
        Log.e(TAG, firstRow.toString());
            canvas.drawText(firstRow, 0, 0, fontPaintOne);
            canvas.drawText(secondRow, 0, 80, fontPaintTwo);
            canvas.drawText(thirdRow, 0, 160, fontPaintThree);
            canvas.drawText(fourthRow, 0, 240, fontPaintFour);
        } else {
            canvas.drawText(text, 0, 0, fontPaintOne);
        }
    }
    //==============================================================================================

    public byte[] createRequest(int slaveId, int func, int address, int value, int reqType) {
        ByteQueue mByteQueue = new ByteQueue();
        switch (reqType) {
            case 0:
                boolean val = (value == 1);
                WriteCoilRequest mWrCoilReq = new WriteCoilRequest(address, val);
                mWrCoilReq.setUnitID(slaveId);
                mWrCoilReq.setHeadless();
                mByteQueue.push(mWrCoilReq.getUnitID());
                mByteQueue.push(mWrCoilReq.getFunctionCode());
                mByteQueue.push(mWrCoilReq.getMessage());
                // CRC
                int[] crc = ModbusUtil.calculateCRC(mByteQueue.peekAll(), 0,
                        mByteQueue.peekAll().length);

                mByteQueue.push(crc[0]);
                mByteQueue.push(crc[1]);

                break;

            case 1:
                SimpleRegister register = new SimpleRegister(value);
                WriteSingleRegisterRequest mWrSingleRegReq =
                        new WriteSingleRegisterRequest(address, register);
                mWrSingleRegReq.setUnitID(slaveId);
                mWrSingleRegReq.setHeadless();
                mByteQueue.push(mWrSingleRegReq.getUnitID());
                mByteQueue.push(mWrSingleRegReq.getFunctionCode());
                mByteQueue.push(mWrSingleRegReq.getMessage());
                // CRC
                int[] crc1 = ModbusUtil.calculateCRC(mByteQueue.peekAll(), 0,
                        mByteQueue.peekAll().length);

                mByteQueue.push(crc1[0]);
                mByteQueue.push(crc1[1]);
                break;

            case 2:

                ReadMultipleRegistersRequest readMultRegsReq = new ReadMultipleRegistersRequest();

                readMultRegsReq.setUnitID(slaveId);
                readMultRegsReq.setHeadless();
                readMultRegsReq.setReference(address);

                readMultRegsReq.setWordCount(40);
                mByteQueue.push(readMultRegsReq.getUnitID());
                mByteQueue.push(readMultRegsReq.getFunctionCode());
                mByteQueue.push(readMultRegsReq.getMessage());
                // CRC
                int[] crc2 = ModbusUtil.calculateCRC(mByteQueue.peekAll(), 0,
                        mByteQueue.peekAll().length);

                mByteQueue.push(crc2[0]);
                mByteQueue.push(crc2[1]);
                break;

        }
        return mByteQueue.peekAll();
    }

    private void bytesInOrder(byte[] data) {
        // change the encoding
        ByteBuffer msgBuf = ByteBuffer.wrap(data);
        CharBuffer msgCharBuf = Charset.forName(CP866).decode(msgBuf);

        // Drop slaveId, function, number of bytes and CRC in the end
        char[] msgChArray = new char[msgCharBuf.length()-5];
        for (int i = 0; i < msgChArray.length; i++) {
            msgChArray[i] = msgCharBuf.charAt(i + 3);
        }
        for (int i = 1; i < msgChArray.length; i+=2) {
            if(i < msgChArray.length - 1){
                char temp = msgChArray[i];
                msgChArray[i] = msgChArray[i - 1];
                msgChArray[i - 1] = temp;
            }
        }
        for (char ch : msgChArray){

        Log.e(TAG, String.valueOf(ch));
        }
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
}




