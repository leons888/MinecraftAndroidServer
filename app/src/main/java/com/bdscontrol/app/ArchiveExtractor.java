package com.bdscontrol.app;

import java.io.*;import java.nio.charset.StandardCharsets;import java.util.zip.*;import org.apache.commons.compress.archivers.tar.*;import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;

final class ArchiveExtractor {
 static void zip(File archive,File dest)throws Exception{dest.mkdirs();try(ZipInputStream in=new ZipInputStream(new FileInputStream(archive))){ZipEntry e;byte[]b=new byte[65536];while((e=in.getNextEntry())!=null){File f=safe(dest,e.getName());if(e.isDirectory())f.mkdirs();else{f.getParentFile().mkdirs();try(OutputStream o=new FileOutputStream(f)){int n;while((n=in.read(b))>0)o.write(b,0,n);}}}}}
 static void tarGz(File archive,File dest)throws Exception{try(InputStream in=new GZIPInputStream(new FileInputStream(archive))){tar(in,dest);}}
 static void tarXz(File archive,File dest)throws Exception{try(InputStream in=new XZCompressorInputStream(new BufferedInputStream(new FileInputStream(archive)))){tar(in,dest);}}
 private static File safe(File d,String n)throws IOException{File f=new File(d,n);if(!f.toPath().normalize().startsWith(d.toPath().normalize()))throw new SecurityException("unsafe archive path");return f;}
 private static void tar(InputStream in,File dest)throws Exception{dest.mkdirs();try(TarArchiveInputStream t=new TarArchiveInputStream(in)){TarArchiveEntry e;byte[]b=new byte[65536];while((e=t.getNextTarEntry())!=null){File f=safe(dest,e.getName());if(e.isDirectory())f.mkdirs();else{f.getParentFile().mkdirs();try(OutputStream o=new FileOutputStream(f)){int n;while((n=t.read(b))>0)o.write(b,0,n);}}}}}
}
