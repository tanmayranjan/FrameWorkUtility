package org.sunbird;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class Report {
    public  static JSONObject keys = new JSONObject();
    public static int count = 0;
    public static String createReport(String strFrameworkId,String action,String processId){
        JSONObject process = new JSONObject();
        process.put("action",action);
        process.put("status", "started");
        process.put("reason","");
        keys.put(processId,process);
         writeReport();
        return processId;
    }
    public void changeReport(String Pid,String status,String reason){
        ((JSONObject)keys.get(Pid)).put("status",status);
        ((JSONObject)keys.get(Pid)).put("reason",reason);
        writeReport();
    }
   public static void writeReport(){
        try (FileWriter file = new FileWriter("processes.json")) {

            file.write(keys.toString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public Map<String,Object> returnReport(){
        try{
            File newFile = new File("processes.json");
           // FileOutputStream out = new FileOutputStream(newFile);
            Map<String, Object> fileCreated = new HashMap<>();
            fileCreated.put("file", newFile);
            return  fileCreated;

        }
        catch (Exception e){

            System.out.println("Exception in returning json file =>"  + e);
            return null;
        }

    }
}
