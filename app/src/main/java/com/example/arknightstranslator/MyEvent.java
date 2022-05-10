package com.example.arknightstranslator;


import java.util.EventObject;

public class MyEvent extends EventObject {

    private Type type = Type.TYPE_1;

    public Type getType()
    {
        return type;
    }
    public void setType(Type type)
    {
        this.type = type;
    }
    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public MyEvent(Object source) {
        super(source);
    }

    public enum Type
    {
        TYPE_1, TYPE_2
    }
}
