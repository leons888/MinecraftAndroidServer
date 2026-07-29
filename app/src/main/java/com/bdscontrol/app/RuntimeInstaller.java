package com.bdscontrol.app;

import android.content.Context;import java.io.*;import java.util.*;

/** Installs verified guest assets and never trusts a stale partial extraction. */
final class RuntimeInstaller {
 interface Events{void message(String type,String text);void progress(String id,long done,long total);}
 private final File root,x86lib,bin;private final Events events;
 RuntimeInstaller(Context c,Events e){root=new File(c.getFilesDir(),"bds-runtime");x86lib=new File(root,"x86lib");bin=new File(root,"bin");events=e;root.mkdirs();}
 void install(List<RuntimeManifest.Artifact> artifacts)throws Exception{
  for(RuntimeManifest.Artifact a:artifacts){File marker=new File(root,".verified-"+a.id);if(marker.isFile()&&valid(a))continue;marker.delete();File tmp=new File(root,"downloads/"+a.id);events.message("info","Загрузка "+a.id+": "+a.url);VerifiedDownloader.fetch(a.url,a.sha256,tmp,(d,t)->events.progress(a.id,d,t));
   if("tar.gz".equals(a.kind)||"tar.xz".equals(a.kind)||"zip".equals(a.kind)){if("tar.gz".equals(a.kind)||"tar.xz".equals(a.kind))delete(x86lib);x86lib.mkdirs();if("tar.gz".equals(a.kind))ArchiveExtractor.tarGz(tmp,x86lib);else if("tar.xz".equals(a.kind))ArchiveExtractor.tarXz(tmp,x86lib);else ArchiveExtractor.zip(tmp,x86lib);}
   else{File out=new File(bin,a.id);out.getParentFile().mkdirs();if(out.exists())out.delete();if(!tmp.renameTo(out))throw new IOException("Не удалось установить "+a.id);if(!out.setExecutable(true,false)&&!out.canExecute())throw new IOException("Не удалось сделать "+a.id+" исполняемым");}
   tmp.delete();if(!valid(a))throw new IOException("Артефакт "+a.id+" установлен, но проверка структуры не пройдена");if(!marker.createNewFile())throw new IOException("Не удалось создать marker "+a.id);events.message("info","Установлен "+a.id);
  }
 }
 private boolean valid(RuntimeManifest.Artifact a){if("executable".equals(a.kind)){File f=new File(bin,a.id);return f.isFile()&&f.length()>0&&(f.canExecute()||f.setExecutable(true,false));}if("tar.gz".equals(a.kind)||"tar.xz".equals(a.kind)||"zip".equals(a.kind))return countFiles(x86lib)>0;return false;}
 private int countFiles(File d){int n=0;File[] fs=d.listFiles();if(fs==null)return 0;for(File f:fs)n+=f.isDirectory()?countFiles(f):(f.isFile()&&f.length()>0?1:0);return n;}
 private void delete(File f){if(f.isDirectory()){File[] fs=f.listFiles();if(fs!=null)for(File x:fs)delete(x);}f.delete();}
}
