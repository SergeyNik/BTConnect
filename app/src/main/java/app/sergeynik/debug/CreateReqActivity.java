package app.sergeynik.debug;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.ghgande.j2mod.modbus.msg.WriteCoilRequest;
import com.ghgande.j2mod.modbus.msg.WriteMultipleRegistersRequest;
import com.ghgande.j2mod.modbus.msg.WriteSingleRegisterRequest;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;
import com.ghgande.j2mod.modbus.util.ModbusUtil;
import com.serotonin.util.queue.ByteQueue;

import app.sergeynik.btconnect.R;

public class CreateReqActivity extends AppCompatActivity {

    private final String ACT_RESULT = "REQUEST";
    private int reqType;
    private ByteQueue mByteQueue;
    private WriteCoilRequest mWrCoilReq;
    private WriteSingleRegisterRequest mWrSingleRegReq;
    private WriteMultipleRegistersRequest mWrMultRegsReq;

    private EditText edSlaveID, edAddress, edValue;
    private Spinner spFunction;
    private Button btnCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_req);


        edSlaveID = (EditText) findViewById(R.id.edit_debug_slave_id);
        edAddress = (EditText) findViewById(R.id.edit_debug_address);
        edValue = (EditText) findViewById(R.id.edit_debug_value);
        spFunction = (Spinner) findViewById(R.id.spinner_debug_function);
        btnCreate = (Button) findViewById(R.id.btn_debug_create_request);

        mByteQueue = new ByteQueue();

        spFunction.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                reqType = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createRequest(reqType);

                Intent intent = new Intent();
                intent.putExtra(ACT_RESULT, mByteQueue.popAll());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    public void createRequest(int reqType){
        int slaveId = Integer.parseInt(edSlaveID.getText().toString());
        int address = Integer.parseInt(edAddress.getText().toString());
        int value = Integer.parseInt(edValue.getText().toString());

        switch (reqType){
            case 0:
                boolean val = (value == 1);
                mWrCoilReq = new WriteCoilRequest(address, val);
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
                mWrSingleRegReq = new WriteSingleRegisterRequest(address, register);
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
        }
    }
}
