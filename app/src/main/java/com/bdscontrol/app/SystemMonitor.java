package com.bdscontrol.app;

import android.os.Debug;import android.os.StatFs;import android.os.Build;import org.json.JSONObject;

final class SystemMonitor {
 static String snapshot(){try{JSONObject o=new JSONObject();o.put("processPssKb",Debug.getPss());o.put("cores",Runtime.getRuntime().availableProcessors());o.put("freeMemory",Runtime.getRuntime().freeMemory());o.put("totalMemory",Runtime.getRuntime().totalMemory());o.put("android",Build.VERSION.RELEASE);return o.toString();}catch(Exception e){return "{}";}}
}
