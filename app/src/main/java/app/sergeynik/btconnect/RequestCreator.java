package app.sergeynik.btconnect;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersRequest;
import com.ghgande.j2mod.modbus.msg.WriteFileRecordRequest;
import com.ghgande.j2mod.modbus.util.ModbusUtil;
import com.serotonin.util.queue.ByteQueue;

/**
 * Created by Sergey on 08.03.2017.
 */

public class RequestCreator {
    private static final int FILE = 16;
    private static final int ADDRES_WRITE = 30576;
    private static final int ADDRES_READ = 1956;
    private static final int SLAVE_ID = 1;
    private static final byte STRANGE_BIT = 0x01;

    private int pressedKey = 0;

    private void readMultipleRegistersRequest(ByteQueue byteQueue){

        ReadMultipleRegistersRequest readMultRegsReq = new ReadMultipleRegistersRequest();
        readMultRegsReq.setUnitID(SLAVE_ID);
        readMultRegsReq.setHeadless();
        readMultRegsReq.setReference(ADDRES_READ);

        readMultRegsReq.setWordCount(40);
        byteQueue.push(readMultRegsReq.getUnitID());
        byteQueue.push(readMultRegsReq.getFunctionCode());
        byteQueue.push(readMultRegsReq.getMessage());
        // CRC
        int[] crc2 = ModbusUtil.calculateCRC(byteQueue.peekAll(), 0,
                byteQueue.peekAll().length);

        byteQueue.push(crc2[0]);
        byteQueue.push(crc2[1]);
    }

    private void writeFileRecordRequest(ByteQueue byteQueue){
        WriteFileRecordRequest fileReq = new WriteFileRecordRequest();
        fileReq.setUnitID(SLAVE_ID);
        fileReq.setHeadless();
        WriteFileRecordRequest.RecordRequest recReq =
                new WriteFileRecordRequest.RecordRequest(
                        FILE,
                        ADDRES_WRITE,
                        new short[]{(short) pressedKey} // Value
                );
        fileReq.addRequest(recReq);

        byteQueue.push(fileReq.getUnitID());
        byteQueue.push(fileReq.getFunctionCode());
        byteQueue.push(fileReq.getMessage());
        byte popLast = byteQueue.tailPop();
        byte popPenulte = byteQueue.tailPop();
        byteQueue.push(popLast);
        byteQueue.push(STRANGE_BIT);
        // CRC
        int[] crc4 = ModbusUtil.calculateCRC(byteQueue.peekAll(), 0,
                byteQueue.peekAll().length);
        byteQueue.push(crc4[0]);
        byteQueue.push(crc4[1]);
    }

    synchronized byte[] createRequest(int requestType) {
        ByteQueue byteQueue = new ByteQueue();
        switch (requestType) {
            case Modbus.READ_MULTIPLE_REGISTERS:
                readMultipleRegistersRequest(byteQueue);
                break;
            case Modbus.WRITE_FILE_RECORD:
                writeFileRecordRequest(byteQueue);
                break;
            default:
                break;
        }
        return byteQueue.peekAll();
    }

    public void setPressedKey(int pressedKey) {
        this.pressedKey = pressedKey;
    }
}
