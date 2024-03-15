package ru.alexanna.portreceiver;

import jssc.SerialPortException;

public class EquipmentOperationReceiver extends SerialPortReceiver {
    private final int answerByteNumber;

    public EquipmentOperationReceiver(int answerByteNumber) {
        this.answerByteNumber = answerByteNumber;
    }

    @Override
    byte[] createRequest(int chkPntAddress) {
        byte[] request = new byte[13];
        request[0] = (byte) 0xfe;
        request[1] = loByte(chkPntAddress);
        request[2] = hiByte(chkPntAddress);
        request[3] = 0x06;
        for (int i = 4; i <= 9; i++)
            request[i] = (byte) 0xfd;
        int checkSum = calcCheckSum(request, 10);
        request[10] = loByte(checkSum);
        request[11] = hiByte(checkSum);
        request[12] = 0;
        return request;
    }

    @Override
    void completeReceivingProcess() throws SerialPortException {
            for (Integer address : getAddresses()) {
                sendRequest(address);
                byte[] acceptBytes = acceptAnswer(answerByteNumber);
                putAcceptedBytes(address, acceptBytes);
            }
    }

}
