package com.bdscontrol.app;

import java.io.*;import java.nio.charset.StandardCharsets;import java.util.zip.*;

final class ArchiveExtractor {
 static void zip(File archive,File dest)throws Exception{dest.mkdirs();try(ZipInputStream in=new ZipInputStream(new FileInputStream(archive))){ZipEntry e;byte[]b=new byte[65536];while((e=in.getNextEntry())!=null){File f=safe(dest,e.getName());if(e.isDirectory())f.mkdirs();else{f.getParentFile().mkdirs();try(OutputStream o=new FileOutputStream(f)){int n;while((n=in.read(b))>0)o.write(b,0,n);}}}}}
 static void tarGz(File archive,File dest)throws Exception{dest.mkdirs();try(InputStream in=new GZIPInputStream(new FileInputStream(archive))){tar(in,dest);}}
 private static File safe(File d,String n)throws IOException{File f=new File(d,n);if(!f.toPath().normalize().startsWith(d.toPath().normalize()))throw new SecurityException("unsafe archive path");return f;}
 private static void tar(InputStream in,File dest)throws Exception{byte[]h=new byte[512],b=new byte[65536];while(read(in,h)==512&&!empty(h)){String n=text(h,0,100),ln=text(h,345,155);if(!ln.isEmpty())n=ln;long size=oct(h,124,12);int type=h[156];File f=safe(dest,n);if(type=='5')f.mkdirs();else{f.getParentFile().mkdirs();try(OutputStream o=new FileOutputStream(f)){long left=size;while(left>0){int k=in.read(b,0,(int)Math.min(b.length,left));if(k<0)throw new EOFException();o.write(b,0,k);left-=k;}}}long pad=(512-size%512)%512;while(pad>0){long k=in.skip(pad);if(k<=0)throw new EOFException();pad-=k;}}}
 private static int read(InputStream i,byte[]b)throws IOException{int p=0,n;while(p<b.length&&(n=i.read(b,p,b.length-p))>0)p+=n;return p;}private static boolean empty(byte[]b){for(byte x:b)if(x!=0)return false;return true;}private static String text(byte[]b,int p,int n){int e=p;while(e<p+n&&b[e]!=0)e++;return new String(b,p,e-p,StandardCharsets.UTF_8).trim();}private static long oct(byte[]b,int p,int n){String s=text(b,p,n).trim();return s.isEmpty()?0:Long.parseLong(s,8);}
}
