package com.example.arknightstranslator;

public class TranslateVariant {
    private String id;
    private String translation;

    public TranslateVariant(String id, String translation)
    {
        this.id = id;
        this.translation = translation;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }
}
