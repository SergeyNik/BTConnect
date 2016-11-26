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

import app.sergeynik.btconnect.R;

public class DebugActivity extends AppCompatActivity {

    private boolean isCreateReq = false;
    private TextView response;
    private EditText request;
    private Button btnCreateReq;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        response = (TextView) findViewById(R.id.txt_read_response);
        request = (EditText) findViewById(R.id.edit_current_request);
        btnCreateReq = (Button) findViewById(R.id.btn_send_request);

        if (request.getText().toString().equals("Request") ||
                request.getText().toString().equals("")){
            btnCreateReq.setText("Create");
            isCreateReq = true;
        }

        request.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (request.getText().toString().equals("Request") ||
                        request.getText().toString().equals("")){
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
                    intent = new Intent(DebugActivity.this, CreateReqActivity.class);
                    startActivityForResult(intent, 1);
                }
            }
        });


    }
}
