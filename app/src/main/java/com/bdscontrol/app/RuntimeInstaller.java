package com.bdscontrol.app;

import android.content.Context;import java.io.*;import java.util.*;

/**
 * Downloads the pinned runtime artifacts into app data.
 * Archives land in bds-runtime/x86lib (x86_64 guest libraries for Box64),
 * single executables land in bds-runtime/bin and are always launched through Box64.
 */
final class RuntimeInstaller {
 interface Events{void message(String type,String text);void progress(String id,long done,long total);}
 private final File root;private final Events events;
 RuntimeInstaller(Context c,Events e){root=new File(c.getFilesDir(),"bds-runtime");events=e;root.mkdirs();}
 void install(List<RuntimeManifest.Artifact> artifacts){new Thread(()->{try{
  for(RuntimeManifest.Artifact a:artifacts){
   File marker=new File(root,".verified-"+a.id);
   if(marker.isFile())continue;
   events.message("info","Downloading "+a.id);
   File tmp=new File(root,"downloads/"+a.id);
   VerifiedDownloader.fetch(a.url,a.sha256,tmp,(d,t)->events.progress(a.id,d,t));
   if("tar.gz".equals(a.kind))ArchiveExtractor.tarGz(tmp,new File(root,"x86lib"));
   else if("tar.xz".equals(a.kind))ArchiveExtractor.tarXz(tmp,new File(root,"x86lib"));
   else if("zip".equals(a.kind))ArchiveExtractor.zip(tmp,new File(root,"x86lib"));
   else{File out=new File(root,"bin/"+a.id);out.getParentFile().mkdirs();out.delete();if(!tmp.renameTo(out))throw new IOException("cannot install "+a.id);}
   tmp.delete();
   if(!marker.isFile()&&!marker.createNewFile())throw new IOException("cannot mark verified "+a.id);
   events.message("info","Installed "+a.id);
  }
  events.message("ready","x86_64 guest libraries and tunnel agent installed; Box64 itself ships inside the APK");
 }catch(Exception e){events.message("error","Runtime installation failed: "+e.getMessage());}},"runtime-install").start();}
}
