package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import javassist.bytecode.Descriptor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.sunbird.FWCreateExcel;
import org.sunbird.FWNewMasterFile;
import org.sunbird.IniFile;
import play.mvc.*;
import scala.util.parsing.json.JSON;
import utils.ResponseHeaders;

import java.io.*;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
@With(ResponseHeaders.class)
public class FrameworkController extends Controller {
    Path currentRelativePath = Paths.get("");
    String strFilePath = currentRelativePath.toAbsolutePath().toString();
    public static File xlsfile;
    public static String reqCompleted = "false";

    public CompletionStage<Result> createExcel() {
        CompletionStage<Result> response = null;
        System.out.println(request());
        try {
            String strFrameworkId = request().body().asJson().get("framework").asText();
            FWCreateExcel fwCreateExcel = new FWCreateExcel();
            Map<String, Object> res = fwCreateExcel.createExcel(strFrameworkId);
            File data = ((File) res.get("file"));
            Result result = ok(data);
            response =
                    CompletableFuture.completedFuture(result);
            return response;
        } catch (Exception e) {
            System.out.println(e);
            return response;
        }
    }

    public CompletionStage<Result> publishFramework() {
        CompletionStage<Result> response = null;
        try {
            System.out.println(request());
            String strFrameworkId = request().body().asJson().get("framework").asText();
            //  String strChannel = request().body().asJson().get("channel").asText();
            IniFile loadConfig = new IniFile(strFilePath + "/configLive.ini");
            FWNewMasterFile fwNewMasterFile = new FWNewMasterFile(loadConfig);
            String data = fwNewMasterFile.publishFramework(strFrameworkId);
            Result result = ok(data);
            response = CompletableFuture.completedFuture(result);
            return response;
        } catch (Exception e) {
            System.out.println(e);
            return response;
        }

    }

    public CompletionStage<Result> createupdateoperation() {
        int opt = 1;
        System.out.println(request());
        String strFrameworkDescr = "";
        CompletionStage<Result> response = null;
        try {
            final Socket socket;
            Http.MultipartFormData body = request().body().asMultipartFormData();
            Http.MultipartFormData.FilePart<File> filePart = body.getFile("File");
            String strFileExtn = (filePart.getFilename().substring(filePart.getFilename().lastIndexOf(".") + 1));
            xlsfile = filePart.getFile();
            String strFrameworkName = (((String[]) (body.asFormUrlEncoded().get("fwName")))[0]);
            String strFrameworkId = (((String[]) (body.asFormUrlEncoded().get("fwCode")))[0]);
            if (body.asFormUrlEncoded().get("fwDescription") != null) {
                strFrameworkDescr = (((String[]) (body.asFormUrlEncoded().get("fwDescription")))[0]);

            }
            String action = (((String[]) (body.asFormUrlEncoded().get("action")))[0]);
            // String strChannel = (((String[]) (body.asFormUrlEncoded().get("channel")))[0]);
            IniFile loadConfig = new IniFile(strFilePath + "/configLive.ini");
            if (action.equalsIgnoreCase("update")) {
                opt = 2;
            } else if (action.equalsIgnoreCase("create")) {
                opt = 1;
            }
            FWNewMasterFile fwNewMasterFile = new FWNewMasterFile(loadConfig);
            reqCompleted = "false";
            String data = fwNewMasterFile.createFramework(xlsfile, strFileExtn, strFrameworkName, strFrameworkId, strFrameworkDescr, opt);

            Result result = ok(data);
            response = CompletableFuture.completedFuture(result);
            return response;
        } catch (Exception e) {
            System.out.println(e);
            return response;
        }
    }

    public CompletionStage<Result> deleteOperation() {
        CompletionStage<Result> response = null;
        String term = "";
        String data;
        try {
            IniFile loadConfig = new IniFile(strFilePath + "/configLive.ini");
            FWNewMasterFile fwNewMasterFile = new FWNewMasterFile(loadConfig);
            System.out.println(request());
            String type = request().body().asJson().get("type").asText();
            String strFrameworkId = request().body().asJson().get("fwCode").asText();
            String category = request().body().asJson().get("catgCode").asText();
            if (request().body().asJson().get("termCode") != null) {
                term = request().body().asJson().get("termCode").asText();
            }
            if (term == "") {
                data = fwNewMasterFile.deleteCategory(strFrameworkId, category);
            } else {
                data = fwNewMasterFile.deleteTerm(strFrameworkId, category, term);
            }
            Result result = ok(data);
            response = CompletableFuture.completedFuture(result);
            return response;
        } catch (Exception e) {
            System.out.println(e);
            return response;
        }
    }

    public CompletionStage<Result> liveTerms() {
        CompletionStage<Result> response = null;
        JSONArray terms = null;
        ArrayList<JsonNode> data = new ArrayList<>();
        String status;
        try {
            IniFile loadConfig = new IniFile(strFilePath + "/configLive.ini");
            FWNewMasterFile fwNewMasterFile = new FWNewMasterFile(loadConfig);
            System.out.println(request());
            int action = request().body().asJson().get("action").asInt();
            Iterator<JsonNode> nodeIterator = request().body().asJson().get("terms").iterator();
            while (nodeIterator.hasNext()) {
                JsonNode node = nodeIterator.next();
                String identifier = node.get("identifier").asText();
                String[] tempArray = identifier.split("_");
                status = fwNewMasterFile.readTerm(tempArray[0], tempArray[1], tempArray[2], action);
                if (status.equalsIgnoreCase("Live")) {
                    data.add(node);
                }
            }
            // System.out.println(data.toString());
            Result result = ok(data.toString());
            response = CompletableFuture.completedFuture(result);
            return response;
        } catch (Exception e) {
            System.out.println(e);
            return response;
        }
    }

    public CompletionStage<Result> setdefaultframework() {
        CompletionStage<Result> response = null;
        String status = "";
        try {
            IniFile loadConfig = new IniFile(strFilePath + "/configLive.ini");
            FWNewMasterFile fwNewMasterFile = new FWNewMasterFile(loadConfig);
            System.out.println(request());
            String strFrameworkId = request().body().asJson().get("fwCode").asText();
            String rootorgId = request().body().asJson().get("rootorgId").asText();

            status = fwNewMasterFile.setDefaultFramework(strFrameworkId, rootorgId);
            // System.out.println(data.toString());
            Result result = ok(status);
            response = CompletableFuture.completedFuture(result);
            return response;
        } catch (Exception e) {
            System.out.println(e);
            return response;
        }
    }


    public CompletionStage<Result> getStatus() {
        CompletionStage<Result> response = null;
        try {
            System.out.println(request());
            //  String strFrameworkId = request().body().asJson().get("framework").asText();
            //  String strChannel = request().body().asJson().get("channel").asText();
            //   IniFile loadConfig = new IniFile(strFilePath + "/configLive.ini");
            // FWNewMasterFile fwNewMasterFile = new FWNewMasterFile(loadConfig);
            String data = reqCompleted;
            Result result = ok(data);
            response = CompletableFuture.completedFuture(result);
            return response;
        } catch (Exception e) {
            System.out.println(e);
            response = CompletableFuture.completedFuture(ok("failed"));
            return response;
        }

    }
}
