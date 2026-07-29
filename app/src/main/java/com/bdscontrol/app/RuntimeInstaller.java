package com.bdscontrol.app;

import android.content.Context;import java.io.*;import java.util.*;

/**
 * Downloads only the guest libraries and tunnel agent; Box64 is packaged at build time.
 *
 * install() is deliberately blocking. It used to start its own thread and return
 * immediately, which made the caller announce "Runtime готов" while a 27 MB glibc
 * bundle was still downloading. Callers must invoke this off the main thread.
 */
final class RuntimeInstaller {
 interface Events{void message(String type,String text);void progress(String id,long done,long total);}
 private final File root;private final Events events;
 RuntimeInstaller(Context c,Events e){root=new File(c.getFilesDir(),"bds-runtime");events=e;root.mkdirs();}
 void install(List<RuntimeManifest.Artifact> artifacts)throws Exception{
  for(RuntimeManifest.Artifact a:artifacts){
   File marker=new File(root,".verified-"+a.id);if(marker.isFile()){events.message("info",a.id+" уже установлен и проверен");continue;}
   events.message("info","Загрузка "+a.id+": "+a.url);File tmp=new File(root,"downloads/"+a.id);
   try{VerifiedDownloader.fetch(a.url,a.sha256,tmp,(d,t)->events.progress(a.id,d,t));}catch(Exception e){throw new IOException("Не удалось загрузить или проверить "+a.id+". Проверь интернет и свободное место, затем повтори подготовку Runtime. URL: "+a.url,e);}
   try{
    if("tar.gz".equals(a.kind))ArchiveExtractor.tarGz(tmp,new File(root,"x86lib"));
    else if("tar.xz".equals(a.kind))ArchiveExtractor.tarXz(tmp,new File(root,"x86lib"));
    else if("zip".equals(a.kind))ArchiveExtractor.zip(tmp,new File(root,"x86lib"));
    else{File out=new File(root,"bin/"+a.id);out.getParentFile().mkdirs();out.delete();if(!tmp.renameTo(out))throw new IOException("не удалось установить "+a.id+" в данные приложения");out.setExecutable(true,false);}
   }catch(Exception e){throw new IOException("Не удалось распаковать "+a.id+": "+e.getMessage(),e);}
   tmp.delete();if(!marker.isFile()&&!marker.createNewFile())throw new IOException("не удалось отметить "+a.id+" как проверенный");events.message("info","Установлен "+a.id);
  }
  events.message("info","Файлы Runtime готовы. Box64 встроен в APK; BDS и guest-библиотеки остаются в данных приложения.");
 }
}
