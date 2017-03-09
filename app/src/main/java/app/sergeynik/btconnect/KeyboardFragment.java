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
public class KeyboardFragment extends Fragment implements View.OnClickListener {

    private Button btnEscape;
    private Button btnToNumpad;
    private Button btnEnter;
    private Button btnLeft;
    private Button btnRight;
    private Button btnUp;
    private Button btnDown;
    private KeyboardCallback thisCallback;

    public KeyboardFragment() {
        // Required empty public constructor
    }

    public interface KeyboardCallback {
        void keyBoardReturn(int id);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        thisCallback = (KeyboardCallback)activity;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_keyboard, container, false);

        btnEscape = (Button) view.findViewById(R.id.btn_escape);
        btnToNumpad = (Button) view.findViewById(R.id.btn_to_numpad);
        btnEnter = (Button) view.findViewById(R.id.btn_enter);
        btnLeft = (Button) view.findViewById(R.id.btn_left);
        btnRight = (Button) view.findViewById(R.id.btn_right);
        btnUp = (Button) view.findViewById(R.id.btn_up);
        btnDown = (Button) view.findViewById(R.id.btn_down);

        btnEscape.setOnClickListener(this);
        btnToNumpad.setOnClickListener(this);
        btnEnter.setOnClickListener(this);
        btnLeft.setOnClickListener(this);
        btnRight.setOnClickListener(this);
        btnUp.setOnClickListener(this);
        btnDown.setOnClickListener(this);

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        thisCallback = null;
    }

    @Override
    public void onClick(View v) {
        thisCallback.keyBoardReturn(v.getId());
    }
}
