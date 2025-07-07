/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.travelpackchecklist;

/**
 *
 * @author ASUS
 */
import java.util.*;

public class Messages {
    private static ResourceBundle bundle;

    public static void setLocale(Locale locale) {
        bundle = ResourceBundle.getBundle("com.mycompany.travelpackchecklist.Labels", locale);
    }

    public static String get(String key) {
        if (bundle == null) {
            setLocale(Locale.getDefault());
        }
        return bundle.getString(key);
    }

    public static Locale getCurrentLocale() {
        if (bundle == null) {
            setLocale(Locale.getDefault());
        }
        return bundle.getLocale();
    }
}
