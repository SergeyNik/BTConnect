package app.sergeynik.btconnect;

import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersRequest;
import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersResponse;

/**
 * Created by Sergey on 26.11.2016.
 */
public class TransferControl {
    private static TransferControl ourInstance = new TransferControl();

    ReadMultipleRegistersRequest mRegistersRequest;
    ReadMultipleRegistersResponse mRegistersResponse;
    private int mTransferType;
    private byte[] request;
    private byte[] response;

    public static TransferControl getInstance() {
        return ourInstance;
    }

    private TransferControl() {
        mTransferType = 1;
    }

    public int getTransferType() {
        return mTransferType;
    }

    public void putRequest(byte[] bytes) {
        request = bytes;
    }

    public byte[] getRequest() {
        return request;
    }

    public byte[] getResponse() {
        return response;
    }

    public void putResponse(byte[] response) {
        this.response = response;
    }

    public ReadMultipleRegistersRequest getRegistersRequest() {
        return mRegistersRequest;
    }

    public void setRegistersRequest(ReadMultipleRegistersRequest registersRequest) {
        mRegistersRequest = registersRequest;
    }

    public ReadMultipleRegistersResponse getRegistersResponse() {
        return mRegistersResponse;
    }

    public void setRegistersResponse(ReadMultipleRegistersResponse registersResponse) {
        mRegistersResponse = registersResponse;
    }
}
