package com.bdscontrol.app;

import org.json.JSONArray;import org.json.JSONObject;import java.net.URI;import java.util.*;

final class RuntimeManifest {
 static final String URL="https://raw.githubusercontent.com/leons888/MinecraftAndroidServer/main/runtime-manifest.json";
 static List<Artifact> parse(String json)throws Exception{JSONArray a=new JSONObject(json).getJSONArray("artifacts");List<Artifact> out=new ArrayList<>();for(int i=0;i<a.length();i++){JSONObject x=a.getJSONObject(i);String id=x.getString("id"),url=x.getString("url"),sha=x.getString("sha256"),kind=x.getString("kind");if(!url.startsWith("https://"))throw new IllegalArgumentException(id+": URL must use HTTPS");if(!sha.matches("[0-9a-fA-F]{64}"))throw new IllegalArgumentException(id+": pinned SHA-256 is required");out.add(new Artifact(id,url,sha.toLowerCase(Locale.US),kind));}return out;}
 static final class Artifact{final String id,url,sha256,kind;Artifact(String i,String u,String s,String k){id=i;url=u;sha256=s;kind=k;}}
}
