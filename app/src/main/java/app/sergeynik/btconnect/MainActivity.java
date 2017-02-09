package app.sergeynik.btconnect;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
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

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import app.sergeynik.library.BluetoothSPP;
import app.sergeynik.library.BluetoothState;
import app.sergeynik.library.DeviceList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SurfaceHolder.Callback {

    private static final String TAG = "Svetlin SurfaceView";
    Paint fontPaint;
    Paint redPaint;
    String text = "Test width text";
    int fontSize = 60;
    float[] widths;
    float width;
    private String s;
    private String string;
    private Canvas canvas;
    private byte[] dataBytes;
    private Button btnSend;
    private BluetoothSPP bt;
    private int fragmentId;
    private FragmentManager fragmentManager;
    private FragmentTransaction transaction;

    private SurfaceView mSurface;

    Menu menu;

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        // Get current fragment id
        fragmentId = fragment.getId();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // DRAW---------------------------------------------
        mSurface = (SurfaceView) findViewById(R.id.surface);
        mSurface.getHolder().addCallback(this);

        redPaint = new Paint();
        redPaint.setColor(Color.RED);

        fontPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fontPaint.setTypeface(Typeface.createFromAsset(
                getAssets(), "fonts/LucidaDOS.ttf"));
//        fontPaint.setTextLocale(new Locale("ru"));
        fontPaint.setTextSize(fontSize);
        fontPaint.setStyle(Paint.Style.STROKE);

        // ширина текста
        width = fontPaint.measureText(text);

        // посимвольная ширина
        widths = new float[text.length()];
        fontPaint.getTextWidths(text, widths);
        // DRAW---------------------------------------------

        btnSend = (Button) findViewById(R.id.btn_send);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bt.send(createRequest(1, 3, 1956, 12, 2), false);
                Log.e("MESSAGA", "Sendaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
                canvas = mSurface.getHolder().lockCanvas();
                if (canvas == null) {
                    Log.e(TAG, "Cannot draw onto the canvas as it's null");
                } else {
                    drawMyStuff(canvas);
                    mSurface.getHolder().unlockCanvasAndPost(canvas);
                }

            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
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
                if (data != null){

                    ByteQueue byteQueue = new ByteQueue();
                    for (int i = 0; i < data.length; i++) {
                        byteQueue.push(data[i]);
                    }
                    ByteBuffer buf= ByteBuffer.wrap(byteQueue.peekAll());
                    CharBuffer charbuf = Charset.forName("Cp866").decode(buf);
                    char[] ch_array = charbuf.array();
                    for (char ch:ch_array)
                        Log.e(TAG, String.valueOf(ch));


                    canvas = mSurface.getHolder().lockCanvas();
                    if (canvas == null) {
                        Log.e(TAG, "Cannot draw onto the canvas as it's null");
                    } else {
                        drawMyStuff(canvas);
                        mSurface.getHolder().unlockCanvasAndPost(canvas);
                    }

                };
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
                // изменить на обработку фрагментов
//                intent = new Intent(this, CanvasActivity.class);
//                startActivity(intent);
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
        canvas.drawRGB(255, 128, 128);

        canvas.translate(50, 250);


        // вывод текста
//        canvas.drawText(text, 0, 0, fontPaint);
        if (string != null){

            //canvas.drawText(Arrays.toString(dataBytes), 0, 0, fontPaint);

            canvas.drawText(string, 0, 0, fontPaint);
        } else{
            canvas.drawText(text, 0, 0, fontPaint);
        }


//        Random random = new Random();
//        Log.i(TAG, "Drawing...");
//        canvas.drawRGB(255, 128, 128);
    }



    public byte[] createRequest(int slaveId, int func, int address, int value, int reqType){
        ByteQueue mByteQueue = new ByteQueue();
        switch (reqType){
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

                readMultRegsReq.setWordCount(39);
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

}


