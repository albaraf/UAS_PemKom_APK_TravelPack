/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.travelpackchecklist;

/**
 *
 * @author ASUS
 */
import com.fazecast.jSerialComm.SerialPort;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.*;

public class SerialManager {
    private SerialPort serialPort;
    private InputStream inputStream;

    public void connect(String portName) {
        serialPort = SerialPort.getCommPort(portName);
        serialPort.setBaudRate(9600);

        if (serialPort.openPort()) {
            System.out.println("Terhubung ke " + portName);
            inputStream = serialPort.getInputStream(); // ✅ ditangani dengan try-catch
        } else {
            JOptionPane.showMessageDialog(null, "Gagal membuka port serial: " + portName);
        }
    }

    public void listenSerialInput() {
        if (inputStream == null) return;

        new Thread(() -> {
            try {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = inputStream.read(buffer)) > -1) {
                    String received = new String(buffer, 0, len);
                    System.out.println("Data masuk: " + received);
                    // Tambahkan aksi sesuai kebutuhan
                }
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Terjadi kesalahan saat membaca data serial.");
            }
        }).start();
    }
}
