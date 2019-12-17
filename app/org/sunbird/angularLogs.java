package org.sunbird;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class angularLogs {

    public angularLogs() throws IOException {
    }

    public  void generateLogs(String message){
/*
        String dateInString = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss").format(new Date());
        dateInString = dateInString.replaceAll(":", "-");
*/

        try {
            File file = new File("angularLogs.log");
            FileWriter fr = new FileWriter(file, true);
            fr.write( message + "\n");
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
