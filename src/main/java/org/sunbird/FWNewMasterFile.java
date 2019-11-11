
package org.sunbird;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class FWNewMasterFile {
    IniFile configFile = null;
    Logger logger = null;
    String strToken = null, strApiUrl, strApiBody, strResponse = "";
    JSONParser parser = new JSONParser();
    Object obj;
    JSONObject jsonObject;
    static ArrayList<String> errors = new ArrayList<String>();
static  JSONObject getFWresponse;
    public FWNewMasterFile(IniFile loadConfig) throws IOException {
        configFile = loadConfig;

        strToken = configFile.getString("API", "api_token", "");
        strToken = strToken.replace("'", "");

        logger = Logger.getLogger("FrameworkCreationLog");

        FileHandler fh;

        Path currentRelativePath = Paths.get("D:\\TaxonomyFiles(copy)\\Taxonomy Files\\logs\\log");
        String strFilePath = currentRelativePath.toAbsolutePath().toString();

        String dateInString = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss").format(new Date());
        dateInString = dateInString.replaceAll(":", "-");
        // This block configure the logger with handler and formatter
        fh = new FileHandler(strFilePath + dateInString + ".log");
        logger.addHandler(fh);
        SimpleFormatter formatter = new SimpleFormatter();
        fh.setFormatter(formatter);
    }

    public void createFramework(String strInputExcelFile, String strFrameworkName, String strFrameworkId, String strFrameworkDescr, String strChannel) {
      try {
          JSONParser parser = new JSONParser();

          // Validate Channel
          String strGetChannelAPIURL = configFile.getString("API", "api_base_url", "") + configFile.getString("API", "api_get_channel", "") + strChannel;
          String strGetChannelAPIResponse = Postman.getDetails(logger, strGetChannelAPIURL, strToken);
          JSONObject getChannelResponse = (JSONObject) parser.parse(strGetChannelAPIResponse);
          JSONObject getChannelParams = (JSONObject) getChannelResponse.get("params");
          String strGetChannelStatus = getChannelParams.get("status").toString();

          if (strGetChannelStatus.equalsIgnoreCase("failed")) {
              System.out.println("Channel not found");
              System.exit(0);
          }
          // Validate Framework
         String strFWGetStatus = readFramework(strFrameworkId);
          JSONObject jsonFWDetails = null;

          if (strFWGetStatus.equalsIgnoreCase("failed")) {
              //create framework
              strApiUrl = configFile.getString("API", "api_base_url", "") + configFile.getString("API", "api_framework_create", "");


              String strApiBody = "{\"request\": {\"framework\": {\"name\": \"" + strFrameworkName + "\", \"description\": \"" + strFrameworkDescr + "\","
                      + "\"code\": \"" + strFrameworkId + "\"}}}";


              logger.finest("In Framework MasterFile --> FrameworkCreate --> strApiUrl:: " + strApiUrl);
              logger.finest("In Framework MasterFile --> FrameworkCreate --> strApiBody:: " + strApiBody);
              strResponse = "";
              strResponse = Postman.transceive(logger, strToken, "", strApiUrl, strApiBody, strChannel);
              logger.finest("In Framework MasterFile --> FrameworkCreate --> strResponse:: " + strResponse);
              JSONObject createFWresponse = (JSONObject) parser.parse(strResponse);
              JSONObject createFWparams = (JSONObject) createFWresponse.get("params");
              String strFWCreateStatus = createFWparams.get("status").toString();

              if (strFWCreateStatus.equalsIgnoreCase("successful")) {
                  JSONObject createFWresult = (JSONObject) createFWresponse.get("result");
                  String strFWNodeId = createFWresult.get("node_id").toString();
                  System.out.println("Created Framework id" + strFWNodeId);
                  File inputExcelFile = new File(strInputExcelFile);
                  FWNewExcelReader.readExcel(inputExcelFile, configFile, strChannel, strFWNodeId);
              }
          } else {
              JSONObject getFWresult = (JSONObject) getFWresponse.get("result");
              JSONObject getFWframework = (JSONObject) getFWresult.get("framework");
              Object frameworkname = (Object) getFWframework.get("name");
              String fwname = frameworkname.toString();
              if (!fwname.equalsIgnoreCase(strFrameworkName)) {
                  strApiUrl = configFile.getString("API", "api_base_url", "") + configFile.getString("API", "api_framework_update", "") + strFrameworkId;
                  logger.finest("In Framework MasterFile --> Framework Update --> strApiUrl:: " + strApiUrl);
                  strApiBody = "{\"request\": {\"framework\": {\"name\": \"" + strFrameworkName + "\", \"description\": \"" + strFrameworkDescr + "\"}}}";
                  logger.finest("In Framework MasterFile --> Framework Update --> strApiBody:: " + strApiBody);
                  strResponse = Postman.patch(logger, strToken, "", strApiUrl, strApiBody, strChannel);

                  System.out.println(strApiBody + "\n" + strResponse);
              }

              File inputExcelFile = new File(strInputExcelFile);
              FWNewExcelReader.readExcel(inputExcelFile, configFile, strChannel, strFrameworkId);

          }
      }
      catch (Exception e){
          e.printStackTrace();
          System.err.println("createFramework method --> Exception :" + e.getMessage());

      }
    }

    public void createCategory(String strFrameworkId, String strChannel, String categoryCode, String categoryName)  {
       try {
           String response = readCategory(strFrameworkId, categoryCode, categoryName, strChannel);
           if (response.equalsIgnoreCase("failed")) {
               if (categoryName == "") {
                   categoryName = categoryCode;
               }
               strApiUrl = configFile.getString("API", "api_base_url", "") + configFile.getString("API", "api_framework_category_create", "") + "=" + strFrameworkId;
               logger.finest("In Framework MasterFile --> FWCat - create --> strApiUrl:: " + strApiUrl);
               strApiBody = "{\"request\": {\"category\": {\"name\": \"" + categoryName + "\" , \"description\": \"" + categoryName + "\", \"code\": \"" + categoryCode + "\"}}}";
               logger.finest("In Framework MasterFile --> FWCat - " + categoryName + " create --> strApiBody:: " + strApiBody);
               strResponse = Postman.transceive(logger, strToken, "", strApiUrl, strApiBody, strChannel);
               logger.finest("In Framework MasterFile --> FWCat - " + categoryName + " create --> strResponse:: " + strResponse);
               JSONObject catgResponse = (JSONObject) ((JSONObject) parser.parse(strResponse)).get("params");
               String status = (String) catgResponse.get("status");
               if (status.equalsIgnoreCase("failed")) {
                   String errormsg = (String) catgResponse.get("errmsg");
                   errors.add("You tried to create the category " + categoryCode + " But got the following error :" + errormsg);
               }
           }
       }
       catch (Exception e){
           e.printStackTrace();
           System.err.println("createCategory method --> Exception :" + e.getMessage());

       }
    }

    public void createTerm(String strFrameworkId, String strChannel, String category, String term)  {
        try {
            String termResponse = readTerm(strFrameworkId, category, term);
            String catgResponse = readCategory(strFrameworkId, category);
            if (termResponse.equals("failed") && catgResponse.equalsIgnoreCase("successful")) {
                strApiUrl = configFile.getString("API", "api_base_url", "") + configFile.getString("API", "api_framework_category_term_create", "") + "=" + strFrameworkId + "&category=" + category;
                logger.finest("In Framework MasterFile --> FWCat - Board - Term create --> strApiUrl:: " + strApiUrl);
                strApiBody = "{\"request\": {\"term\": {\"name\": \"" + term + "\", \"description\": \"" + term + "\", " +
                        "\"code\":\"" + term.toLowerCase().replaceAll("\\s+", "") + "\"}}}";
                logger.finest("In Framework MasterFile --> FWCat - " + term + "- Term create --> strApiBody:: " + strApiBody);
                strResponse = Postman.transceive(logger, strToken, "", strApiUrl, strApiBody, strChannel);
                logger.finest("In Framework MasterFile --> FWCat - " + term + " - Term create --> strResponse:: " + strResponse);
                JSONObject termCreatedResponse = (JSONObject) ((JSONObject) parser.parse(strResponse)).get("params");
                String status = (String) termCreatedResponse.get("status");
                if (status.equalsIgnoreCase("failed")) {
                    String errormsg = (String) termCreatedResponse.get("errmsg");
                    errors.add("You tried to create the term " + term + " but got the following error :" + errormsg);
                }
            } else if (catgResponse.equalsIgnoreCase("failed")) {

                errors.add("You tried to create the term " + term + " but it failed due to error in category " + category);
            }
        }
        catch (Exception e){
            e.printStackTrace();
            System.err.println("createTerm method --> Exception :" + e.getMessage());

        }
    }

    public void createParentChildRelation(String strFrameworkId, String strChannel, String category, String parentTerm, ArrayList<String> childTerm)  {
       try {
           parentTerm = parentTerm.toLowerCase().replaceAll("\\s+", "");
           String parentTermIdentifier = readTerm(strFrameworkId, category, parentTerm);
           String childTermIdentifer;
           String strtermResponse;
           //    boolean flag = true;
           if (parentTermIdentifier != "" && !parentTermIdentifier.equals("failed")) {
               // Updating parent term
               strApiUrl = configFile.getString("API", "api_base_url", "") + configFile.getString("API", "api_framework_category_term_update", "") + parentTerm + "?framework=" + strFrameworkId + "&category=" + category;
               logger.finest("In Framework MasterFile --> Parent Term ->" + parentTerm + " --> strApiUrl:: " + strApiUrl);
               strApiBody = "{\"request\": {\"term\": {\"children\": [";

               for (int i = 0; i < childTerm.size(); i++) {
                   childTermIdentifer = readTerm(strFrameworkId, category, childTerm.get(i).toLowerCase().replaceAll("\\s+", ""));
                   //    flag =  validateParentChildRelation(strFrameworkId,category,parentTerm,childTermIdentifer);
                   if (!childTermIdentifer.equals("failed") && childTermIdentifer != "") {
                       strApiBody = strApiBody + "{\"identifier\": \"" + childTermIdentifer + "\"},";
                   }

               }

               strApiBody.substring(0, strApiBody.length() - 1);
               strApiBody = strApiBody + "]}}}";
               strtermResponse = Postman.patch(logger, strToken, "", strApiUrl, strApiBody, strChannel);
               logger.finest("In Framework MasterFile Parent Term ->" + parentTerm + " --> :: " + strtermResponse);

               // Updating Child Term
               for (int i = 0; i < childTerm.size(); i++) {
                   strApiUrl = configFile.getString("API", "api_base_url", "") + configFile.getString("API", "api_framework_category_term_update", "") + childTerm.get(i).toLowerCase().replaceAll("\\s+", "") + "?framework=" + strFrameworkId + "&category=" + category;
                   logger.finest("In Framework MasterFile --> child Term ->" + childTerm.get(i) + " --> strApiUrl:: " + strApiUrl);
                   strApiBody = "{\"request\": {\"term\": {\"parents\": [{\"identifier\": \"" + parentTermIdentifier + "\"}]}}}";
                   strtermResponse = Postman.patch(logger, strToken, "", strApiUrl, strApiBody, strChannel);
                   logger.finest("In Framework MasterFile child Term ->" + childTerm + " --> :: " + strtermResponse);

               }

           }
       }
       catch (Exception e){
           e.printStackTrace();
           System.err.println("createParentChildRelation method --> Exception :" + e.getMessage());

       }
    }

    public String readTerm(String strFrameworkId, String category, String Term)  {
        try {
            String strTermIdentifier = "";
            strApiUrl = configFile.getString("API", "api_base_url", "") + configFile.getString("API", "api_framework_category_term_read", "") + Term.toLowerCase().replaceAll("\\s+", "") + "?framework=" + strFrameworkId + "&category=" + category;
            logger.finest("In Framework MasterFile --> " + Term + "Term read --> strApiUrl:: " + strApiUrl);
            String strTermReadResponse = Postman.getDetails(logger, strApiUrl, strToken);
            logger.finest("In Framework MasterFile --> " + Term + " Term read --> strTermReadResponse:: " + strTermReadResponse);
            JSONObject getTermDtlsresponse = (JSONObject) parser.parse(strTermReadResponse);
            JSONObject getTermDtlsparams = (JSONObject) getTermDtlsresponse.get("params");
            String strTermDtlsGetStatus = getTermDtlsparams.get("status").toString();

            if (strTermDtlsGetStatus.equals("successful")) {
                JSONObject getTermDtlsResult = (JSONObject) getTermDtlsresponse.get("result");
                JSONObject getTermDtls = (JSONObject) getTermDtlsResult.get("term");
                strTermIdentifier = getTermDtls.get("identifier").toString();
                return strTermIdentifier;
            } else if (strTermDtlsGetStatus.equals("failed")) {
                return "failed";
            } else {
                return "";
            }
        }
        catch (Exception e){
            e.printStackTrace();
            System.err.println("readTerm method --> Exception :" + e.getMessage());
            return "";
        }
    }

    public void createAssociations(String strFrameworkId, String strChannel, String parentCategory, String parentTerm, ArrayList<Object> childTermDetails)  {
       try {
           String strtermResponse = "";
           String childTermIdentifier;
           String catg;
           String term;
           boolean flag = false;
           ArrayList<String> catgList = new ArrayList<String>();
           ArrayList<String> termList = new ArrayList<String>();

           strApiBody = "{\"request\": {\"term\": {\"associations\": [";

           for (int i = 0; i < childTermDetails.size(); i++) {
               jsonObject = (JSONObject) childTermDetails.get(i);
               catgList = (ArrayList<String>) jsonObject.get("categories");
               termList = (ArrayList<String>) jsonObject.get("terms");
               for (int j = 0; j < catgList.size(); j++) {
                   catg = catgList.get(j);
                   term = termList.get(j);
                   childTermIdentifier = readTerm(strFrameworkId, catg, term.toLowerCase().replaceAll("\\s+", ""));
                   if (!childTermIdentifier.equals("failed") && childTermIdentifier != "") {
                       flag = true;
                       strApiBody = strApiBody + "{\"identifier\": \"" + childTermIdentifier + "\"},";
                   }
               }
               // catg = (String)(jsonObject.get("category"));
               // term = (String)jsonObject.get("term");
           /* childTermIdentifier=readTerm(strFrameworkId,catg,term.toLowerCase().replaceAll("\\s+",""));
            if(!childTermIdentifier.equals("failed") && childTermIdentifier != ""){
                strApiBody = strApiBody + "{\"identifier\": \"" + childTermIdentifier+"\"},";
            }*/

           }
           if (flag) {
               String strParentBody = checkParent(strFrameworkId, parentCategory, parentTerm);

               strApiBody = strApiBody.substring(0, strApiBody.length() - 1);
               strApiBody = strApiBody + "]";
               if (!strParentBody.equalsIgnoreCase("failed")) {
                   strApiBody = strApiBody + "," + strParentBody;
               }
               strApiBody = strApiBody + "}}}";
               System.out.println("Associations request body\n" + strApiBody);
               strApiUrl = configFile.getString("API", "api_base_url", "") + configFile.getString("API", "api_framework_category_term_update", "") + parentTerm.toLowerCase().replaceAll("\\s+", "") + "?framework=" + strFrameworkId + "&category=" + parentCategory;
               strtermResponse = Postman.patch(logger, strToken, "", strApiUrl, strApiBody, strChannel);
               logger.finest("In Framework MasterFile Associations Term ->" + parentTerm + " --> :: " + strtermResponse);
           }
       }
       catch (Exception e){
           e.printStackTrace();
           System.err.println("createAssociations method --> Exception :" + e.getMessage());

       }
    }

    public String readCategory(String strFrameworkId, String strCatCode)  {
       try {
           strApiUrl = configFile.getString("API", "api_base_url", "") + configFile.getString("API", "api_framework_category_read", "") + strCatCode + "?framework=" + strFrameworkId;
           logger.finest("In Framework MasterFile --> FrameworkCategoryGetDetails --> strApiUrl:: " + strApiUrl);
           strResponse = Postman.getDetails(logger, strApiUrl, strToken);
           logger.finest("In Framework MasterFile --> FrameworkCategoryGetDetails --> strResponse:: " + strResponse);
           JSONObject getFWCatresponse = (JSONObject) parser.parse(strResponse);
           JSONObject getFWcatparams = (JSONObject) getFWCatresponse.get("params");
           String strFWCatGetStatus = getFWcatparams.get("status").toString();

           if (strFWCatGetStatus.equalsIgnoreCase("successful")) {
               return "successful";
           }
           return "failed";
       }
       catch (Exception e){
           e.printStackTrace();
           System.err.println("readCategory method --> Exception :" + e.getMessage());
           return  "failed";
       }
    }

    public String readCategory(String strFrameworkId, String strCatCode, String strCatName, String strChannel)  {
        try {
            strApiUrl = configFile.getString("API", "api_base_url", "") + configFile.getString("API", "api_framework_category_read", "") + strCatCode + "?framework=" + strFrameworkId;
            logger.finest("In Framework MasterFile --> FrameworkCategoryGetDetails --> strApiUrl:: " + strApiUrl);
            strResponse = Postman.getDetails(logger, strApiUrl, strToken);
            logger.finest("In Framework MasterFile --> FrameworkCategoryGetDetails --> strResponse:: " + strResponse);
            JSONObject getFWCatresponse = (JSONObject) parser.parse(strResponse);
            JSONObject getFWcatparams = (JSONObject) getFWCatresponse.get("params");
            String strFWCatGetStatus = getFWcatparams.get("status").toString();

            if (strFWCatGetStatus.equalsIgnoreCase("successful")) {
                String nm = (((JSONObject) ((JSONObject) getFWCatresponse.get("result")).get("category")).get("name")).toString();
                if (!nm.equals(strCatName) || strCatName != null) {

                    strApiUrl = configFile.getString("API", "api_base_url", "") + configFile.getString("API", "api_framework_category_update", "") + strCatCode + "?framework=" + strFrameworkId;
                    logger.finest("In Framework MasterFile --> category  ->" + strCatCode + " --> strApiUrl:: " + strApiUrl);
                    strApiBody = "{\"request\": {\"category\": {\"name\":  \"" + strCatName + "\"}}}";
                    strResponse = Postman.patch(logger, strToken, "", strApiUrl, strApiBody, strChannel);
                    logger.finest("In Framework MasterFile category ->" + strCatCode + " --> :: " + strResponse);

                }
                return "successful";
            }
            return "failed";
        }
        catch (Exception e){
            e.printStackTrace();
            System.err.println("readCategory method --> Exception :" + e.getMessage());
            return "failed";
        }
    }

    public String publishFramework(String strFrameworkId, String strChannel)  {
       try {
           strApiUrl = configFile.getString("API", "api_base_url", "") + configFile.getString("API", "api_framework_publish", "") + strFrameworkId;
           logger.finest("Publishing Framework" + strFrameworkId + " " + strApiUrl);
           strApiBody = "{}";
           strResponse = Postman.transceive(logger, strToken, "", strApiUrl, strApiBody, strChannel);
           JSONObject fwPublishResponse = (JSONObject) ((JSONObject) parser.parse(strResponse)).get("params");
           String status = (String) fwPublishResponse.get("status");
           if (status.equalsIgnoreCase("successful")) {
               return "successful";
           } else {
               errors.add("You tried to published the framework " + strFrameworkId + "but got the following error : " + (String) fwPublishResponse.get("errmsg"));
               return "failed";
           }
       }
       catch (Exception e){
           e.printStackTrace();
           System.err.println("publishFramework method --> Exception :" + e.getMessage());
           return "failed";

       }
    }

    public void errorList() {
        System.out.println("Error list  :");
        for (int k = 0; k < FWNewExcelReader.errorList.size(); k++) {
            System.out.println(FWNewExcelReader.errorList.get(k));
        }
        for (int k = 0; k < errors.size(); k++) {
            System.out.println(errors.get(k));
        }
    }
    public String readFramework(String strFrameworkId){
       try {
            strApiUrl = configFile.getString("API", "api_base_url", "") + configFile.getString("API", "api_framework_read", "") + strFrameworkId;
           logger.finest("In Framework MasterFile --> FrameworkGetDetails --> strApiUrl:: " + strApiUrl);
            strResponse = Postman.getDetails(logger, strApiUrl, strToken);
           logger.finest("In Framework MasterFile --> FrameworkGetDetails --> strResponse:: " + strResponse);
            getFWresponse = (JSONObject) parser.parse(strResponse);
           JSONObject getFWparams = (JSONObject) getFWresponse.get("params");
           String strFWGetStatus = getFWparams.get("status").toString();
           return  strFWGetStatus;
       }
       catch (Exception e){
           e.printStackTrace();
           return "";
       }
    }
    public String checkParent(String strFrameworkId, String category, String Term){
        try {
            strApiUrl = configFile.getString("API", "api_base_url", "") + configFile.getString("API", "api_framework_category_term_read", "") + Term.toLowerCase().replaceAll("\\s+", "") + "?framework=" + strFrameworkId + "&category=" + category;
            logger.finest("In Framework MasterFile --> " + Term + "Term read --> strApiUrl:: " + strApiUrl);
            String strTermReadResponse = Postman.getDetails(logger, strApiUrl, strToken);
            logger.finest("In Framework MasterFile --> " + Term + " Term read --> strTermReadResponse:: " + strTermReadResponse);
            JSONObject getTermDtlsresponse = (JSONObject) parser.parse(strTermReadResponse);
                JSONObject getTermDtlsResult = (JSONObject) getTermDtlsresponse.get("result");
                JSONObject getTermDtls = (JSONObject) getTermDtlsResult.get("term");
                JSONArray parents = (JSONArray) getTermDtls.get("parents");
                if(parents.size() > 0){
                    JSONObject parentObj = (JSONObject)parents.get(0);
                    String id = parentObj.get("identifier").toString();
                    String parentReq = "\"parents\" : [{\"identifier\": \"" + id +"\" }]";
                    return parentReq;
                }
            //    return strTermIdentifier;
            else {
                return "failed";
            }
        }
        catch (Exception e){
            e.printStackTrace();
            System.err.println("readTerm method --> Exception :" + e.getMessage());
           return "failed";
        }
    }
}
