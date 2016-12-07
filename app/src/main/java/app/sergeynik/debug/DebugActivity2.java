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

import java.util.Arrays;
import java.util.List;

import app.sergeynik.btconnect.R;

public class DebugActivity2 extends AppCompatActivity {
    private String[] mParams;
    private RecyclerView mRecycler;
    private RecyclerView.Adapter mAdapter;
    private  RecyclerView.LayoutManager mLayoutManager;
    public static final String EXTRA_POS_ID =
            "app.sergeynik.debug.menu_position";

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data == null){
            return;
        }
        int myValue = data.getIntExtra(NumberPadActivity.EXTRA_OUT_VALUE, 1);
        Log.d("MESSAGA", String.valueOf(myValue));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_req2);

        mRecycler = (RecyclerView) findViewById(R.id.create_req_recycler);
        mRecycler.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecycler.setLayoutManager(mLayoutManager);

        mParams = getResources().getStringArray(R.array.req_params);

        mAdapter = new RequestAdapter(Arrays.asList(mParams));
        mRecycler.setAdapter(mAdapter);
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
            startActivityForResult(intent, 1);
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
            RequestHolder vh = new RequestHolder(v);
            return vh;
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
}
