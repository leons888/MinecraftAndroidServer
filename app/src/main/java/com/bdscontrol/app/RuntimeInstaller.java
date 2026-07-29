package com.bdscontrol.app;

import android.content.Context;import java.io.*;import java.util.*;

/** Downloads, extracts, normalizes and validates the complete guest runtime. */
final class RuntimeInstaller {
 interface Events{void message(String type,String text);void progress(String id,long done,long total);}
 // Official Box64 bundle deliberately removes glibc, libc, libpthread, libdl,
 // libm, librt and ld-linux. Box64 supplies those through native wrappers/loader.
 // Only supplemental guest libraries belong in this archive contract.
 static final String[] REQUIRED={"libgcc_s.so.1","libstdc++.so.6","libzstd.so.1"};
 private final File root,x86lib,normalized,bin;private final Events events;
 RuntimeInstaller(Context c,Events e){root=new File(c.getFilesDir(),"bds-runtime");x86lib=new File(root,"x86lib");normalized=new File(root,"guest-libs");bin=new File(root,"bin");events=e;root.mkdirs();}
 void install(List<RuntimeManifest.Artifact> artifacts)throws Exception{
  for(RuntimeManifest.Artifact a:artifacts){File marker=new File(root,".verified-"+a.id);if(marker.isFile()&&valid(a))continue;marker.delete();File tmp=new File(root,"downloads/"+a.id);events.message("info","Загрузка "+a.id+": "+a.url);VerifiedDownloader.fetch(a.url,a.sha256,tmp,(d,t)->events.progress(a.id,d,t));
   if("tar.gz".equals(a.kind)||"tar.xz".equals(a.kind)||"zip".equals(a.kind)){delete(x86lib);delete(normalized);x86lib.mkdirs();normalized.mkdirs();if("tar.gz".equals(a.kind))ArchiveExtractor.tarGz(tmp,x86lib);else if("tar.xz".equals(a.kind))ArchiveExtractor.tarXz(tmp,x86lib);else ArchiveExtractor.zip(tmp,x86lib);normalize();}
   else{File out=new File(bin,a.id);out.getParentFile().mkdirs();if(out.exists())out.delete();if(!tmp.renameTo(out))throw new IOException("Не удалось установить "+a.id);if(!out.setExecutable(true,false)&&!out.canExecute())throw new IOException("Не удалось сделать "+a.id+" исполняемым");}
   tmp.delete();if(!valid(a))throw new IOException("Артефакт "+a.id+" установлен, но обязательные supplemental guest-библиотеки не найдены: "+missing());if(!marker.createNewFile())throw new IOException("Не удалось создать marker "+a.id);events.message("info","Установлен "+a.id);
  }
 }
 private void normalize()throws IOException{List<File> files=new ArrayList<>();collect(x86lib,files);Map<String,File> byName=new HashMap<>();for(File f:files){String n=f.getName();if(n.contains(".so")){byName.putIfAbsent(n,f);File out=new File(normalized,n);if(!out.exists())copy(f,out);}}for(String required:REQUIRED){if(new File(normalized,required).isFile())continue;File candidate=findVersioned(required,byName);if(candidate!=null)copy(candidate,new File(normalized,required));}File[] done=normalized.listFiles();events.message("info","Нормализовано supplemental guest-библиотек: "+(done==null?0:done.length));}
 private File findVersioned(String name,Map<String,File> byName){String stem=name.substring(0,name.indexOf(".so"));for(Map.Entry<String,File> e:byName.entrySet()){String n=e.getKey();if(n.startsWith(stem+"-")||n.startsWith(stem+".so."))return e.getValue();}return null;}
 private String missing(){List<String> m=new ArrayList<>();for(String n:REQUIRED)if(!new File(normalized,n).isFile()||new File(normalized,n).length()==0)m.add(n);return m.toString();}
 private boolean valid(RuntimeManifest.Artifact a){if("executable".equals(a.kind)){File f=new File(bin,a.id);return f.isFile()&&f.length()>0&&(f.canExecute()||f.setExecutable(true,false));}if("tar.gz".equals(a.kind)||"tar.xz".equals(a.kind)||"zip".equals(a.kind))return missing().equals("[]");return false;}
 private void collect(File d,List<File> out){File[] fs=d.listFiles();if(fs==null)return;for(File f:fs){if(f.isFile())out.add(f);else if(f.isDirectory())collect(f,out);}}
 private void delete(File f){if(f.isDirectory()){File[] fs=f.listFiles();if(fs!=null)for(File x:fs)delete(x);}f.delete();}
 private void copy(File from,File to)throws IOException{to.getParentFile().mkdirs();try(InputStream i=new FileInputStream(from);OutputStream o=new FileOutputStream(to)){byte[]b=new byte[65536];int n;while((n=i.read(b))>0)o.write(b,0,n);}}
}
