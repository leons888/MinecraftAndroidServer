package com.bdscontrol.app;

import java.io.*;import java.nio.file.Path;import java.util.*;import java.util.zip.*;import org.apache.commons.compress.archivers.tar.*;import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;

/** Safe archive extraction with complete, order-independent link materialization. */
final class ArchiveExtractor {
 static void zip(File archive,File dest)throws Exception{dest.mkdirs();try(ZipInputStream in=new ZipInputStream(new BufferedInputStream(new FileInputStream(archive)))){ZipEntry e;byte[]b=new byte[65536];while((e=in.getNextEntry())!=null){File f=safe(dest,e.getName());if(e.isDirectory())f.mkdirs();else{f.getParentFile().mkdirs();try(OutputStream o=new FileOutputStream(f)){int n;while((n=in.read(b))>0)o.write(b,0,n);}}}}}
 static void tarGz(File archive,File dest)throws Exception{try(InputStream in=new GZIPInputStream(new BufferedInputStream(new FileInputStream(archive)))){tar(in,dest);}}
 static void tarXz(File archive,File dest)throws Exception{try(InputStream in=new XZCompressorInputStream(new BufferedInputStream(new FileInputStream(archive)))){tar(in,dest);}}
 private static String norm(String n){return n.replace('\\','/').replaceFirst("^/+","").replaceFirst("^\\./","");}
 private static File safe(File d,String n)throws IOException{File f=new File(d,n);if(!f.toPath().normalize().startsWith(d.toPath().normalize()))throw new SecurityException("unsafe archive path");return f;}
 private static File archivePath(File root,String n)throws IOException{return safe(root,norm(n));}
 private static void tar(InputStream in,File dest)throws Exception{
  dest.mkdirs();Map<String,String> links=new LinkedHashMap<>();Set<String> materialized=new HashSet<>();byte[] b=new byte[65536];
  try(TarArchiveInputStream t=new TarArchiveInputStream(in)){
   TarArchiveEntry e;while((e=t.getNextTarEntry())!=null){String name=norm(e.getName());File out=archivePath(dest,name);if(e.isDirectory()){out.mkdirs();continue;}out.getParentFile().mkdirs();if(e.isSymbolicLink()||e.isLink()){links.put(name,e.getLinkName());continue;}try(OutputStream o=new FileOutputStream(out)){int n;while((n=t.read(b))>0)o.write(b,0,n);}materialized.add(name);}
  }
  int pending=links.size();while(pending>0){int before=pending;for(Map.Entry<String,String> e:links.entrySet()){if(materialized.contains(e.getKey()))continue;String target=resolveTarget(e.getKey(),e.getValue());String resolved=resolveLinkName(target,links,new HashSet<>());File source=archivePath(dest,resolved);if(!source.isFile())continue;File out=archivePath(dest,e.getKey());copy(source,out);if(!out.isFile()||out.length()==0)throw new IOException("Не удалось материализовать guest-файл "+e.getKey());materialized.add(e.getKey());pending--;}
   if(pending==0)break;if(before==pending){List<String> missing=new ArrayList<>();for(Map.Entry<String,String> e:links.entrySet())if(!materialized.contains(e.getKey()))missing.add(e.getKey()+" -> "+e.getValue());throw new IOException("Не разрешены guest-ссылки: "+missing);}
  }
 }
 private static String resolveTarget(String link,String raw){String n=norm(raw);if(raw.startsWith("./")||raw.startsWith("/"))return n;int slash=link.lastIndexOf('/');return slash<0?n:norm(link.substring(0,slash+1)+n);}
 private static String resolveLinkName(String name,Map<String,String> links,Set<String> seen)throws IOException{String n=norm(name);String raw=links.get(n);if(raw==null)return n;if(!seen.add(n))throw new IOException("Циклическая guest-ссылка: "+n);return resolveLinkName(resolveTarget(n,raw),links,seen);}
 private static void copy(File from,File to)throws IOException{to.getParentFile().mkdirs();try(InputStream i=new FileInputStream(from);OutputStream o=new FileOutputStream(to)){byte[]b=new byte[65536];int n;while((n=i.read(b))>0)o.write(b,0,n);}}
}
