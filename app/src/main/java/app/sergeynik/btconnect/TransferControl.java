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

    private final char[] decodeTable = new char[]{
            '1' + '2' + '3' + 0x16 +
            '4' + '5' + '6' + 0x17 +
            '7' + '8' + '9' + 0x30 +
            0x24 + '0' + 0x25 + 0x31 +
            0x27, + 0x26};




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
