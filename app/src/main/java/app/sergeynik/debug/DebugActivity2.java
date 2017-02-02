package app.sergeynik.debug;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ghgande.j2mod.modbus.msg.WriteCoilRequest;
import com.ghgande.j2mod.modbus.msg.WriteSingleRegisterRequest;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;
import com.ghgande.j2mod.modbus.util.ModbusUtil;
import com.serotonin.util.queue.ByteQueue;

import java.util.Arrays;
import java.util.List;

import app.sergeynik.btconnect.R;

public class DebugActivity2 extends AppCompatActivity {
    private final int REQUEST_CODE_SLAVE = 0;
    private final int REQUEST_CODE_FUNC = 1;
    private final int REQUEST_CODE_ADDR = 2;
    private final int REQUEST_CODE_VAL = 3;
    private WriteCoilRequest mWrCoilReq;
    private WriteSingleRegisterRequest mWrSingleRegReq;
    private String[] mParams;
    private RecyclerView mRecycler;
    private RecyclerView.Adapter mAdapter;
    private  RecyclerView.LayoutManager mLayoutManager;
    public static final String EXTRA_POS_ID =
            "app.sergeynik.debug.menu_position";

    private TextView txtReqConstr;
    private ByteQueue mByteQueue;
    private int slaveId;
    private int function;
    private int address;
    private int value;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            switch (requestCode) {
                case REQUEST_CODE_SLAVE:
                    slaveId = data.getIntExtra("slave_id", 1);
                    break;
                case REQUEST_CODE_FUNC:
                    function = data.getIntExtra("function", 1);
                    break;
                case REQUEST_CODE_ADDR:
                    address = data.getIntExtra("address", 1);
                    break;
                case REQUEST_CODE_VAL:
                    value = data.getIntExtra("value", 1);
                    break;
            }
           // txtReqConstr.setText(mByteQueue.peekAll().toString());
        } else {
            Log.d("MESSAGA", "errorNotOK");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_req2);
        mByteQueue = new ByteQueue();
        txtReqConstr = (TextView) findViewById(R.id.str_request_constructor);

        mRecycler = (RecyclerView) findViewById(R.id.create_req_recycler);
        mRecycler.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecycler.setLayoutManager(mLayoutManager);

        mParams = getResources().getStringArray(R.array.req_params);

        mAdapter = new RequestAdapter(Arrays.asList(mParams));
        mRecycler.setAdapter(mAdapter);
    }


    @Override
    public void finish() {
        Intent data = new Intent();
        createRequest(slaveId, function, address, value, 1);
        data.putExtra("REQUEST", mByteQueue.peekAll());
        Log.d("MESSAGA", mByteQueue.toString());
        setResult(RESULT_OK, data);
        super.finish();
    }

    private class RequestHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mTitle;

        public RequestHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mTitle = (TextView) itemView.findViewById(R.id.req_params_name);
        }

        @Override
        public void onClick(View v) {
            Intent intent;
            Snackbar.make(v, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();

            intent = new Intent(getApplicationContext(), NumberPadActivity.class);
            intent.putExtra(EXTRA_POS_ID, getAdapterPosition());
            switch (getAdapterPosition()){
                case REQUEST_CODE_SLAVE:
                    startActivityForResult(intent, REQUEST_CODE_SLAVE);
                    break;
                case REQUEST_CODE_FUNC:
                    startActivityForResult(intent, REQUEST_CODE_FUNC);
                    break;
                case REQUEST_CODE_ADDR:
                    startActivityForResult(intent, REQUEST_CODE_ADDR);
                    break;
                case REQUEST_CODE_VAL:
                    startActivityForResult(intent, REQUEST_CODE_VAL);
                    break;
            }
        }
    }


    private class RequestAdapter extends RecyclerView.Adapter<RequestHolder> {

        private List<String> paramsReq;

        public RequestAdapter(List<String> paramsReq) {
            this.paramsReq = paramsReq;
        }

        @Override
        public RequestHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_request_params, parent, false);
            // set the view's size, margins, paddings and layout parameters
            return new RequestHolder(v);
        }

        @Override
        public void onBindViewHolder(RequestHolder holder, int position) {
            holder.mTitle.setText(paramsReq.get(position));
        }

        @Override
        public int getItemCount() {
            return paramsReq.size();
        }
    }


    public void createRequest(int slaveId, int func, int address, int value, int reqType){
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
