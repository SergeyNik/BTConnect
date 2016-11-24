package app.sergeynik.btconnect;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ghgande.j2mod.modbus.msg.WriteCoilRequest;
import com.ghgande.j2mod.modbus.msg.WriteSingleRegisterRequest;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;
import com.ghgande.j2mod.modbus.util.ModbusUtil;
import com.serotonin.util.queue.ByteQueue;

import app.sergeynik.library.BluetoothSPP;
import app.sergeynik.library.BluetoothState;
import app.sergeynik.library.DeviceList;


public class BTMasterFragment extends Fragment {

    private AppCompatActivity app;
    private BluetoothSPP bt;

    private WriteCoilRequest mCoilRequest;
    private WriteSingleRegisterRequest mRegRequest;
    private ByteQueue requestInBytes;
    private byte[] req;

    private TextView textRead, textStatus;
    private EditText editCoilAddres, editCoilValue, editRgAddress, editRegValue;
    private Button btnSend, btnSendCoil;

    Menu menu;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        app = (AppCompatActivity) getActivity();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        this.menu = menu;
        inflater.inflate(R.menu.menu_connection, menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_btmaster, container, false);
        Log.d("request", "---------------------------------------------------------------");
        textStatus = (TextView)view.findViewById(R.id.txt_status);
        btnSend = (Button)view.findViewById(R.id.btnSend);
        btnSendCoil = (Button)view.findViewById(R.id.btn_send_coil);
        editCoilAddres = (EditText) view.findViewById(R.id.edit_coil_address);
        editCoilValue = (EditText) view.findViewById(R.id.edit_coil_value);
        editRgAddress = (EditText) view.findViewById(R.id.edit_register_address);
        editRegValue = (EditText) view.findViewById(R.id.edit_register_value);

        bt = new BluetoothSPP(getActivity());
        //createRequest();

        // Support bluetooth on device?
        if(!bt.isBluetoothAvailable()){
            Toast.makeText(getActivity()
                    , "Bluetooth is not available"
                    , Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        // Message from friend
        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {
                textRead.append(message + "\n");
            }
        });

        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            public void onDeviceDisconnected() {
                textStatus.setText("Status : Not connect");
                menu.clear();
                app.getMenuInflater().inflate(R.menu.menu_connection, menu);
            }

            public void onDeviceConnectionFailed() {
                textStatus.setText("Status : Connection failed");
            }

            public void onDeviceConnected(String name, String address) {
                textStatus.setText("Status : Connected to " + name);
                menu.clear();
                app.getMenuInflater().inflate(R.menu.menu_disconnection, menu);
            }
        });

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.menu_android_connect) {
            bt.setDeviceTarget(BluetoothState.DEVICE_ANDROID);
			/*
			if(bt.getServiceState() == BluetoothState.STATE_CONNECTED)
    			bt.disconnect();*/
            Intent intent = new Intent(getContext(), DeviceList.class);
            startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
        } else if(id == R.id.menu_device_connect) {
            bt.setDeviceTarget(BluetoothState.DEVICE_OTHER);
			/*
			if(bt.getServiceState() == BluetoothState.STATE_CONNECTED)
    			bt.disconnect();*/
            Intent intent = new Intent(getContext(), DeviceList.class);
            startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
        } else if(id == R.id.menu_disconnect) {
            if(bt.getServiceState() == BluetoothState.STATE_CONNECTED)
                bt.disconnect();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Activity on bluetooth test
        if (!bt.isBluetoothEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if(!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_ANDROID);
                setup();
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if(resultCode == Activity.RESULT_OK)
                bt.connect(data);
        } else if(requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if(resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_ANDROID);
                setup();
            } else {
                Toast.makeText(getContext()
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bt.disconnect();
        bt.stopService();
    }

    // Functions------------------------------------------------------------------------------------
    public void setup() {

        btnSend.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                createRequest();
                bt.send(requestInBytes.peekAll(), false);
            }
        });

        btnSendCoil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createRequestCoil();
                bt.send(requestInBytes.peekAll(), false);
            }
        });
    }

    public void createRequestCoil(){
        requestInBytes = new ByteQueue();

        int i = Integer.parseInt(editCoilAddres.getText().toString());
        int j = Integer.parseInt(editCoilValue.getText().toString());
        boolean b = j == 1;
        mCoilRequest = new WriteCoilRequest(i, b);
        mCoilRequest.setUnitID(1);
        mCoilRequest.setHeadless();
        requestInBytes.push(mCoilRequest.getUnitID());
        requestInBytes.push(mCoilRequest.getFunctionCode());
        requestInBytes.push(mCoilRequest.getMessage());

        int[] crc = ModbusUtil.calculateCRC(requestInBytes.peekAll(), 0,
                requestInBytes.peekAll().length);

        requestInBytes.push(crc[0]);
        requestInBytes.push(crc[1]);


        Log.d("request", requestInBytes.toString());
    }


    public void createRequest(){
        requestInBytes = new ByteQueue();

        int i = Integer.parseInt(editRgAddress.getText().toString());
        int j = Integer.parseInt(editRegValue.getText().toString());

        SimpleRegister simpleRegister = new SimpleRegister(j);
        mRegRequest = new WriteSingleRegisterRequest(i, simpleRegister);
        mRegRequest.setUnitID(1);
        mRegRequest.setHeadless();
        requestInBytes.push(mRegRequest.getUnitID());
        requestInBytes.push(mRegRequest.getFunctionCode());
        requestInBytes.push(mRegRequest.getMessage());

        int[] crc = ModbusUtil.calculateCRC(requestInBytes.peekAll(), 0,
                requestInBytes.peekAll().length);

        requestInBytes.push(crc[0]);
        requestInBytes.push(crc[1]);

        Log.d("request", requestInBytes.toString());
    }


}


//switch (functionCode) {
//        case Modbus.READ_COILS:
//        request = new ReadCoilsRequest();
//        break;
//        case Modbus.READ_INPUT_DISCRETES:
//        request = new ReadInputDiscretesRequest();
//        break;
//        case Modbus.READ_MULTIPLE_REGISTERS:
//        request = new ReadMultipleRegistersRequest();
//        break;
//        case Modbus.READ_INPUT_REGISTERS:
//        request = new ReadInputRegistersRequest();
//        break;
//        case Modbus.WRITE_COIL:
//        request = new WriteCoilRequest();
//        break;
//        case Modbus.WRITE_SINGLE_REGISTER:
//        request = new WriteSingleRegisterRequest();
//        break;
//        case Modbus.WRITE_MULTIPLE_COILS:
//        request = new WriteMultipleCoilsRequest();
//        break;
//        case Modbus.WRITE_MULTIPLE_REGISTERS:
//        request = new WriteMultipleRegistersRequest();
//        break;
//        case Modbus.READ_EXCEPTION_STATUS:
//        request = new ReadExceptionStatusRequest();
//        break;
//        case Modbus.READ_SERIAL_DIAGNOSTICS:
//        request = new ReadSerialDiagnosticsRequest();
//        break;
//        case Modbus.READ_COMM_EVENT_COUNTER:
//        request = new ReadCommEventCounterRequest();
//        break;
//        case Modbus.READ_COMM_EVENT_LOG:
//        request = new ReadCommEventLogRequest();
//        break;
//        case Modbus.REPORT_SLAVE_ID:
//        request = new ReportSlaveIDRequest();
//        break;
//        case Modbus.READ_FILE_RECORD:
//        request = new ReadFileRecordRequest();
//        break;
//        case Modbus.WRITE_FILE_RECORD:
//        request = new WriteFileRecordRequest();
//        break;
//        case Modbus.MASK_WRITE_REGISTER:
//        request = new MaskWriteRegisterRequest();
//        break;
//        case Modbus.READ_WRITE_MULTIPLE:
//        request = new ReadWriteMultipleRequest();
//        break;
//        case Modbus.READ_FIFO_QUEUE:
//        request = new ReadFIFOQueueRequest();
//        break;
//        case Modbus.READ_MEI:
//        request = new ReadMEIRequest();
//        break;
//default:
//        request = new IllegalFunctionRequest(functionCode);
//        break;
//        }
//        return request;
//        }


//        SlaveId           11
//        Function	        05
//        Coil Address Hi	00
//        Coil Address Lo	AC
//        Write Data Hi	FF	0 0
//        Write Data Lo	00	F F
//        Error Check Lo	4E
//        Error Check Hi	8B
//        Trailer	None	CR LF
//        Total Bytes	    8