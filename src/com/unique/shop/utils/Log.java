package com.unique.shop.utils;

import java.util.Date;

public class Log {

    public static void send(String s) {
        Date date = new Date(System.currentTimeMillis());
        System.out.println("[" + date.getHours() + ":" + (date.getMinutes() < 10 ? "0" : "") + date.getMinutes() + ":" + (date.getSeconds() < 10 ? "0" : "") + date.getSeconds() + "] " + s);
    }

    public static void send(String pref, String s) {


        send("[" + pref + "] " + s);
    }

}
