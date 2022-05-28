package com.example.arknightstranslator;

import java.util.regex.Pattern;

public class TextCleaner {
    public static String removeCSS(String text)
    {
        return text.replaceAll("<.+?>","");
    }
}
