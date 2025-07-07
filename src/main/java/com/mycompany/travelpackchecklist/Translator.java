/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.travelpackchecklist;

/**
 *
 * @author ASUS
 */
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;

import java.net.URLEncoder;

public class Translator {

    private static final String API_URL = "https://api.mymemory.translated.net/get?q=%s&langpair=%s";

    public static String translate(String text, String targetLang) {
        try {
            String encodedText = URLEncoder.encode(text, "UTF-8");
            String langpair = "id|" + targetLang;
            String encodedLangpair = URLEncoder.encode(langpair, "UTF-8");

            String url = String.format(API_URL, encodedText, encodedLangpair);

            HttpResponse<JsonNode> response = Unirest.get(url).asJson();
            if (response.getStatus() == 200) {
                String translated = response.getBody()
                        .getObject()
                        .getJSONObject("responseData")
                        .getString("translatedText");
                return translated;
            } else {
                return text; // fallback
            }
        } catch (Exception e) {
            e.printStackTrace();
            return text;
        }
    }
}