package com.bdscontrol.app;

import java.io.*;import java.util.ArrayList;import java.util.List;import java.util.zip.*;import org.apache.commons.compress.archivers.tar.*;import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;

final class ArchiveExtractor {
 static void zip(File archive,File dest)throws Exception{dest.mkdirs();try(ZipInputStream in=new ZipInputStream(new FileInputStream(archive))){ZipEntry e;byte[]b=new byte[65536];while((e=in.getNextEntry())!=null){File f=safe(dest,e.getName());if(e.isDirectory())f.mkdirs();else{f.getParentFile().mkdirs();try(OutputStream o=new FileOutputStream(f)){int n;while((n=in.read(b))>0)o.write(b,0,n);}}}}}
 static void tarGz(File archive,File dest)throws Exception{try(InputStream in=new GZIPInputStream(new BufferedInputStream(new FileInputStream(archive)))){tar(in,dest);}}
 static void tarXz(File archive,File dest)throws Exception{try(InputStream in=new XZCompressorInputStream(new BufferedInputStream(new FileInputStream(archive)))){tar(in,dest);}}
 private static File safe(File d,String n)throws IOException{File f=new File(d,n);if(!f.toPath().normalize().startsWith(d.toPath().normalize()))throw new SecurityException("unsafe archive path");return f;}
 /**
  * Extracts a tar stream. Links are recreated in a second pass: in a tar the link
  * entry usually comes before its target, so creating them inline meant the copy
  * fallback had nothing to copy. Failures are no longer swallowed, because the
  * Box64 x86 libs bundle relies on symlinks (libc.so.6 -> libc-2.xx.so) and losing
  * them leaves Box64 unable to resolve the guest glibc.
  */
 private static void tar(InputStream in,File dest)throws Exception{
  dest.mkdirs();
  List<String[]> symlinks=new ArrayList<>(),hardlinks=new ArrayList<>();
  try(TarArchiveInputStream t=new TarArchiveInputStream(in)){
   TarArchiveEntry e;byte[]b=new byte[65536];
   while((e=t.getNextTarEntry())!=null){
    File f=safe(dest,e.getName());
    if(e.isDirectory()){f.mkdirs();continue;}
    if(e.isSymbolicLink()){symlinks.add(new String[]{e.getName(),e.getLinkName()});continue;}
    if(e.isLink()){hardlinks.add(new String[]{e.getName(),e.getLinkName()});continue;}
    f.getParentFile().mkdirs();
    try(OutputStream o=new FileOutputStream(f)){int n;while((n=t.read(b))>0)o.write(b,0,n);}
   }
  }
  for(String[] l:hardlinks){
   File f=safe(dest,l[0]),target=safe(dest,l[1]);
   f.getParentFile().mkdirs();f.delete();
   try{android.system.Os.link(target.getAbsolutePath(),f.getAbsolutePath());}catch(Throwable ignored){copy(target,f);}
   if(!f.isFile())throw new IOException("Не удалось воссоздать жёсткую ссылку "+l[0]+" -> "+l[1]);
  }
  for(String[] l:symlinks){
   File f=safe(dest,l[0]);
   f.getParentFile().mkdirs();f.delete();
   try{android.system.Os.symlink(l[1],f.getAbsolutePath());continue;}catch(Throwable ignored){}
   File target=l[1].startsWith("/")?safe(dest,l[1].substring(1)):new File(f.getParentFile(),l[1]);
   if(!target.isFile())throw new IOException("Не удалось воссоздать ссылку "+l[0]+" -> "+l[1]+": Box64 не найдёт guest glibc");
   copy(target,f);
  }
 }
 private static void copy(File from,File to)throws IOException{if(!from.isFile())return;try(InputStream i=new FileInputStream(from);OutputStream o=new FileOutputStream(to)){byte[]b=new byte[65536];int n;while((n=i.read(b))>0)o.write(b,0,n);}}
}
