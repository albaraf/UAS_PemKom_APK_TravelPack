package com.mycompany.travelpackchecklist;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*; // ✔ Generic
import com.mongodb.client.*; // ✔ Non-relational DB
import org.bson.Document;
import org.bson.conversions.Bson;
import static com.mongodb.client.model.Filters.*;

public class Main {
    private static Map<String, User> users = new HashMap<>(); // ✔ Generic
    private static java.util.List<ChecklistItem<String>> checklist = new ArrayList<>(); // ✔ Generic
    private static String currentUsername = null;

    private static final String CONNECTION_STRING = "mongodb://localhost:27017"; // ✔ Non-relational DB
    private static final String DB_NAME = "travelpackdb";
    private static final MongoClient mongoClient = MongoClients.create(CONNECTION_STRING); // ✔ Non-relational DB
    private static final MongoDatabase database = mongoClient.getDatabase(DB_NAME);
    private static final MongoCollection<Document> userCollection = database.getCollection("users");
    private static final MongoCollection<Document> checklistCollection = database.getCollection("checklist");

    public static void main(String[] args) {
        loadUsers();
        SwingUtilities.invokeLater(Main::showLoginForm); // ✔ Multi-threading
    }

    private static void showLoginForm() {
        JFrame frame = new JFrame(Messages.get("login.title")); // ✔ Internationalization
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 250);

        JPanel inputPanel = new JPanel(new GridLayout(4, 2));
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JComboBox<String> languageSelector = new JComboBox<>(new String[]{"Indonesia", "English", "العربية"});

        JLabel usernameLabel = new JLabel(Messages.get("label.username")); // ✔ Internationalization
        JLabel passwordLabel = new JLabel(Messages.get("label.password")); // ✔ Internationalization
        JLabel languageLabel = new JLabel(Messages.get("label.language")); // ✔ Internationalization

        inputPanel.add(usernameLabel);
        inputPanel.add(usernameField);
        inputPanel.add(passwordLabel);
        inputPanel.add(passwordField);
        inputPanel.add(languageLabel);
        inputPanel.add(languageSelector);

        JButton loginButton = new JButton(Messages.get("button.login")); // ✔ Internationalization
        JButton tambahUserButton = new JButton(Messages.get("button.adduser")); // ✔ Internationalization
        JButton hapusUserButton = new JButton(Messages.get("button.deleteuser")); // ✔ Internationalization

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(loginButton);
        buttonPanel.add(tambahUserButton);
        buttonPanel.add(hapusUserButton);

        languageSelector.addActionListener(e -> {
            String selectedLanguage = (String) languageSelector.getSelectedItem();
            setLocaleFromSelection(selectedLanguage);
            usernameLabel.setText(Messages.get("label.username"));
            passwordLabel.setText(Messages.get("label.password"));
            languageLabel.setText(Messages.get("label.language"));
            loginButton.setText(Messages.get("button.login"));
            tambahUserButton.setText(Messages.get("button.adduser"));
            hapusUserButton.setText(Messages.get("button.deleteuser"));
        });

        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String hash = PasswordUtil.hash(password); // ✔ Cryptography

            if (users.containsKey(username)) {
                if (users.get(username).getPasswordHash().equals(hash)) {
                    currentUsername = username;
                    frame.dispose();
                    loadChecklist();
                    showChecklistUI();
                } else {
                    JOptionPane.showMessageDialog(frame, Messages.get("login.wrongpassword"));
                }
            } else {
                JOptionPane.showMessageDialog(frame, Messages.get("login.usernotfound"));
            }
        });

        tambahUserButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            if (users.containsKey(username)) {
                JOptionPane.showMessageDialog(frame, Messages.get("user.exists"));
            } else {
                String hash = PasswordUtil.hash(password); // ✔ Cryptography
                users.put(username, new User(username, hash));
                saveUsers();
                JOptionPane.showMessageDialog(frame, Messages.get("user.added"));
            }
        });

        hapusUserButton.addActionListener(e -> {
            String username = usernameField.getText();
            if (!users.containsKey(username)) {
                JOptionPane.showMessageDialog(frame, Messages.get("login.usernotfound"));
            } else {
                users.remove(username);
                deleteUserFromDB(username);
                JOptionPane.showMessageDialog(frame, Messages.get("user.deleted"));
            }
        });

        frame.setLayout(new BorderLayout());
        frame.add(inputPanel, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void setLocaleFromSelection(String selectedLanguage) {
        Locale selectedLocale;
        switch (selectedLanguage) {
            case "Indonesia":
                selectedLocale = new Locale("id", "ID");
                break;
            case "العربية":
                selectedLocale = new Locale("ar");
                break;
            case "English":
            default:
                selectedLocale = new Locale("en");
                break;
        }
        Messages.setLocale(selectedLocale); // ✔ Internationalization
    }

    private static void showChecklistUI() {
        JFrame frame = new JFrame(Messages.get("checklist.title")); // ✔ Internationalization
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 500);

        DefaultListModel<ChecklistItem<String>> model = new DefaultListModel<>(); // ✔ Generic
        checklist.forEach(model::addElement);
        JList<ChecklistItem<String>> list = new JList<>(model);
        JScrollPane scrollPane = new JScrollPane(list);

        JButton addButton = new JButton(Messages.get("add.item")); // ✔ Internationalization
        JButton markButton = new JButton(Messages.get("mark.packed"));
        JButton saveButton = new JButton(Messages.get("save"));
        JButton deleteButton = new JButton(Messages.get("delete"));
        JButton logoutButton = new JButton(Messages.get("logout"));
        JButton exportButton = new JButton(Messages.get("button.exportcsv"));
        JButton importButton = new JButton(Messages.get("button.importcsv")); // ✔ Serialization
        
        addButton.addActionListener(e -> {
            String item = JOptionPane.showInputDialog(frame, Messages.get("add.item"));
            if (item != null && !item.isBlank()) {
                String lang = Messages.getCurrentLocale().getLanguage();
                String translatedItem = item;
                if (!lang.equals("id")) {
                    translatedItem = Translator.translate(item, lang);
                }
                int result = JOptionPane.showConfirmDialog(frame, Messages.get("important"), Messages.get("important.title"), JOptionPane.YES_NO_OPTION);
                boolean penting = (result == JOptionPane.YES_OPTION);
                ChecklistItem<String> newItem = new ChecklistItem<>(translatedItem, penting);
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

        saveButton.addActionListener(e -> {
            checklist.clear();
            for (int i = 0; i < model.size(); i++) {
                checklist.add(model.get(i));
            }
            checklistCollection.deleteMany(eq("username", currentUsername));
            for (ChecklistItem<String> item : checklist) {
                Document doc = new Document("username", currentUsername)
                        .append("item", item.getItem())
                        .append("important", item.isImportant())
                        .append("packed", item.isPacked());
                checklistCollection.insertOne(doc);
            }
            JOptionPane.showMessageDialog(frame, Messages.get("save.success"));
        });

        deleteButton.addActionListener(e -> {
            int selected = list.getSelectedIndex();
            if (selected != -1) {
                ChecklistItem<String> selectedItem = model.get(selected);
                int confirm = JOptionPane.showConfirmDialog(frame,
                        Messages.get("delete.confirm") + " \"" + selectedItem.getItem() + "\"?",
                        Messages.get("delete.title"), JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    model.remove(selected);
                    checklist.remove(selectedItem);
                    Bson filter = and(eq("username", currentUsername), eq("item", selectedItem.getItem()));
                    checklistCollection.deleteOne(filter);
                }
            } else {
                JOptionPane.showMessageDialog(frame, Messages.get("select.item"));
            }
        });

        exportButton.addActionListener(e -> exportChecklistToCSV()); // ✔ Serialization

        importButton.addActionListener(e -> {
            importChecklistFromCSV(); // ✔ Serialization
            model.clear();
            checklist.forEach(model::addElement);
        });

        logoutButton.addActionListener(e -> {
            frame.dispose();
            currentUsername = null;
            showLoginForm();
        });

        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        panel.add(addButton);
        panel.add(markButton);
        panel.add(saveButton);
        panel.add(deleteButton);
        panel.add(exportButton);
        panel.add(importButton);
        panel.add(logoutButton);

        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(panel, BorderLayout.SOUTH);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        SerialManager serial = new SerialManager(); // ✔ Serial Communication
        serial.connect("COM3");
        serial.listenSerialInput();
    }

    private static void exportChecklistToCSV() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Pilih lokasi untuk export CSV");
            int userSelection = fileChooser.showSaveDialog(null);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                if (!file.getName().toLowerCase().endsWith(".csv")) {
                    file = new File(file.getAbsolutePath() + ".csv");
                }
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
                writer.println("item,important,packed");
                for (ChecklistItem<String> item : checklist) {
                    writer.println("\"" + item.getItem() + "\"," + item.isImportant() + "," + item.isPacked());
                }
                writer.close();
                JOptionPane.showMessageDialog(null, "Berhasil export ke CSV!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Gagal export: " + e.getMessage());
        }
    }

    private static void importChecklistFromCSV() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Pilih file CSV untuk diimport");
            int userSelection = fileChooser.showOpenDialog(null);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(selectedFile), "UTF-8"));
                String line;
                checklist.clear();
                reader.readLine(); // skip header
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",", -1);
                    if (parts.length >= 3) {
                        String item = parts[0].replace("\"", "");
                        boolean important = Boolean.parseBoolean(parts[1]);
                        boolean packed = Boolean.parseBoolean(parts[2]);
                        ChecklistItem<String> newItem = new ChecklistItem<>(item, important);
                        newItem.setPacked(packed);
                        checklist.add(newItem);
                    }
                }
                reader.close();
                JOptionPane.showMessageDialog(null, "Berhasil import dari CSV!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Gagal import: " + e.getMessage());
        }
    }

    private static void loadUsers() {
        users.clear();
        for (Document doc : userCollection.find()) {
            String username = doc.getString("username");
            String passwordHash = doc.getString("passwordHash");
            users.put(username, new User(username, passwordHash));
        }
    }

    private static void saveUsers() {
        userCollection.drop();
        for (User user : users.values()) {
            Document doc = new Document("username", user.getUsername())
                    .append("passwordHash", user.getPasswordHash());
            userCollection.insertOne(doc);
        }
    }

    private static void deleteUserFromDB(String username) {
        Bson filter = eq("username", username);
        userCollection.deleteOne(filter);
    }

    private static void loadChecklist() {
        checklist.clear();
        for (Document doc : checklistCollection.find(eq("username", currentUsername))) {
            String item = doc.getString("item");
            boolean penting = doc.getBoolean("important", false);
            boolean dibawa = doc.getBoolean("packed", false);
            ChecklistItem<String> checklistItem = new ChecklistItem<>(item, penting);
            checklistItem.setPacked(dibawa);
            checklist.add(checklistItem);
        }
    }
}