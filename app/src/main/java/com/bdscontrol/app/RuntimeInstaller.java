package com.bdscontrol.app;

import android.content.Context;import java.io.*;import java.util.*;

final class RuntimeInstaller {
 interface Events{void message(String type,String text);void progress(String id,long done,long total);}
 private final File root;private final Events events;
 RuntimeInstaller(Context c,Events e){root=new File(c.getFilesDir(),"bds-runtime/runtime");events=e;}
 void install(List<RuntimeManifest.Artifact> artifacts){new Thread(()->{try{for(RuntimeManifest.Artifact a:artifacts){File marker=new File(root,".verified-"+a.id);if(marker.isFile())continue;events.message("info","Downloading "+a.id);File tmp=new File(root,"downloads",a.id+(a.kind.equals("tar.gz")?".tar.gz":""));VerifiedDownloader.fetch(a.url,a.sha256,tmp,(d,t)->events.progress(a.id,d,t));if(a.kind.equals("tar.gz"))ArchiveExtractor.tarGz(tmp,new File(root,"rootfs"));else{File bin=new File(root,"bin",a.id);bin.getParentFile().mkdirs();if(!tmp.renameTo(bin))throw new IOException("cannot install "+a.id);bin.setExecutable(true,false);}if(!marker.createNewFile())throw new IOException("cannot mark verified "+a.id);}events.message("ready","Verified runtime installation complete");}catch(Exception e){events.message("error","Runtime installation failed: "+e.getMessage());}},"runtime-install").start();}
}
