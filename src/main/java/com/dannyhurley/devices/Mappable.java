package com.dannyhurley.devices;

public abstract class Mappable {
    public static String id(int midiChannel, int cc) {
        return midiChannel + "-" + cc;
    }
}
