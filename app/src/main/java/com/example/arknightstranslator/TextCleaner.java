package com.example.arknightstranslator;

public class TextCleaner {
    public static String removeCSS(String text)
    {
        return text.replaceAll("<.+?>","");
    }
}
