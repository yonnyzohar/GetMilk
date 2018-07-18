package com.yonnyzohar.getmilk.eventDispatcher;

public class TimerEvent extends SimpleEvent {

    public static final String TIMER = "timer";

    private long timePassed;
    public long getTimePassed() {
        return timePassed;
    }
    public void setTimePassed(long timePassed) {
        this.timePassed = timePassed;
    }

    private int count;
    public int getCount() {
        return count;
    }
    public void setCount(int count) {
        this.count = count;
    }

    public TimerEvent(String type, long timePassed, int count) {
        super(type);
        this.timePassed = timePassed;
        this.count = count;
    }

}
