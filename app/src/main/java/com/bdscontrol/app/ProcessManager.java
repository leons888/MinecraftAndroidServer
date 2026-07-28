package com.bdscontrol.app;

import java.io.*;
import java.util.*;

final class ProcessManager {
    private Process process;
    synchronized boolean start(List<String> command, File dir, Map<String,String> env, LineSink sink) throws IOException {
        if(process!=null&&process.isAlive()) return false;
        ProcessBuilder b=new ProcessBuilder(command).directory(dir).redirectErrorStream(false); b.environment().putAll(env); process=b.start();
        stream(process.getInputStream(),sink,"stdout"); stream(process.getErrorStream(),sink,"stderr"); return true;
    }
    private void stream(InputStream in,LineSink sink,String source){ new Thread(()->{try(BufferedReader r=new BufferedReader(new InputStreamReader(in))){String l;while((l=r.readLine())!=null)sink.line(source,l);}catch(IOException e){sink.line(source,e.toString());}},"bds-log").start(); }
    synchronized void write(String s)throws IOException{if(process!=null&&process.isAlive()){OutputStream o=process.getOutputStream();o.write((s+"\n").getBytes());o.flush();}}
    synchronized void stop(){if(process!=null){process.destroy();process=null;}}
    synchronized boolean alive(){return process!=null&&process.isAlive();}
    interface LineSink{void line(String source,String line);}
}
