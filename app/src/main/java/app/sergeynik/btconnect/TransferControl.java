package app.sergeynik.btconnect;

/**
 * Created by Sergey on 26.11.2016.
 */
public class TransferControl {
    private static TransferControl ourInstance = new TransferControl();

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
}
