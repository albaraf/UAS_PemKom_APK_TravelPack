/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.travelpackchecklist;

/**
 *
 * @author ASUS
 */
import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class Main {
    private static Map<String, User> users = new HashMap<>();
    private static java.util.List<ChecklistItem<String>> checklist = new ArrayList<>();
    private static final String USERS_FILE = "users.ser";
    private static final String CHECKLIST_FILE = "checklist.ser";

    public static void main(String[] args) {
        loadUsers();
        loadChecklist();
        SwingUtilities.invokeLater(Main::showLoginForm);
    }

    private static void showLoginForm() {
        JFrame frame = new JFrame("TravelPack Login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 150);

        JPanel panel = new JPanel(new GridLayout(3, 2));
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JButton loginButton = new JButton("Login");

        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(new JLabel());
        panel.add(loginButton);

        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String hash = PasswordUtil.hash(password);

            if (users.containsKey(username)) {
                if (users.get(username).getPasswordHash().equals(hash)) {
                    frame.dispose();
                    showChecklistUI();
                } else {
                    JOptionPane.showMessageDialog(frame, "Password salah.");
                }
            } else {
                users.put(username, new User(username, hash));
                saveUsers();
                JOptionPane.showMessageDialog(frame, "User baru dibuat.");
                frame.dispose();
                showChecklistUI();
            }
        });

        frame.add(panel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void showChecklistUI() {
        JFrame frame = new JFrame("TravelPack - Checklist Perjalanan");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);

        DefaultListModel<ChecklistItem<String>> model = new DefaultListModel<>();
        checklist.forEach(model::addElement);
        JList<ChecklistItem<String>> list = new JList<>(model);
        JScrollPane scrollPane = new JScrollPane(list);

        JButton addButton = new JButton("Tambah Barang");
        JButton markButton = new JButton("Tandai Dibawa");
        JButton saveButton = new JButton("Simpan");

        addButton.addActionListener(e -> {
            String item = JOptionPane.showInputDialog(frame, "Nama barang:");
            if (item != null && !item.isBlank()) {
                int result = JOptionPane.showConfirmDialog(frame, "Apakah barang penting?", "Penting?", JOptionPane.YES_NO_OPTION);
                boolean penting = (result == JOptionPane.YES_OPTION);
                ChecklistItem<String> newItem = new ChecklistItem<>(item, penting);
                checklist.add(newItem);
                model.addElement(newItem);
            }
        });

        markButton.addActionListener(e -> {
            int selected = list.getSelectedIndex();
            if (selected != -1) {
                model.get(selected).setPacked(true);
                list.repaint();
            }
        });

        saveButton.addActionListener(e -> saveChecklist());

        JPanel panel = new JPanel();
        panel.add(addButton);
        panel.add(markButton);
        panel.add(saveButton);

        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(panel, BorderLayout.SOUTH);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void loadUsers() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(USERS_FILE))) {
            users = (Map<String, User>) ois.readObject();
        } catch (Exception ignored) {}
    }

    private static void saveUsers() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USERS_FILE))) {
            oos.writeObject(users);
        } catch (IOException ignored) {}
    }

    private static void saveChecklist() {
    try (PrintWriter writer = new PrintWriter(new FileWriter(CHECKLIST_FILE))) {
        for (ChecklistItem<String> item : checklist) {
            writer.printf("%s,%b,%b%n",
                item.getItem().replace(",", " "),  // hilangkan koma agar tidak rusak format CSV
                item.isImportant(),
                item.isPacked());
        }
        JOptionPane.showMessageDialog(null, "Checklist disimpan dalam format tabel (CSV).");
    } catch (IOException e) {
        JOptionPane.showMessageDialog(null, "Gagal menyimpan checklist.");
    }
}


    private static void loadChecklist() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(CHECKLIST_FILE))) {
            checklist = (List<ChecklistItem<String>>) ois.readObject();
        } catch (Exception ignored) {}
    }
}
