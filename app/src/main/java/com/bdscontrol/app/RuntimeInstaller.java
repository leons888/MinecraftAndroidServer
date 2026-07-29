package com.bdscontrol.app;

import android.content.Context;import java.io.*;import java.util.*;

/** Downloads guest libraries and the tunnel agent; Box64 is packaged in the APK. */
final class RuntimeInstaller {
 interface Events{void message(String type,String text);void progress(String id,long done,long total);}
 private final File root;private final Events events;
 RuntimeInstaller(Context c,Events e){root=new File(c.getFilesDir(),"bds-runtime");events=e;root.mkdirs();}
 void install(List<RuntimeManifest.Artifact> artifacts)throws Exception{
  for(RuntimeManifest.Artifact a:artifacts){
   File marker=new File(root,".verified-"+a.id);
   if(marker.isFile()&&validInstalled(a)){events.message("info",a.id+" уже установлен и проверен");continue;}
   if(marker.exists())marker.delete();
   events.message("info","Загрузка "+a.id+": "+a.url);File tmp=new File(root,"downloads/"+a.id);
   try{VerifiedDownloader.fetch(a.url,a.sha256,tmp,(d,t)->events.progress(a.id,d,t));}catch(Exception e){throw new IOException("Не удалось загрузить или проверить "+a.id+". Проверь интернет и свободное место. URL: "+a.url,e);}
   try{
    if("tar.gz".equals(a.kind))ArchiveExtractor.tarGz(tmp,new File(root,"x86lib"));
    else if("tar.xz".equals(a.kind))ArchiveExtractor.tarXz(tmp,new File(root,"x86lib"));
    else if("zip".equals(a.kind))ArchiveExtractor.zip(tmp,new File(root,"x86lib"));
    else{File out=new File(root,"bin/"+a.id);out.getParentFile().mkdirs();out.delete();if(!tmp.renameTo(out))throw new IOException("не удалось установить "+a.id+" в данные приложения");if(!out.setExecutable(true,false)&&!out.canExecute())throw new IOException("не удалось сделать "+a.id+" исполняемым");}
   }catch(Exception e){throw new IOException("Не удалось распаковать "+a.id+": "+e.getMessage(),e);}
   tmp.delete();if(!validInstalled(a))throw new IOException("Артефакт "+a.id+" распакован неполностью");
   if(!marker.createNewFile())throw new IOException("не удалось отметить "+a.id+" как проверенный");events.message("info","Установлен "+a.id);
  }
  events.message("info","Файлы Runtime готовы. Box64 встроен в APK; BDS и guest-библиотеки остаются в данных приложения.");
 }
 private boolean validInstalled(RuntimeManifest.Artifact a){
  if("executable".equals(a.kind)){File f=new File(root,"bin/"+a.id);return f.isFile()&&(f.canExecute()||f.setExecutable(true,false));}
  if("tar.gz".equals(a.kind)||"tar.xz".equals(a.kind)||"zip".equals(a.kind)){File d=new File(root,"x86lib");return hasGuestLibrary(d);}
  return true;
 }
 private boolean hasGuestLibrary(File d){if(d==null||!d.isDirectory())return false;File[] fs=d.listFiles();if(fs==null)return false;for(File f:fs){if(f.isFile()&&f.getName().contains(".so"))return true;if(f.isDirectory()&&hasGuestLibrary(f))return true;}return false;}
}
