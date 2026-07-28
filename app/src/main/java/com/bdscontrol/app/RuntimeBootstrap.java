package com.bdscontrol.app;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.*;

final class RuntimeBootstrap {
    interface Events { void message(String type,String text); void progress(long done,long total); }
    private final Context context; private final File root; private final Events events;
    RuntimeBootstrap(Context c,Events e){context=c;root=new File(c.getFilesDir(),"bds-runtime/runtime");events=e;root.mkdirs();}
    File root(){return root;}
    boolean ready(){return executable("bin/proot")&&executable("bin/box64")&&new File(root,"rootfs/usr/lib/x86_64-linux-gnu/ld-linux-x86-64.so.2").isFile();}
    void ensure(){new Thread(()->{try{if(!executable("bin/proot"))downloadProot();if(!executable("bin/box64"))downloadBox64();if(!new File(root,"rootfs/usr/lib/x86_64-linux-gnu/ld-linux-x86-64.so.2").isFile())downloadRootfs();if(!ready())throw new IOException("runtime incomplete after bootstrap");events.message("ready","Embedded PRoot, Box64 and glibc rootfs are ready");}catch(Exception e){events.message("error","Runtime bootstrap failed: "+e.getMessage());}},"runtime-bootstrap").start();}
    private boolean executable(String rel){File f=new File(root,rel);return f.isFile()&&f.setExecutable(true,false);}
    private void downloadProot()throws Exception{String url="https://raw.githubusercontent.com/proot-me/proot-static-build/master/static/proot-arm64";File out=new File(root,"bin/proot");out.getParentFile().mkdirs();download(url,out,null);if(!executable("bin/proot"))throw new IOException("cannot make PRoot executable");events.message("info","Downloaded PRoot arm64 from proot-me/proot-static-build");}
    private void downloadBox64()throws Exception{String url=releaseAsset("ptitSeb","box64",new String[]{"android","aarch64","arm64","box64"});if(url==null)throw new IOException("Box64 release has no downloadable ARM64 Linux asset; source builds cannot be performed safely inside APK");File out=new File(root,"bin/box64");download(url,out,null);if(!executable("bin/box64"))throw new IOException("cannot make Box64 executable");events.message("info","Downloaded Box64 ARM64 release asset");}
    private void downloadRootfs()throws Exception{String url=releaseAsset("termux","proot-distro",new String[]{"debian","aarch64","arm64","pd"});if(url==null)throw new IOException("No official Debian ARM64 proot-distro archive was found in the release assets");File archive=new File(root,"debian-rootfs");download(url,archive,null);extractTar(archive,new File(root,"rootfs"));archive.delete();events.message("info","Downloaded and extracted Debian ARM64 rootfs");}
    private String releaseAsset(String owner,String repo,String[] terms)throws Exception{String json=http("https://api.github.com/repos/"+owner+"/"+repo+"/releases/latest");JSONObject rel=new JSONObject(json);JSONArray a=rel.optJSONArray("assets");if(a==null)return null;for(int i=0;i<a.length();i++){JSONObject x=a.getJSONObject(i);String n=x.optString("name").toLowerCase(Locale.US);boolean ok=true;for(String t:terms)if(!n.contains(t.toLowerCase(Locale.US)))ok=false;if(ok)return x.optString("browser_download_url",null);}return null;}
    private String http(String url)throws Exception{HttpURLConnection c=(HttpURLConnection)new URL(url).openConnection();c.setRequestProperty("Accept","application/vnd.github+json");c.setRequestProperty("User-Agent","BDS-Control/0.1");try(InputStream in=c.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[]b=new byte[8192];int n;while((n=in.read(b))>0)out.write(b,0,n);return out.toString(StandardCharsets.UTF_8.name());}}
    private void download(String url,File out,DownloadManager.Progress p)throws Exception{HttpURLConnection c=(HttpURLConnection)new URL(url).openConnection();c.setRequestProperty("User-Agent","BDS-Control/0.1");c.connect();if(c.getResponseCode()/100!=2)throw new IOException("HTTP "+c.getResponseCode()+" for "+url);long total=c.getContentLengthLong(),done=0;out.getParentFile().mkdirs();try(InputStream in=c.getInputStream();OutputStream os=new FileOutputStream(out)){byte[]b=new byte[65536];int n;while((n=in.read(b))>0){os.write(b,0,n);done+=n;if(p!=null)p.update(done,total);events.progress(done,total);}}}
    private void extractTar(File archive,File dest)throws Exception{dest.mkdirs();String n=archive.getName();if(n.endsWith(".zip")){try(ZipInputStream z=new ZipInputStream(new FileInputStream(archive))){unzip(z,dest);}return;}throw new IOException("rootfs archive compression is not supported by the built-in extractor: "+n+". Android APK has no tar.xz decoder without a bundled native library.");}
    private void unzip(ZipInputStream z,File dest)throws Exception{ZipEntry e;byte[]b=new byte[65536];while((e=z.getNextEntry())!=null){File f=new File(dest,e.getName());if(!f.toPath().normalize().startsWith(dest.toPath().normalize()))throw new SecurityException("unsafe archive path");if(e.isDirectory())f.mkdirs();else{f.getParentFile().mkdirs();try(OutputStream o=new FileOutputStream(f)){int n;while((n=z.read(b))>0)o.write(b,0,n);}}}}
}
