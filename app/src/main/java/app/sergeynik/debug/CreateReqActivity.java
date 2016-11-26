package app.sergeynik.debug;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

        mByteQueue= new ByteQueue();
    }






    public void createRequestCoil(int reqType){

        switch (reqType){
            case 0:
                mWrCoilReq = new WriteCoilRequest(1000, true);
                mWrCoilReq.setUnitID(1);
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
                SimpleRegister register = new SimpleRegister(10);
                mWrSingleRegReq = new WriteSingleRegisterRequest(1000, register);
                mWrSingleRegReq.setUnitID(1);
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


//        int i = Integer.parseInt(editCoilAddres.getText().toString());
//        int j = Integer.parseInt(editCoilValue.getText().toString());

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