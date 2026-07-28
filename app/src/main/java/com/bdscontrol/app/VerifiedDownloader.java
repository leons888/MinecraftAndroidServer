package com.bdscontrol.app;

import java.io.*;import java.net.*;import java.security.*;

final class VerifiedDownloader {
 interface Progress{void onProgress(long done,long total);}
 static File fetch(String url,String sha,File out,Progress p)throws Exception{HttpURLConnection c=(HttpURLConnection)new URL(url).openConnection();c.setConnectTimeout(20000);c.setReadTimeout(60000);c.setRequestProperty("User-Agent","BDS-Control/1.0");c.connect();if(c.getResponseCode()/100!=2)throw new IOException("HTTP "+c.getResponseCode());MessageDigest d=MessageDigest.getInstance("SHA-256");long total=c.getContentLengthLong(),done=0;out.getParentFile().mkdirs();File tmp=new File(out.getPath()+".part");try(InputStream in=c.getInputStream();OutputStream os=new FileOutputStream(tmp)){byte[]b=new byte[65536];int n;while((n=in.read(b))>0){os.write(b,0,n);d.update(b,0,n);done+=n;if(p!=null)p.onProgress(done,total);}}StringBuilder h=new StringBuilder();for(byte x:d.digest())h.append(String.format("%02x",x));if(sha==null||sha.isEmpty()||sha.startsWith("REPLACE_"))throw new SecurityException("Missing verified SHA-256 for artifact");if(!h.toString().equalsIgnoreCase(sha))throw new SecurityException("SHA-256 mismatch: expected "+sha+", got "+h);if(!tmp.renameTo(out))throw new IOException("atomic rename failed");return out;}
}
