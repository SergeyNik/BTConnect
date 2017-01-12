package app.sergeynik.debug;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import app.sergeynik.btconnect.R;

public class NumberPadActivity extends AppCompatActivity {
    public static final String EXTRA_OUT_VALUE = "app.sergeynik.debug.menu_value";
    private static final int SLAVE_ID = 0;
    private static final int FUNCTION = 1;
    private static final int ADDRESS = 2;
    private static final int VALUE = 3;
    Button btnOne, btnTwo, btnThree, btnFour, btnFive,
            btnSix, btnSeven, btnEight, btnNine, btnMinus, btnZero, btnDot, btnDelete, btnOk;
    TextView txtResult;
    private StringBuilder string;
    private Pattern pattern;
    private Matcher matcher;
    private int typeOfMessPart;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_number_pad);

        string = new StringBuilder("0");

        txtResult = (TextView) findViewById(R.id.txt_enter_result);
        btnOne = (Button) findViewById(R.id.btn_one);
        btnTwo = (Button) findViewById(R.id.btn_two);
        btnThree = (Button) findViewById(R.id.btn_three);
        btnFour = (Button) findViewById(R.id.btn_four);
        btnFive = (Button) findViewById(R.id.btn_five);
        btnSix = (Button) findViewById(R.id.btn_six);
        btnSeven = (Button) findViewById(R.id.btn_seven);
        btnEight = (Button) findViewById(R.id.btn_eight);
        btnNine = (Button) findViewById(R.id.btn_nine);
        btnMinus = (Button) findViewById(R.id.btn_minus);
        btnZero = (Button) findViewById(R.id.btn_zero);
        btnDot = (Button) findViewById(R.id.btn_dot);
        btnDelete = (Button) findViewById(R.id.btn_delete);
        btnOk = (Button) findViewById(R.id.btn_ok);

        // Get clicked position
        final Intent intent = getIntent();
        typeOfMessPart = intent.getIntExtra(DebugActivity2.EXTRA_POS_ID, 1);

        btnOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lastCharHandler("1");
                txtResult.setText(string.toString());
            }
        });
        btnTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lastCharHandler("2");
                txtResult.setText(string.toString());
            }
        });
        btnThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lastCharHandler("3");
                txtResult.setText(string.toString());
            }
        });
        btnFour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lastCharHandler("4");
                txtResult.setText(string.toString());
            }
        });
        btnFive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lastCharHandler("5");
                txtResult.setText(string.toString());
            }
        });
        btnSix.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lastCharHandler("6");
                txtResult.setText(string.toString());
            }
        });
        btnSeven.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lastCharHandler("7");
                txtResult.setText(string.toString());
            }
        });
        btnEight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lastCharHandler("8");
                txtResult.setText(string.toString());
            }
        });
        btnNine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lastCharHandler("9");
                txtResult.setText(string.toString());
            }
        });
        btnMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String substring = string.substring(0, string.length());
                pattern = Pattern.compile("\\-");
                matcher = pattern.matcher(substring);
                if(!matcher.find()){
                    StringBuilder temp = new StringBuilder("-");
                    temp.append(string);
                    string = temp;
                    txtResult.setText(string.toString());
                }
            }
        });
        btnZero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!string.substring(0).equals("0")){
                    string.append("0");
                    txtResult.setText(string.toString());
                }
            }
        });
        btnDot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String substring = string.substring(0, string.length());
                pattern = Pattern.compile("\\.");
                matcher = pattern.matcher(substring);
                if(!matcher.find()){
                    string.append(".");
                    txtResult.setText(string.toString());
                }
            }
        });
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(string.length() > 0){
                    string.deleteCharAt(string.length() - 1);
                }
                if(string.length() == 0){
                    string.append("0");
                }
                txtResult.setText(string.toString());
            }
        });
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                int iResult;
                switch (typeOfMessPart){
                    case SLAVE_ID:
                        // 0-255
                        try {
                            iResult = Integer.parseInt(string.toString());
                            if(iResult > 255){
                                iResult = 255;
                            } else if(iResult < 0){
                                iResult = 0;
                            }
                        } catch (NumberFormatException e){
                            iResult = 0;
                        }
                        data.putExtra("slave_id", iResult);
                        break;
                    case FUNCTION:
                        // 0-15
                        try {
                            iResult = Integer.parseInt(string.toString());
                            if(iResult > 15){
                                iResult = 15;
                            } else if(iResult < 0){
                                iResult = 0;
                            }
                        } catch (NumberFormatException e){
                            iResult = 0;
                        }
                        data.putExtra("function", iResult);
                        break;
                    case ADDRESS:
                        // 1-49999
                        try {
                            iResult = Integer.parseInt(string.toString());
                            if(iResult > 49999){
                                iResult = 49999;
                            } else if(iResult < 1){
                                iResult = 1;
                            }
                        } catch (NumberFormatException e){
                            iResult = 0;
                        }
                        data.putExtra("address", iResult);
                        break;
                    case VALUE:
                        try {
                            iResult = Integer.parseInt(string.toString());
                        } catch (NumberFormatException e){
                            iResult = 0;
                        }
                        // выборка bool, int, double, float, uint16
                        data.putExtra("value", iResult);
                        break;
                }
                setResult(RESULT_OK, data);
                finish();

//                int d = Integer.parseInt(string.toString());
//                txtResult.setText("");

                //- 2,147,483,648
                // 2,147,483,647
            }
        });

    }

    private void lastCharHandler(String s) {
        if(string.length() == 1 && string.substring(0).equals("0")){
            string.deleteCharAt(0);
            string.append(s);
        } else if (string.length() >= 1){
            string.append(s);
        } else if(string.length() == 0){
            string.append(s);
        }
    }
}
