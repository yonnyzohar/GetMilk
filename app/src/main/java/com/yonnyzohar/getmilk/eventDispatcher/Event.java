package com.yonnyzohar.getmilk.eventDispatcher;

public interface Event {

    String getType();
    Object getSource();
    void setSource(Object source);
}