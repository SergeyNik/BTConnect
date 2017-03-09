package app.sergeynik.btconnect;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


/**
 * A simple {@link Fragment} subclass.
 */
public class NumPadFragment extends Fragment implements View.OnClickListener{

    private Button btnOne;
    private Button btnTwo;
    private Button btnThree;
    private Button btnFour;
    private Button btnFive;
    private Button btnSix;
    private Button btnSeven;
    private Button btnEight;
    private Button btnNine;
    private Button btnZero;
    private NumPadCallback thisCallback;

    public NumPadFragment() {
        // Required empty public constructor
    }


    public interface NumPadCallback {
        void numPadReturn(int id);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        thisCallback = (NumPadCallback)activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_num_pad, container, false);

        btnOne = (Button) view.findViewById(R.id.btn_one);
        btnTwo = (Button) view.findViewById(R.id.btn_two);
        btnThree = (Button) view.findViewById(R.id.btn_three);
        btnFour = (Button) view.findViewById(R.id.btn_four);
        btnFive = (Button) view.findViewById(R.id.btn_five);
        btnSix = (Button) view.findViewById(R.id.btn_six);
        btnSeven = (Button) view.findViewById(R.id.btn_seven);
        btnEight = (Button) view.findViewById(R.id.btn_eight);
        btnNine = (Button) view.findViewById(R.id.btn_nine);
        btnZero = (Button) view.findViewById(R.id.btn_zero);

        btnOne.setOnClickListener(this);
        btnTwo.setOnClickListener(this);
        btnThree.setOnClickListener(this);
        btnFour.setOnClickListener(this);
        btnFive.setOnClickListener(this);
        btnSix.setOnClickListener(this);
        btnSeven.setOnClickListener(this);
        btnEight.setOnClickListener(this);
        btnNine.setOnClickListener(this);
        btnZero.setOnClickListener(this);

        return view;

    }
    @Override
    public void onDetach() {
        super.onDetach();
        thisCallback = null;
    }

    @Override
    public void onClick(View v) {
        thisCallback.numPadReturn(v.getId());
    }

}
