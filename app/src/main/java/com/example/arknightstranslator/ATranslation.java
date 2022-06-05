package com.example.arknightstranslator;

import java.util.LinkedList;
import java.util.List;

public abstract class ATranslation {

    protected String text;
    private List<MyEventListener> eventListeners = new LinkedList<>();

    protected void notifyEventListeners(MyEvent event)
    {
        for(MyEventListener eventListener:eventListeners)
        {
            eventListener.processEvent(event);
        }
    }
    public void addOnTranslateListener(MyEventListener eventListener)
    {
        eventListeners.add(eventListener);
    }

    public abstract void translate(String textToTranslate);

    public String getText() {
        return text;
    }
}
