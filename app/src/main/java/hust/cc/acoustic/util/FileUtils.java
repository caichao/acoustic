package hust.cc.acoustic.util;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * Created by cc on 2017/3/24.
 */

public class FileUtils {
    public static final String SDPATH = Environment.getExternalStorageDirectory()+ File.separator;//"/sdcard/";

    public void saveBytes(short[] bytes, String name){
        File file = new File(SDPATH+name+".txt");
        FileWriter fw = null;
        try {
            fw = new FileWriter(file);
            if(!file.exists())
                file.createNewFile();
            for(int i = 0; i < bytes.length ; i++){
                fw.write(String.valueOf(bytes[i]) + "\r\n");
                fw.flush();
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                if(fw != null)
                    fw.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public static double[] readTxt(String name, int length){
        String tmp = "";
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        double[] pcm = null;
        try{
            fileReader = new FileReader(new File(SDPATH + name));
            bufferedReader = new BufferedReader(fileReader);
            pcm = new double[length];
            int i = 0;
            while ((tmp = bufferedReader.readLine()) != null){
                pcm[i++] = (short) Float.parseFloat(tmp);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try{
                if(bufferedReader!= null)
                    bufferedReader.close();
                if(fileReader != null)
                    fileReader.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return pcm;
    }

    public static double[] readFilterCoefficient(String name, int length){
        String tmp = "";
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        double[] filterCoefficient = null;
        try{
            fileReader = new FileReader(new File(SDPATH + name));
            bufferedReader = new BufferedReader(fileReader);
            filterCoefficient = new double[length];
            int i = 0;
            while ((tmp = bufferedReader.readLine()) != null){
                filterCoefficient[i++] = Double.parseDouble(tmp);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try{
                if(bufferedReader!= null)
                    bufferedReader.close();
                if(fileReader != null)
                    fileReader.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return filterCoefficient;
    }
}
