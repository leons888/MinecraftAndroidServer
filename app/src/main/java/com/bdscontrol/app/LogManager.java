package com.bdscontrol.app;

import org.json.JSONObject;

final class LogManager {
    private final MainActivity a; LogManager(MainActivity x){a=x;}
    void line(String source,String text){try{JSONObject o=new JSONObject();o.put("source",source);o.put("line",text);o.put("time",System.currentTimeMillis());a.emit("log",o.toString());}catch(Exception ignored){}}
    void event(String type,String message){try{JSONObject o=new JSONObject();o.put("message",message);a.emit(type,o.toString());}catch(Exception ignored){}}
}
