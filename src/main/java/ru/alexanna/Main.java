package ru.alexanna;

import jssc.SerialPortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.alexanna.portreceiver.EquipmentOperationReceiver;
import ru.alexanna.portreceiver.SerialPortReceiver;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) throws SerialPortException, InterruptedException {
        SerialPortReceiver receiver = new EquipmentOperationReceiver(30);
        List<String> portNames = receiver.getPortNames();
        log.debug("Ports {}", portNames);
        String portName = portNames.get(1);
        receiver.setPortParams(portName, 9600, false);
        receiver.setAddresses(List.of(9, 10, 11, 12, 13, 14, 15, 16));
        receiver.openPort();
        Thread thread = new Thread(receiver);
        thread.setName("Thread-" + portName);
        thread.start();
        Timer timer = new Timer("Timer-" + portName,true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Map<Integer, byte[]> bytes = receiver.getReceivedBytes();
                log.info("Map {}", bytes);
                bytes.forEach((key, val) -> log.info("{} - {}", key, receiver.getLogString(val)));
            }
        }, 1000, 1000);

        Thread.sleep(3000);
        thread.interrupt();
    }
}