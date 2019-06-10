package com.dm.search.custom_bridge;

import org.hibernate.search.bridge.TwoWayStringBridge;

import java.util.UUID;

public class UUIDFieldBridge implements TwoWayStringBridge {



    @Override
    public String objectToString(Object object) {
        return object.toString();
    }


    @Override
    public Object stringToObject(String stringValue) {
        return UUID.fromString(stringValue);
    }
}
