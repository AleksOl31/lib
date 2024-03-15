package ru.alexanna.lib.portreceiver;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static jssc.SerialPort.DATABITS_8;
import static jssc.SerialPort.STOPBITS_1;


@SuppressWarnings("unused")
public abstract class SerialPortReceiver implements Runnable {

    private SerialPort port;
    private int baudRate;
    private boolean parity;
    private List<Integer> addresses;
    private final Map<Integer, byte[]> receivedByteCollector = new HashMap<>();
    private volatile Map<Integer, byte[]> bytesReceived;
    private static final Logger log = LoggerFactory.getLogger(SerialPortReceiver.class);

    abstract byte[] createRequest(int chkPntAddress);
    abstract void completeReceivingProcess() throws SerialPortException;

    public SerialPortReceiver() {}

    public SerialPortReceiver(String portName, int baudRate, boolean parity) {
        this();
        setPortParams(portName, baudRate, parity);
    }

    public void setPortParams(String portName, int baudRate, boolean parity) {
        this.port = new SerialPort(portName);
        this.baudRate = baudRate;
        this.parity = parity;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                completeReceivingProcess();
                setReceivedBytes(receivedByteCollector);
            } catch (SerialPortException e) {
                log.error(e.getMessage());
            }
        }
        try {
            closePort();
        } catch (SerialPortException e) {
            log.error(e.getMessage());
        }
    }

    public void setAddresses(List<Integer> addresses) {
        this.addresses = addresses;
    }

    public List<Integer> getAddresses() {
        return addresses;
    }

    public List<String> getPortNames() {
        return Arrays.asList(SerialPortList.getPortNames());
    }

    public void openPort() throws SerialPortException {
        port.openPort();
        port.setParams(baudRate, DATABITS_8, STOPBITS_1, parity ? 1 : 0);
        log.debug("Port {} opened", port.getPortName());
    }

    public void closePort() throws SerialPortException {
        port.closePort();
        log.debug("Port {} closed", port.getPortName());
    }

    public void sendRequest(int address) throws SerialPortException {
        byte[] request = createRequest(address);
        port.writeBytes(request);
        log.debug("Request sent to {}: {}", address, getLogString(request));
    }

    public byte[] acceptAnswer(int byteNumber) throws SerialPortException {
        log.debug("Wait for read port {}...", port.getPortName());
        byte[] buffer = port.readBytes(byteNumber);
        log.debug("Data read, from check point address {}: {}", buffer[1], getLogString(buffer));
        return buffer;
    }

    public int calcCheckSum(byte[] byteArr, int byteNumber) {
        int result = 0;
        for (int i = 0; i < byteNumber; i++) {
            result += unsignedByte(byteArr[i]);
        }
        return result;
    }

    public static String getLogString(byte[] byteArr) {
        StringBuilder builder = new StringBuilder();
        for (byte b : byteArr) {
            builder.append(String.format("%02X", unsignedByte(b)))
                    .append(" ");
        }
        return builder.toString();
    }

    public static int unsignedByte(byte b) {
        return b & 0xff;
    }

    public static byte loByte(int value) {
        return (byte) (value & 0xff);
    }

    public static byte hiByte(int value) {
        return (byte) ((value >> 8) & 0xff);
    }

    public void putAcceptedBytes(Integer key, byte[] acceptedArr) {
        receivedByteCollector.put(key, acceptedArr);
    }

    private void setReceivedBytes(Map<Integer, byte[]> map) {
        bytesReceived = map;
    }

    public Map<Integer, byte[]> getReceivedBytes() {
        return bytesReceived;
    }
}
