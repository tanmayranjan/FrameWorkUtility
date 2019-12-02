package controllers;

import org.sunbird.FWCreateExcel;
import org.sunbird.FWNewMasterFile;
import org.sunbird.IniFile;
import play.mvc.Controller;
import play.mvc.Filter;
import play.mvc.Result;
import play.mvc.With;
import utils.ResponseHeaders;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
@With(ResponseHeaders.class)
public class FrameworkController extends Controller {
    Path currentRelativePath = Paths.get("");
    String strFilePath = currentRelativePath.toAbsolutePath().toString();

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
        System.out.println(request());
        System.out.println(request().body().asJson().toString());
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

    /*public void getFileData(){

    }*/
}
