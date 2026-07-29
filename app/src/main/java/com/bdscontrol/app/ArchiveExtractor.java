package com.bdscontrol.app;

import java.io.*;import java.util.*;import java.util.zip.*;import org.apache.commons.compress.archivers.tar.*;import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;

final class ArchiveExtractor {
 static void zip(File archive,File dest)throws Exception{dest.mkdirs();try(ZipInputStream in=new ZipInputStream(new FileInputStream(archive))){ZipEntry e;byte[]b=new byte[65536];while((e=in.getNextEntry())!=null){File f=safe(dest,e.getName());if(e.isDirectory())f.mkdirs();else{f.getParentFile().mkdirs();try(OutputStream o=new FileOutputStream(f)){int n;while((n=in.read(b))>0)o.write(b,0,n);}}}}}
 static void tarGz(File archive,File dest)throws Exception{try(InputStream in=new GZIPInputStream(new BufferedInputStream(new FileInputStream(archive)))){tar(in,dest);}}
 static void tarXz(File archive,File dest)throws Exception{try(InputStream in=new XZCompressorInputStream(new BufferedInputStream(new FileInputStream(archive)))){tar(in,dest);}}
 private static File safe(File d,String n)throws IOException{File f=new File(d,n);if(!f.toPath().normalize().startsWith(d.toPath().normalize()))throw new SecurityException("unsafe archive path");return f;}
 private static void tar(InputStream in,File dest)throws Exception{
  dest.mkdirs();List<String[]> links=new ArrayList<>();Map<String,String> linkMap=new HashMap<>();
  try(TarArchiveInputStream t=new TarArchiveInputStream(in)){TarArchiveEntry e;byte[]b=new byte[65536];while((e=t.getNextTarEntry())!=null){File f=safe(dest,e.getName());if(e.isDirectory()){f.mkdirs();continue;}if(e.isSymbolicLink()){links.add(new String[]{e.getName(),e.getLinkName()});linkMap.put(e.getName(),e.getLinkName());continue;}if(e.isLink()){links.add(new String[]{e.getName(),e.getLinkName()});linkMap.put(e.getName(),e.getLinkName());continue;}f.getParentFile().mkdirs();try(OutputStream o=new FileOutputStream(f)){int n;while((n=t.read(b))>0)o.write(b,0,n);}}}
  for(String[] link:links){File out=safe(dest,link[0]);File target=resolveChain(dest,out,link[1],linkMap,new HashSet<>());out.getParentFile().mkdirs();out.delete();if(!target.isFile())throw new IOException("Не найден target guest-библиотеки "+link[0]+" -> "+link[1]+" (resolved: "+target+ ")");copy(target,out);if(!out.isFile()||out.length()==0)throw new IOException("Не удалось установить guest-библиотеку "+link[0]+" -> "+link[1]);}
 }
 private static File resolveChain(File dest,File link,String name,Map<String,String> links,Set<String> seen)throws IOException{
  File candidate;if(name.startsWith("/"))candidate=new File(dest,name.substring(1));else if(name.startsWith("./"))candidate=new File(dest,name.substring(2));else candidate=new File(link.getParentFile(),name);
  candidate=candidate.getCanonicalFile();if(!candidate.toPath().startsWith(dest.getCanonicalFile().toPath()))throw new SecurityException("unsafe link target");
  String rel=dest.getCanonicalFile().toPath().relativize(candidate.toPath()).toString();String mapped=links.get(rel);if(mapped==null){File f=candidate;if(!f.isFile()){f=findByName(dest,candidate.getName());}return f;}
  if(!seen.add(rel))throw new IOException("Циклическая guest-ссылка: "+rel);return resolveChain(dest,candidate,mapped,links,seen);
 }
 private static File findByName(File root,String name){ArrayDeque<File> q=new ArrayDeque<>();q.add(root);while(!q.isEmpty()){File d=q.remove();File[] fs=d.listFiles();if(fs==null)continue;for(File f:fs){if(f.isFile()&&!f.isDirectory()&&f.getName().equals(name))return f;if(f.isDirectory())q.add(f);}}return new File(root,name);}
 private static void copy(File from,File to)throws IOException{try(InputStream i=new FileInputStream(from);OutputStream o=new FileOutputStream(to)){byte[]b=new byte[65536];int n;while((n=i.read(b))>0)o.write(b,0,n);}}
}
