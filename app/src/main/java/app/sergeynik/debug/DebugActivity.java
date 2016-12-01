package app.sergeynik.debug;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.serotonin.util.queue.ByteQueue;

import app.sergeynik.btconnect.R;
import app.sergeynik.btconnect.TransferControl;
import app.sergeynik.library.BluetoothSPP;

public class DebugActivity extends AppCompatActivity {

    private BluetoothSPP bt;
    private ByteQueue mByteQueue;
    private ByteQueue screenBytes;
    private TransferControl mControl;
    private boolean isCreateReq = false;
    private TextView txtResponse;
    private EditText edRequest;
    private Button btnCreateReq;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        byte[] act_result = data.getByteArrayExtra("REQUEST");
        mByteQueue.push(act_result);
        edRequest.setText(mByteQueue.toString());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        mControl = TransferControl.getInstance();

        txtResponse = (TextView) findViewById(R.id.txt_read_response);
        edRequest = (EditText) findViewById(R.id.edit_current_request);
        btnCreateReq = (Button) findViewById(R.id.btn_send_request);

        mByteQueue = new ByteQueue();
        screenBytes = new ByteQueue();
        if (edRequest.getText().toString().equals("Request") ||
                edRequest.getText().toString().equals("")){
            btnCreateReq.setText("Create");
            isCreateReq = true;
        }

        edRequest.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (edRequest.getText().toString().equals("Request") ||
                        edRequest.getText().toString().equals("")){
                    btnCreateReq.setText("Create");
                    isCreateReq = true;
                } else {
                    btnCreateReq.setText("Send");
                    isCreateReq = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btnCreateReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                if(isCreateReq){
                    intent = new Intent(DebugActivity.this, DebugActivity2.class);
                    startActivityForResult(intent, 1);
                } else {
                    // send request
                    bt.send(mByteQueue.peekAll(), false);
                }
            }
        });

        bt = mControl.getSpp();

        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            @Override
            public void onDataReceived(byte[] data, String message) {
                screenBytes.push(data);
                txtResponse.append(screenBytes.toString() + "\n");
                screenBytes = new ByteQueue();
            }
        });



    }
}
