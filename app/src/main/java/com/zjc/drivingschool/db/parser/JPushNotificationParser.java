package com.zjc.drivingschool.db.parser;

import com.google.gson.reflect.TypeToken;
import com.mobo.mobolibrary.parser.JsonParser;
import com.zjc.drivingschool.db.model.JPushNotification;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class JPushNotificationParser extends JsonParser<JPushNotification> {

    @Override
    public Type getResultMessageType() {
        return new TypeToken<JPushNotification>() {
        }.getType();
    }

    @Override
    public Type getArrayTypeToken() {
        return new TypeToken<ArrayList<JPushNotification>>() {
        }.getType();
    }
}
