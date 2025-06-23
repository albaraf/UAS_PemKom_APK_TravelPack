/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.travelpackchecklist;

/**
 *
 * @author ASUS
 */
import java.io.Serializable;

public class ChecklistItem<T> implements Serializable {
    private T item;
    private boolean isPacked;
    private boolean isImportant;

    public ChecklistItem(T item, boolean isImportant) {
        this.item = item;
        this.isImportant = isImportant;
        this.isPacked = false;
    }

    public T getItem() { return item; }
    public boolean isPacked() { return isPacked; }
    public void setPacked(boolean packed) { isPacked = packed; }
    public boolean isImportant() { return isImportant; }

    @Override
    public String toString() {
        return String.format("[%s] %s %s",
                isPacked ? "x" : " ",
                item.toString(),
                isImportant ? "(Penting)" : "");
    }
}

