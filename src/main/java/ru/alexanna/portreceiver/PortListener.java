package ru.alexanna.lib.portreceiver;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@SuppressWarnings("unused")
abstract class PortListener implements SerialPortEventListener {
    private final SerialPort port;
    private static final Logger log = LoggerFactory.getLogger(PortListener.class);

    public PortListener(SerialPort port) {
        this.port = port;
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        log.debug("EventValue - {}, isRXCHAR - {}", event.getEventValue(), event.isRXCHAR());

        if (event.isRXCHAR() && event.getEventValue() > 0) {
            try {
                //Получаем ответ от устройства, обрабатываем данные и т.д.
                readData(event.getEventValue());
                //И снова отправляем запрос
                Thread.sleep(300);
                sendRequest();
            } catch (SerialPortException ex) {
                log.error(ex.getMessage());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public abstract byte[] readData(int byteNumber) throws SerialPortException;

    public abstract void sendRequest() throws SerialPortException;

    private static int unsignedByte(byte b) {
        return b & 0xff;
    }

    private static int calcCheckSum(byte[] byteArr, int byteNumber) {
        int result = 0;
        for (int i = 0; i < byteNumber; i++) {
            result += unsignedByte(byteArr[i]);
        }
        return result;
    }

    private static byte loByte(int value) {
        return (byte) (value & 0xff);
    }

    private static byte hiByte(int value) {
        return (byte) ((value >> 8) & 0xff);
    }

/*    private byte[] createRequest(int address) {
        byte[] request = new byte[13];
        request[0] = (byte) 0xfe;
        request[1] = loByte(address);
        request[2] = hiByte(address);
        request[3] = 0x06;
        for (int i = 4; i <= 9; i++)
            request[i] = (byte) 0xfd;
        int checkSum = calcCheckSum(request, 10);
        request[10] = loByte(checkSum);
        request[11] = hiByte(checkSum);
        request[12] = 0;
        return request;
    }*/

}
