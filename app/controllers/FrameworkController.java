package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.sunbird.FWCreateExcel;
import org.sunbird.FWNewMasterFile;
import org.sunbird.IniFile;
import play.mvc.*;
import utils.ResponseHeaders;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
@With(ResponseHeaders.class)
public class FrameworkController extends Controller {
    Path currentRelativePath = Paths.get("");
    String strFilePath = currentRelativePath.toAbsolutePath().toString();
    public static File xlsfile;
    public CompletionStage<Result> createExcel() throws  Exception{
       System.out.println(request());
        System.out.println(request().body().asJson().toString());
        String strFrameworkId = request().body().asJson().get("framework").asText();
        FWCreateExcel fwCreateExcel = new FWCreateExcel();
       Map<String,Object> res = fwCreateExcel.createExcel(strFrameworkId);
       File data= ((File)res.get("file"));
       Result result = ok(data);
CompletionStage<Result> response =
        CompletableFuture.completedFuture(result);
return response;
    }
    public CompletionStage<Result> publishFramework() throws  Exception{
        String strFrameworkId = request().body().asJson().get("framework").asText();
        String strChannel = request().body().asJson().get("channel").asText();
        IniFile loadConfig = new IniFile(strFilePath + "/configLive.ini");

        FWNewMasterFile fwNewMasterFile = new FWNewMasterFile(loadConfig);
      String data =  fwNewMasterFile.publishFramework(strFrameworkId,strChannel);
        Result result = ok(data);
        CompletionStage<Result> response =
                CompletableFuture.completedFuture(result);
        return response;
    }
    public CompletionStage<Result> createupdateoperation() throws  Exception{
        int opt =1;
        Http.MultipartFormData body = request().body().asMultipartFormData();
        System.out.println("Form body " + body);
        Http.MultipartFormData.FilePart<File> filePart = body.getFile("File");
        String strFileExtn = (filePart.getFilename().substring(filePart.getFilename().lastIndexOf(".")+1));
        if(!(strFileExtn.equalsIgnoreCase("xlsx") || strFileExtn.equalsIgnoreCase("xls")))
        {
            System.out.println("Incorrect file format");
          System.exit(0);
        }
         xlsfile = filePart.getFile();
        // FileReader br = new FileReader(xlsfile);
        String strFrameworkName =  (((String[]) (body.asFormUrlEncoded().get("fwName")))[0]);
        String strFrameworkId = (((String[]) (body.asFormUrlEncoded().get("fwCode")))[0]);
        String strFrameworkDescr = (((String[]) (body.asFormUrlEncoded().get("fwDescription")))[0]);
        String action = (((String[]) (body.asFormUrlEncoded().get("action")))[0]);

        String strChannel = (((String[]) (body.asFormUrlEncoded().get("channel")))[0]);
        IniFile loadConfig = new IniFile(strFilePath + "/configLive.ini");
        if(action.equalsIgnoreCase("update")){
            opt = 2;
            }
        else if(action.equalsIgnoreCase("create")){
            opt = 1;
        }
        FWNewMasterFile fwNewMasterFile = new FWNewMasterFile(loadConfig);
        String data =  fwNewMasterFile.createFramework(xlsfile, strFrameworkName, strFrameworkId, strFrameworkDescr, strChannel,opt);

        Result result = ok(data);
        CompletionStage<Result> response =
                CompletableFuture.completedFuture(result);
        return response;
    }

}
