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
    private boolean important;
    private boolean packed;

    public ChecklistItem(T item, boolean important) {
        this.item = item;
        this.important = important;
        this.packed = false;
    }

    public T getItem() {
        return item;
    }

    public boolean isImportant() {
        return important;
    }

    public boolean isPacked() {
        return packed;
    }

    public void setPacked(boolean packed) {
        this.packed = packed;
    }

    @Override
    public String toString() {
        return (important ? "[penting]" : "") + item + (packed ? " (✔️)" : "");
    }
}
