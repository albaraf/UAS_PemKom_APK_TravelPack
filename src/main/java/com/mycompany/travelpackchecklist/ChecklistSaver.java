/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.travelpackchecklist;

/**
 *
 * @author ASUS
 */

import com.mongodb.client.MongoCollection;
import org.bson.Document;

import javax.swing.*;
import java.util.List;

public class ChecklistSaver extends SwingWorker<Void, Void> {
    private final List<ChecklistItem<String>> checklist;
    private final MongoCollection<Document> checklistCollection;

    public ChecklistSaver(List<ChecklistItem<String>> checklist, MongoCollection<Document> checklistCollection) {
        this.checklist = checklist;
        this.checklistCollection = checklistCollection;
    }

    @Override
    protected Void doInBackground() {
        checklistCollection.drop();
        for (ChecklistItem<String> item : checklist) {
            Document doc = new Document("item", item.getItem())
                    .append("important", item.isImportant())
                    .append("packed", item.isPacked());
            checklistCollection.insertOne(doc);
        }
        return null;
    }

    @Override
    protected void done() {
        JOptionPane.showMessageDialog(null, "Checklist disimpan (background thread).");
    }
}
