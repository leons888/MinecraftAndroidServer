package com.bdscontrol.app;

import android.app.ActivityManager;
import android.content.Context;
import android.net.TrafficStats;
import org.json.JSONObject;

final class MonitorManager {
    private final Context context;
    private long lastRx=TrafficStats.getTotalRxBytes(), lastTx=TrafficStats.getTotalTxBytes(), lastAt=System.currentTimeMillis();
    MonitorManager(Context c){context=c;}
    synchronized String snapshot(){
        try{
            ActivityManager am=(ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo mi=new ActivityManager.MemoryInfo(); am.getMemoryInfo(mi);
            long now=System.currentTimeMillis(), rx=TrafficStats.getTotalRxBytes(), tx=TrafficStats.getTotalTxBytes();
            double sec=Math.max(0.001,(now-lastAt)/1000.0); lastAt=now;
            JSONObject o=new JSONObject();
            o.put("availableRamBytes",mi.availMem); o.put("totalRamBytes",mi.totalMem); o.put("lowMemory",mi.lowMemory);
            o.put("rxBytesPerSecond",(rx-lastRx)/sec); o.put("txBytesPerSecond",(tx-lastTx)/sec);
            lastRx=rx; lastTx=tx; o.put("process",SystemMonitor.snapshot()); return o.toString();
        }catch(Exception e){return "{}";}
    }
}
