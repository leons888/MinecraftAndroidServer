package com.bdscontrol.app;

import java.io.*;import java.net.*;import java.security.*;

final class DownloadManager {
    interface Progress{void update(long done,long total);}
    static File download(String url,File out,Progress p)throws Exception{HttpURLConnection c=(HttpURLConnection)new URL(url).openConnection();c.setRequestProperty("User-Agent","BDS-Control/0.1");c.connect();if(c.getResponseCode()/100!=2)throw new IOException("HTTP "+c.getResponseCode());long total=c.getContentLengthLong(),done=0;try(InputStream in=c.getInputStream();OutputStream os=new FileOutputStream(out)){byte[]b=new byte[65536];int n;while((n=in.read(b))>0){os.write(b,0,n);done+=n;if(p!=null)p.update(done,total);}}return out;}
    static String sha256(File f)throws Exception{MessageDigest d=MessageDigest.getInstance("SHA-256");try(InputStream in=new FileInputStream(f)){byte[]b=new byte[65536];int n;while((n=in.read(b))>0)d.update(b,0,n);}StringBuilder s=new StringBuilder();for(byte x:d.digest())s.append(String.format("%02x",x));return s.toString();}
}
