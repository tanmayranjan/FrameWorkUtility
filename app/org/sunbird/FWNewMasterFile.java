
package org.sunbird;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class FWNewMasterFile {
    IniFile configFile = null;
    static String termCode="";
    Logger logger = null;
    String strToken = null, strApiUrl, strApiBody, strResponse = "";
    JSONParser parser = new JSONParser();
    Object obj;
    JSONObject jsonObject;
    static ArrayList<String> errors = new ArrayList<String>();
    static JSONObject getFWresponse;
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

    public String createFramework(File inputExcelFile,String strFileExtn, String strFrameworkName, String strFrameworkId, String strFrameworkDescr, String strChannel,int option) {
        try {
            JSONParser parser = new JSONParser();

            // Check for correct extension
            if(!(strFileExtn.equalsIgnoreCase("xlsx") || strFileExtn.equalsIgnoreCase("xls")))
            {
                System.out.println("Incorrect file format");
                System.exit(0);
            }
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
                if(option == 2){
                    System.out.println("Framework not found");
                    System.exit(0);

                }
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
                   // File inputExcelFile = new File(strInputExcelFile);
                  String fwresponse =  FWNewExcelReaderWriter.readExcel(inputExcelFile, configFile, strChannel, strFWNodeId);
                  return fwresponse;
                }
            }
            else {
                if(option == 1){
                    System.out.println("Framework Already Exists");
                    System.exit(0);

                }
                JSONObject getFWresult = (JSONObject) getFWresponse.get("result");
                JSONObject getFWframework = (JSONObject) getFWresult.get("framework");
                Object frameworkname = (Object) getFWframework.get("name");
                String fwname = frameworkname.toString();
                if (!fwname.equalsIgnoreCase(strFrameworkName)) {
                    strApiUrl = configFile.getString("API", "api_base_url", "") + configFile.getString("API", "api_framework_update", "") + strFrameworkId;
                    logger.finest("In Framework MasterFile --> Framework Create --> strApiUrl:: " + strApiUrl);
                    strApiBody = "{\"request\": {\"framework\": {\"name\": \"" + strFrameworkName + "\", \"description\": \"" + strFrameworkDescr + "\"}}}";
                    logger.finest("In Framework MasterFile --> Framework Create --> strApiBody:: " + strApiBody);
                    strResponse = Postman.patch(logger, strToken, "", strApiUrl, strApiBody, strChannel);

                    System.out.println(strApiBody + "\n" + strResponse);
                }

            //    File inputExcelFile = new File(strInputExcelFile);
              String fwresponse =  FWNewExcelReaderWriter.readExcel(inputExcelFile, configFile, strChannel, strFrameworkId);
              return fwresponse;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            System.err.println("createFramework method --> Exception :" + e.getMessage());
            return "failed";
        }
        return "ok";
    }

    public void createCategory(String strFrameworkId, String strChannel, String categoryCode, String categoryName) {
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
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("createCategory method --> Exception :" + e.getMessage());

        }
    }

    public String createTerm(String strFrameworkId, String strChannel, String category, String term,String termType) {
        try {
            String translationvalue = null;
            String termResponse = "";
            if(termCode != ""){
                termResponse = readTerm(strFrameworkId, category, termCode,1);
                if(termResponse.equalsIgnoreCase("successful")){
                    return "AlreadyCreated_";
                }
            }
            if (term != null && term != "" && termCode == "") {
               // termResponse = readTerm(strFrameworkId, category, term);
                termCode = generateGUID();
                termCode = termCode.toLowerCase();
            }
            String catgResponse = readCategory(strFrameworkId, category);
            if (!termCode.equals("") && catgResponse.equalsIgnoreCase("successful")) {
                strApiUrl = configFile.getString("API", "api_base_url", "") + configFile.getString("API", "api_framework_category_term_create", "") + "=" + strFrameworkId + "&category=" + category;
                logger.finest("In Framework MasterFile -->  Term create --> strApiUrl:: " + strApiUrl);
                strApiBody = "{\"request\": {\"term\": {\"name\": \"" + term + "\",\"translations\": null, \"description\": \"" + term + "\", " +
                        "\"code\":\"" + termCode + "\"}}}";
                logger.finest("In Framework MasterFile --> Term create - " + term + "- Term create --> strApiBody:: " + strApiBody);
                strResponse = Postman.transceive(logger, strToken, "", strApiUrl, strApiBody, strChannel);
                logger.finest("In Framework MasterFile --> Term create - " + term + " - Term create --> strResponse:: " + strResponse);
                JSONObject termCreatedResponse = (JSONObject) ((JSONObject) parser.parse(strResponse)).get("params");
                String status = (String) termCreatedResponse.get("status");
                if (status.equalsIgnoreCase("failed")) {
                    String errormsg = (String) termCreatedResponse.get("errmsg");
                    errors.add("You tried to create the term " + term + " but got the following error :" + errormsg);
                    return "failed";
                }
                return "successful"+"_"+termCode;
            }
            else if (catgResponse.equalsIgnoreCase("failed")) {

                errors.add("You tried to create the term " + term + " but it failed due to error in category " + category);
                return "failed";
            }
            else if(termCode != "" && termType.equalsIgnoreCase("child")) {
              String parentResponse =  checkParent(strFrameworkId,category,termCode);
              if(parentResponse != "failed"){
                  errors.add("The term " + term + " already has a parent , please add a uniqure character to this term and then try again");
                  return "failed";
              }
            }

            return "successful_"+termCode;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("createTerm method --> Exception :" + e.getMessage());
            return "failed";
        }
    }

    public void createParentChildRelation(String strFrameworkId, String strChannel, String category, String parentTerm, ArrayList<String> childTerm) {
        try {
            String parentTermIdentifier = "";
            if (parentTerm != null) {
              //  parentTerm = parentTerm.toLowerCase().replaceAll("\\s+", "");
                parentTermIdentifier = readTerm(strFrameworkId, category, parentTerm,2);
            }
            String childTermIdentifer = "";
            String strtermResponse;
            //    boolean flag = true;
            if (parentTermIdentifier != "" && !parentTermIdentifier.equals("failed")) {
                // Updating parent term
                strApiUrl = configFile.getString("API", "api_base_url", "") + configFile.getString("API", "api_framework_category_term_update", "") + parentTerm + "?framework=" + strFrameworkId + "&category=" + category;
                logger.finest("In Framework MasterFile --> Parent Term Relationship->" + parentTerm + " --> strApiUrl:: " + strApiUrl);
                strApiBody = "{\"request\": {\"term\": {\"children\": [";

                for (int i = 0; i < childTerm.size(); i++) {
                    if (childTerm.get(i) != null) {
                        String childTermCode = (childTerm.get(i)).toString();
                    //    childTermCode = childTermCode.toLowerCase().replaceAll("\\s+", "");
                        childTermIdentifer = readTerm(strFrameworkId, category, childTermCode,2);
                    }
                    //    flag =  validateParentChildRelation(strFrameworkId,category,parentTerm,childTermIdentifer);
                    if (!childTermIdentifer.equals("failed") && childTermIdentifer != "") {
                        strApiBody = strApiBody + "{\"identifier\": \"" + childTermIdentifer + "\"},";
                    }

                }

                strApiBody.substring(0, strApiBody.length() - 1);
                strApiBody = strApiBody + "]}}}";
                strtermResponse = Postman.patch(logger, strToken, "", strApiUrl, strApiBody, strChannel);
                logger.finest("In Framework MasterFile Parent Term Relation->" + parentTerm + " --> :: " + strtermResponse);

                // Updating Child Term
                    for (int i = 0; i < childTerm.size(); i++) {
                        strApiUrl = configFile.getString("API", "api_base_url", "") + configFile.getString("API", "api_framework_category_term_update", "") + childTerm.get(i) + "?framework=" + strFrameworkId + "&category=" + category;
                        logger.finest("In Framework MasterFile --> child Term relation->" + childTerm.get(i) + " --> strApiUrl:: " + strApiUrl);
                        strApiBody = "{\"request\": {\"term\": {\"parents\": [{\"identifier\": \"" + parentTermIdentifier + "\"}]}}}";
                        strtermResponse = Postman.patch(logger, strToken, "", strApiUrl, strApiBody, strChannel);
                        logger.finest("In Framework MasterFile child Term relation->" + childTerm + " --> :: " + strtermResponse);

                    }


            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("createParentChildRelation method --> Exception :" + e.getMessage());

        }
    }

    public String readTerm(String strFrameworkId, String category, String Term,int action) {
        try {
            String strTermIdentifier = "";
            strApiUrl = configFile.getString("API", "api_base_url", "") + configFile.getString("API", "api_framework_category_term_read", "") + Term + "?framework=" + strFrameworkId + "&category=" + category;
            logger.finest("In Framework MasterFile read term--> " + Term + "Term read --> strApiUrl:: " + strApiUrl);
            String strTermReadResponse = Postman.getDetails(logger, strApiUrl, strToken);
            logger.finest("In Framework MasterFile read term--> " + Term + " Term read --> strTermReadResponse:: " + strTermReadResponse);
            JSONObject getTermDtlsresponse = (JSONObject) parser.parse(strTermReadResponse);
            JSONObject getTermDtlsparams = (JSONObject) getTermDtlsresponse.get("params");
            String strTermDtlsGetStatus = getTermDtlsparams.get("status").toString();

            if (strTermDtlsGetStatus.equals("successful")) {
                if(action == 1 ){
                    return "successful";
                }
                JSONObject getTermDtlsResult = (JSONObject) getTermDtlsresponse.get("result");
                JSONObject getTermDtls = (JSONObject) getTermDtlsResult.get("term");
                strTermIdentifier = getTermDtls.get("identifier").toString();
                return strTermIdentifier;

            } else if (strTermDtlsGetStatus.equals("failed")) {
                return "failed";
            } else {
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("readTerm method --> Exception :" + e.getMessage());
            return "";
        }
    }
    public JSONObject readTerm(String strFrameworkId, String category, String Term, String action) {
        try {
            String strTermIdentifier = "";
            JSONObject termDetails = new JSONObject();
            strApiUrl = configFile.getString("API", "api_base_url", "") + configFile.getString("API", "api_framework_category_term_read", "") + Term + "?framework=" + strFrameworkId + "&category=" + category;
            logger.finest("In Framework MasterFile read term--> " + Term + "Term read --> strApiUrl:: " + strApiUrl);
            String strTermReadResponse = Postman.getDetails(logger, strApiUrl, strToken);
            logger.finest("In Framework MasterFile read term--> " + Term + " Term read --> strTermReadResponse:: " + strTermReadResponse);
            JSONObject getTermDtlsresponse = (JSONObject) parser.parse(strTermReadResponse);
            JSONObject getTermDtlsparams = (JSONObject) getTermDtlsresponse.get("params");
            String strTermDtlsGetStatus = getTermDtlsparams.get("status").toString();

            if (strTermDtlsGetStatus.equals("successful")) {
                JSONObject getTermDtlsResult = (JSONObject) getTermDtlsresponse.get("result");
                JSONObject getTermDtls = (JSONObject) getTermDtlsResult.get("term");
                if(action.equalsIgnoreCase("termdetails")){
                     termDetails.put("children",(JSONArray)getTermDtls.get("children"));
                    termDetails.put("associations",(JSONArray)getTermDtls.get("associations"));

                }
                if(action.equalsIgnoreCase("readchildterm")){
                    return getTermDtls;
                }
                return termDetails;
            } else if (strTermDtlsGetStatus.equals("failed")) {
                return null;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("readTerm method --> Exception :" + e.getMessage());
            return null;
        }
    }

    public void createAssociations(String strFrameworkId, String strChannel, String parentCategory, String parentTerm, ArrayList<Object> childTermDetails) {
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
                    childTermIdentifier = readTerm(strFrameworkId, catg, term,2);
                    if (!childTermIdentifier.equals("failed") && childTermIdentifier != "") {
                        flag = true;
                        strApiBody = strApiBody + "{\"identifier\": \"" + childTermIdentifier + "\"},";
                    }
                }


            }
            if (flag) {
                String strParentBody = checkParent(strFrameworkId, parentCategory, parentTerm);

                strApiBody = strApiBody.substring(0, strApiBody.length() - 1);
                strApiBody = strApiBody + "]";
                if (!strParentBody.equalsIgnoreCase("failed")) {
                    strApiBody = strApiBody + "," + strParentBody;
                }
                strApiBody = strApiBody + "}}}";
             //   System.out.println("Associations request body\n" + strApiBody);
                strApiUrl = configFile.getString("API", "api_base_url", "") + configFile.getString("API", "api_framework_category_term_update", "") + parentTerm + "?framework=" + strFrameworkId + "&category=" + parentCategory;
                strtermResponse = Postman.patch(logger, strToken, "", strApiUrl, strApiBody, strChannel);
                logger.finest("In Framework MasterFile Associations Term ->" + parentTerm + " --> :: " + strtermResponse);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("createAssociations method --> Exception :" + e.getMessage());

        }
    }

    public String readCategory(String strFrameworkId, String strCatCode) {
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
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("readCategory method --> Exception :" + e.getMessage());
            return "failed";
        }
    }
    public JSONArray readCategory(String strFrameworkId, String strCatCode,String action) {
        try {
            strApiUrl = configFile.getString("API", "api_base_url", "") + configFile.getString("API", "api_framework_category_read", "") + strCatCode + "?framework=" + strFrameworkId;
            logger.finest("In Framework MasterFile --> FrameworkCategoryGetDetails --> strApiUrl:: " + strApiUrl);
            strResponse = Postman.getDetails(logger, strApiUrl, strToken);
            logger.finest("In Framework MasterFile --> FrameworkCategoryGetDetails --> strResponse:: " + strResponse);
            JSONObject getFWCatresponse = (JSONObject) parser.parse(strResponse);
            JSONObject getFWcatparams = (JSONObject) getFWCatresponse.get("params");
            String strFWCatGetStatus = getFWcatparams.get("status").toString();

            if (strFWCatGetStatus.equalsIgnoreCase("successful")) {
                JSONArray getTermList = (JSONArray)((JSONObject) (((JSONObject) getFWCatresponse.get("result")).get("category"))).get("terms");
                 return  getTermList;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("readCategory method --> Exception :" + e.getMessage());
            return null;
        }
    }

    public String readCategory(String strFrameworkId, String strCatCode, String strCatName, String strChannel) {
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
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("readCategory method --> Exception :" + e.getMessage());
            return "failed";
        }
    }

    public String publishFramework(String strFrameworkId, String strChannel) {
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
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("publishFramework method --> Exception :" + e.getMessage());
            return "failed";

        }
    }

    public void errorList() {
        if(errors.size() > 0){
        System.out.println("Error list  :");
        /*for (int k = 0; k < FWNewExcelReader.errorList.size(); k++) {
            System.out.println(FWNewExcelReader.errorList.get(k));
        }*/
        for (int k = 0; k < errors.size(); k++) {
            System.out.println(errors.get(k));
        }
     }
    }

    public String readFramework(String strFrameworkId) {
        try {
            strApiUrl = configFile.getString("API", "api_base_url", "") + configFile.getString("API", "api_framework_read", "") + strFrameworkId;
            logger.finest("In Framework MasterFile --> FrameworkGetDetails --> strApiUrl:: " + strApiUrl);
            strResponse = Postman.getDetails(logger, strApiUrl, strToken);
            logger.finest("In Framework MasterFile --> FrameworkGetDetails --> strResponse:: " + strResponse);
            getFWresponse = (JSONObject) parser.parse(strResponse);
            JSONObject getFWparams = (JSONObject) getFWresponse.get("params");
            String strFWGetStatus = getFWparams.get("status").toString();
            return strFWGetStatus;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("In read framework method");
            return "";
        }
    }
    public JSONArray readFramework(String strFrameworkId,String action) {
        try {
                strApiUrl = configFile.getString("API", "api_base_url", "") + configFile.getString("API", "api_framework_read", "") + strFrameworkId;
                logger.finest("In Framework MasterFile --> FrameworkGetDetails --> strApiUrl:: " + strApiUrl);
                strResponse = Postman.getDetails(logger, strApiUrl, strToken);
                logger.finest("In Framework MasterFile --> FrameworkGetDetails --> strResponse:: " + strResponse);
                getFWresponse = (JSONObject) parser.parse(strResponse);
                JSONObject getFW = (JSONObject) ((JSONObject) getFWresponse.get("result")).get("framework");
                JSONArray categoryarray = (JSONArray) getFW.get("categories");
                return categoryarray;

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("In read framework method");
            return null;
        }
    }

    public String checkParent(String strFrameworkId, String category, String Term) {
        try {
            JSONArray parents = new JSONArray();
            strApiUrl = configFile.getString("API", "api_base_url", "") + configFile.getString("API", "api_framework_category_term_read", "") + Term + "?framework=" + strFrameworkId + "&category=" + category;
            logger.finest("In Framework MasterFile --> " + Term + "Term read --> strApiUrl:: " + strApiUrl);
            String strTermReadResponse = Postman.getDetails(logger, strApiUrl, strToken);
            logger.finest("In Framework MasterFile --> " + Term + " Term read --> strTermReadResponse:: " + strTermReadResponse);
            JSONObject getTermDtlsresponse = (JSONObject) parser.parse(strTermReadResponse);
            JSONObject getTermDtlsResult = (JSONObject) getTermDtlsresponse.get("result");
            JSONObject getTermDtls = (JSONObject) getTermDtlsResult.get("term");
            parents = (JSONArray) getTermDtls.get("parents");
            if (parents != null && parents.size() > 0) {
                JSONObject parentObj = (JSONObject) parents.get(0);
                String id = parentObj.get("identifier").toString();
                String parentReq = "\"parents\" : [{\"identifier\": \"" + id + "\" }]";
                return parentReq;
            }
            //    return strTermIdentifier;
            else {
                return "failed";
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("checkparent method --> Exception :" + e.getMessage());
            return "failed";
        }
    }
    public String generateGUID(){
        int n=40;
        byte[] array = new byte[256];
        new Random().nextBytes(array);

        String randomString
                = new String(array, Charset.forName("UTF-8"));

        // Create a StringBuffer to store the result
        StringBuffer r = new StringBuffer();

        // remove all spacial char
        String  AlphaNumericString
                = randomString
                .replaceAll("[^A-Za-z0-9]", "");


        // from the generated random String into the result
        for (int k = 0; k < AlphaNumericString.length(); k++) {

            if (Character.isLetter(AlphaNumericString.charAt(k))
                    && (n > 0)
                    || Character.isDigit(AlphaNumericString.charAt(k))
                    && (n > 0)) {

                r.append(AlphaNumericString.charAt(k));
                n--;
            }
        }

        // return the resultant string
        return r.toString();
    }
}
