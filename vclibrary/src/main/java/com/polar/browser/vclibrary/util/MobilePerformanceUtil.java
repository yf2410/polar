package com.polar.browser.vclibrary.util;

import android.app.ActivityManager;
import android.content.Context;
import android.text.format.Formatter;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * Created by FKQ on 2016/12/15.
 */

public class MobilePerformanceUtil {

    /** 获取手机CUP使用率
     * @return
     */
    public static String getProcessCpuRate() {

        StringBuilder tv = new StringBuilder();
        String rate = "CPU";
        try {
            String Result;
            Process p;
            p = Runtime.getRuntime().exec("top -n 1");

            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((Result = br.readLine()) != null) {
                if (Result.trim().length() < 1) {
                    continue;
                } else {
                    String[] CPUusr = Result.split("%");
                    tv.append("USER:" + CPUusr[0] + "\n");
                    String[] CPUusage = CPUusr[0].split("User");
                    String[] SYSusage = CPUusr[1].split("System");
                    tv.append("CPU:" + CPUusage[1].trim() + " length:" + CPUusage[1].trim().length() + "\n");
                    tv.append("SYS:" + SYSusage[1].trim() + " length:" + SYSusage[1].trim().length() + "\n");

                    rate = String.valueOf(Integer.parseInt(CPUusage[1].trim()) + Integer.parseInt(SYSusage[1].trim()));
                    break;
                }
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return rate;
    }

//    public static String getNetType () {
//        if (NetWorkUtils.isWifiConnected())
//    }

    public static int getProcessCpuRates() {
        String[] cpuInfos = null;
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream("/proc/stat")), 1000);
            String load = reader.readLine();
            reader.close();
            cpuInfos = load.split(" ");
        }catch(IOException ex){
//            Log.e(TAG, "IOException" + ex.toString());
            return 0;
        }
        long totalCpu = 0;
        try{
            totalCpu = Long.parseLong(cpuInfos[2])
                    + Long.parseLong(cpuInfos[3]) + Long.parseLong(cpuInfos[4])
                    + Long.parseLong(cpuInfos[6]) + Long.parseLong(cpuInfos[5])
                    + Long.parseLong(cpuInfos[7]) + Long.parseLong(cpuInfos[8]);
        }catch(ArrayIndexOutOfBoundsException e){
//            Log.i(TAG, "ArrayIndexOutOfBoundsException" + e.toString());
            return 0;
        }
        return (int) totalCpu;
    }

    public static float getProcessCpuRatef() {

        float totalCpuTime1 = getTotalCpuTime();
        float processCpuTime1 = getAppCpuTime();
        try
        {
            Thread.sleep(360);
        }
        catch (Exception e)
        {
        }

        float totalCpuTime2 = getTotalCpuTime();
        float processCpuTime2 = getAppCpuTime();

        float cpuRate = 100 * (processCpuTime2 - processCpuTime1)
                / (totalCpuTime2 - totalCpuTime1);

        return cpuRate;
    }

    public static long getTotalCpuTime() {   // 获取系统总CPU使用时间
        String[] cpuInfos = null;
        try
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream("/proc/stat")), 1000);
            String load = reader.readLine();
            reader.close();
            cpuInfos = load.split(" ");
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        long totalCpu = Long.parseLong(cpuInfos[2])
                + Long.parseLong(cpuInfos[3]) + Long.parseLong(cpuInfos[4])
                + Long.parseLong(cpuInfos[6]) + Long.parseLong(cpuInfos[5])
                + Long.parseLong(cpuInfos[7]) + Long.parseLong(cpuInfos[8]);
        return totalCpu;
    }

    public static long getAppCpuTime() {   // 获取应用占用的CPU时间
        String[] cpuInfos = null;
        try
        {
            int pid = android.os.Process.myPid();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream("/proc/" + pid + "/stat")), 1000);
            String load = reader.readLine();
            reader.close();
            cpuInfos = load.split(" ");
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        long appCpuTime = Long.parseLong(cpuInfos[13])
                + Long.parseLong(cpuInfos[14]) + Long.parseLong(cpuInfos[15])
                + Long.parseLong(cpuInfos[16]);
        return appCpuTime;
    }

    public static void displayBriefMemory(Context context) {

        final ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);

        ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();

        activityManager.getMemoryInfo(info);

//        Log.i(tag,"系统剩余内存:"+(info.availMem >> 10)+"k");
        System.out.println("--MyLog--系统剩余内存:"+(info.availMem >> 10)/1024/1024);

//        Log.i(tag,"系统是否处于低内存运行："+info.lowMemory);
        System.out.println("--MyLog--系统是否处于低内存运行："+info.lowMemory);

//        Log.i(tag,"当系统剩余内存低于"+info.threshold+"时就看成低内存运行");
        System.out.println("--MyLog--当系统剩余内存低于"+info.threshold/1024/1024+"时就看成低内存运行");

    }

    /**
     * 获取android当前剩余内存大小
     * @param context
     * @return
     */
    public static String getAvailMemory(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        return Formatter.formatFileSize(context, mi.availMem);// 将获取的内存大小规格化
    }

    public static String getTotalMemory1(Context context) {
        String str1 = "/proc/meminfo";// 系统内存信息文件
        String str2;
        String[] arrayOfString;
        long initial_memory = 0;

        try {
            FileReader localFileReader = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(
                    localFileReader, 8192);
            str2 = localBufferedReader.readLine();// 读取meminfo第一行，系统总内存大小

            arrayOfString = str2.split("\\s+");
            for (String num : arrayOfString) {
                Log.i(str2, num + "\t");
            }

            initial_memory = Integer.valueOf(arrayOfString[1]).intValue() * 1024;// 获得系统总内存，单位是KB，乘以1024转换为Byte
            localBufferedReader.close();

        } catch (IOException e) {
        }
        return Formatter.formatFileSize(context, initial_memory);// Byte转换为KB或者MB，内存大小规格化
    }

    /**
     * 应用程序已获得内存
     * @return
     */
    public static String appUserMemory() {
        long totalMemorys = ((int) Runtime.getRuntime().totalMemory())/1024/1024;
        return String.valueOf(totalMemorys) + "MB";
    }
}


