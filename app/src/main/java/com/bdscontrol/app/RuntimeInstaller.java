package com.bdscontrol.app;

import android.content.Context;import java.io.*;import java.util.*;

/** Downloads only the guest libraries and tunnel agent; Box64 is packaged at build time. */
final class RuntimeInstaller {
 interface Events{void message(String type,String text);void progress(String id,long done,long total);}
 private final File root;private final Events events;
 RuntimeInstaller(Context c,Events e){root=new File(c.getFilesDir(),"bds-runtime");events=e;root.mkdirs();}
 void install(List<RuntimeManifest.Artifact> artifacts){new Thread(()->{try{
  for(RuntimeManifest.Artifact a:artifacts){
   File marker=new File(root,".verified-"+a.id);if(marker.isFile())continue;
   events.message("info","Downloading "+a.id+" from "+a.url);File tmp=new File(root,"downloads/"+a.id);
   try{VerifiedDownloader.fetch(a.url,a.sha256,tmp,(d,t)->events.progress(a.id,d,t));}catch(Exception e){throw new IOException("Could not download or verify "+a.id+". Check internet access, free storage, and retry Runtime install. URL: "+a.url,e);}
   if("tar.gz".equals(a.kind))ArchiveExtractor.tarGz(tmp,new File(root,"x86lib"));else if("tar.xz".equals(a.kind))ArchiveExtractor.tarXz(tmp,new File(root,"x86lib"));else if("zip".equals(a.kind))ArchiveExtractor.zip(tmp,new File(root,"x86lib"));else{File out=new File(root,"bin/"+a.id);out.getParentFile().mkdirs();out.delete();if(!tmp.renameTo(out))throw new IOException("cannot install "+a.id+" into app data");if(!out.setExecutable(true,false))throw new IOException("cannot mark "+a.id+" executable");}
   tmp.delete();if(!marker.isFile()&&!marker.createNewFile())throw new IOException("cannot mark verified "+a.id);events.message("info","Installed "+a.id);
  }events.message("ready","Runtime files ready. Box64 is embedded in this APK; BDS and guest libraries remain in app data.");
 }catch(Exception e){events.message("error","Runtime installation failed: "+e.getMessage());}},"runtime-install").start();}
}
