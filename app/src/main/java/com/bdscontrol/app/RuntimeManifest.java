package com.bdscontrol.app;

import org.json.JSONArray;import org.json.JSONObject;import java.util.*;

final class RuntimeManifest {
 static final String URL="https://raw.githubusercontent.com/leons888/MinecraftAndroidServer/main/runtime-manifest.json";
 static List<Artifact> parse(String json)throws Exception{JSONArray a=new JSONObject(json).getJSONArray("artifacts");List<Artifact> out=new ArrayList<>();for(int i=0;i<a.length();i++){JSONObject x=a.getJSONObject(i);out.add(new Artifact(x.getString("id"),x.getString("url"),x.getString("sha256"),x.getString("kind")));}return out;}
 static final class Artifact{final String id,url,sha256,kind;Artifact(String i,String u,String s,String k){id=i;url=u;sha256=s;kind=k;}}
}
