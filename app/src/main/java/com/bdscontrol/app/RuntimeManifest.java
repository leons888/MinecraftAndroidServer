package com.bdscontrol.app;

import org.json.JSONArray;import org.json.JSONObject;import java.util.*;

final class RuntimeManifest {
 static final String URL="https://raw.githubusercontent.com/leons888/MinecraftAndroidServer/main/runtime-manifest.json";
 static List<Artifact> parse(String json)throws Exception{JSONObject root=new JSONObject(json);JSONArray a=root.optJSONArray("artifacts");if(a==null||a.length()==0)throw new IllegalArgumentException("runtime-manifest.json: artifacts отсутствуют");List<Artifact> out=new ArrayList<>();Set<String> ids=new HashSet<>();for(int i=0;i<a.length();i++){JSONObject x=a.getJSONObject(i);String id=x.getString("id"),url=x.getString("url"),sha=x.getString("sha256"),kind=x.getString("kind");if(!ids.add(id))throw new IllegalArgumentException(id+": duplicate artifact id");if(!url.startsWith("https://"))throw new IllegalArgumentException(id+": URL must use HTTPS");if(!sha.matches("[0-9a-fA-F]{64}"))throw new IllegalArgumentException(id+": pinned SHA-256 is required");if(!Arrays.asList("tar.gz","tar.xz","zip","executable").contains(kind))throw new IllegalArgumentException(id+": unsupported kind "+kind);out.add(new Artifact(id,url,sha.toLowerCase(Locale.US),kind));}return out;}
 static final class Artifact{final String id,url,sha256,kind;Artifact(String i,String u,String s,String k){id=i;url=u;sha256=s;kind=k;}}
}
