package com.example.arknightstranslator;

import java.util.EventListener;

public interface MyEventListener extends EventListener {
    void processEvent(MyEvent event);
}
