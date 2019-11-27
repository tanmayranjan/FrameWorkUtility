package org.sunbird;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class FWCreateExcel {
    static int rownumber = 1,cellnumber = 3,cellnumberForAssociation=3,counter = 1;
    int flag =1 ;
    ArrayList<Object> termList = new ArrayList<Object>();
    String termIdentifier,termName,termCode[],associatedTermCode,associatedTermIdentifier,associatedTermName,tempTermCode="",childtermIdentifier,childtermName,childtermCode[];
    String categoryCode,categoryName,categoryIdentifier,categoryResponse,associatedCategory;
    Path currentRelativePath = Paths.get("");
    String strFilePath = currentRelativePath.toAbsolutePath().toString();
    IniFile loadConfig = new IniFile(strFilePath + "/configLive.ini");
    FWNewMasterFile masterObj = new FWNewMasterFile(loadConfig);
    XSSFWorkbook workbook = new XSSFWorkbook();
    JSONArray allCategoriesArray = new JSONArray();
    JSONArray codesOfTerm = new JSONArray();
    Map<String, Object[]> data = new TreeMap<String, Object[]>();
    Map<String, Object[]> childData = new TreeMap<String, Object[]>();
    Map<String, Object[]> associationData = new TreeMap<String, Object[]>();

    // Create association sheet
    XSSFSheet sheet2 = workbook.createSheet("Associations");
    FWCreateExcel() throws  Exception{
    }
     public void createExcel(String strFrameworkId) throws Exception{
        // Create Categories sheet
        XSSFSheet sheet1 = workbook.createSheet("Categories");
        associationData.put("1",new Object[]{"Parent Category Code","Parent Term Name","Parent Term Code","Associated Category Code","Associated Term Name","Associated Term Code"});
       createRowsAndColumns(associationData,sheet2,"SheetHeading");
        associationData.clear();
        JSONObject categoryObj;
        String strFwStatus = masterObj.readFramework(strFrameworkId);
        if(strFwStatus.equalsIgnoreCase("successful")){
            // List of Categories
           JSONArray categoryList = masterObj.readFramework(strFrameworkId,"createExcel");

            data.put("1", new Object[] {"Category Name", "Category Code"});
           for(int i = 0 ; i < categoryList.size() ; i++)
           {
                categoryObj = (JSONObject) categoryList.get(i);
                categoryIdentifier = ((categoryObj.get("identifier")).toString());
                categoryName = ((categoryObj.get("name")).toString());
                categoryCode = categoryIdentifier.substring(categoryIdentifier.indexOf('_')+ 1);
               data.put(""+(i+2),new Object[]{categoryName,categoryCode});
               JSONObject allCategories = new JSONObject();
               allCategories.put("categoryCode",categoryCode);
               allCategories.put("sheet", workbook.createSheet(categoryName));
               allCategoriesArray.add(allCategories);
           }
           // Writing categories detail in its sheet
            createRowsAndColumns(data,sheet1,"categories");
            data.clear();
          for(int i = 0 ; i < allCategoriesArray.size() ; i++ ){
                  categoryCode = (((JSONObject)(allCategoriesArray.get(i))).get("categoryCode")).toString();
                 categoryResponse = masterObj.readCategory(strFrameworkId,categoryCode);
                 if(categoryResponse.equalsIgnoreCase("successful")){
                     // Getting terms of the category selected
                    termList = masterObj.readCategory(strFrameworkId,categoryCode,"createExcel");
                     data.put("1", new Object[]{"Category Code","Parent Term Name","Parent Term Code","Child Term-1 Name","Child Term-1 Code"});
                    // Creating heading for the sheet
                     createRowsAndColumns(data,workbook.getSheetAt(i+2),"SheetHeading");
                     data.clear();
                     for(int j = 0 ; j < termList.size() ; j++){
                         flag = 1;
                         flag = writeParent(strFrameworkId,(JSONObject) termList.get(j),j,i);
                         checkChild(strFrameworkId, j, i, termCode[2]);
                         if(flag == 1) {
                             rownumber++;
                         }

                    }
                     rownumber = 1;
   }
              }
           // Writing the excel
           FileOutputStream out = new FileOutputStream(new File("demo.xlsx"));
            workbook.write(out);
            out.close();
        }
    }
    // Assigning the cells of excel with values
    public void createRowsAndColumns(Map<String, Object[]> data,XSSFSheet sheet,String action){
       try {
           Set<String> keyset = data.keySet();
           int rownum, cellnum;
           if (action.equalsIgnoreCase("categories") || action.equalsIgnoreCase("SheetHeading")) {
               rownum = 0;

           } else if (action.equalsIgnoreCase("associations") || action.equalsIgnoreCase("ParentAssociation") || action.equalsIgnoreCase("createAssociation")) {
               rownum = counter;
           } else {
               rownum = rownumber;
           }
           for (String key : keyset) {

               Row row = sheet.getRow(rownum);
               if (row == null) {
                   row = sheet.createRow(rownum);
               }

               Object[] objArr = data.get(key);
               if (action.equalsIgnoreCase("categories") || action.equalsIgnoreCase("parentTerms") || action.equalsIgnoreCase("SheetHeading") || action.equalsIgnoreCase("ParentAssociation")) {
                   cellnum = 0;
                   rownum++;

               } else if (action.equalsIgnoreCase("createAssociation")) {
                   cellnum = cellnumberForAssociation;
               } else {
                   cellnum = cellnumber;
               }
               for (Object obj : objArr) {
                   Cell cell = row.createCell(cellnum++);
                   if (obj instanceof String) {
                       /*if (Charset.defaultCharset().toString().equalsIgnoreCase("windows-1252")) {
                           byte[] d = ((String)obj).getBytes("windows-1252");
                           obj = new String(d, Charset.forName("UTF-8"));
                       }*/
                       cell.setCellValue((String) obj);

                   } else if (obj instanceof Integer)
                       cell.setCellValue((Integer) obj);

                   if (key.equals("1")) {
                       CellStyle c = workbook.createCellStyle();
                       XSSFFont f = workbook.createFont();
                       f.setBold(true);
                       c.setFont(f);
                       cell = row.getCell(cellnum - 1);
                       cell.setCellStyle(c);
                   }

               }
           }
       }
       catch (Exception e){

       }
    }
    // Check for parent terms and write it in the excel
    public int writeParent(String strFrameworkId,JSONObject termObj,int j,int i)throws Exception{
        termName=(String)((termObj).get("name"));
        termIdentifier=((JSONObject)termList.get(j)).get("identifier").toString();
        termCode = termIdentifier.split("_");
        JSONObject termdetails = getTermDetails(strFrameworkId,termCode[2]);
        JSONArray children = (JSONArray)termdetails.get("children");
        JSONArray parents = (JSONArray) termObj.get("parents");
        if(j == 0){
            data.put(""+(j+2),new Object[]{categoryCode,termName,termCode[2]});
        }
        else{
            data.put(""+(j+2),new Object[]{"",termName,termCode[2]});

        }
         // First Level Parent term
        if((children == null || children.size() == 0) && (parents == null || parents.size() == 0) ){
            createRowsAndColumns(data,workbook.getSheetAt(i+2),"parentTerms");

        }
        // Parent term which is also a child
        else if(children != null && children.size() > 0){
            createRowsAndColumns(data,workbook.getSheetAt(i+2),"parentTerms");

        }
        // Not a parent term
        else{
            data.clear();
            return 2;
        }

        data.clear();
        return 1;
    }
    // Writing subterms
    public void writeChild(JSONArray children,int j,int i,String strFrameworkId) {
        try {

        for (int k = 0; k < children.size(); k++) {
            childtermName = ((JSONObject) children.get(k)).get("name").toString();
            childtermIdentifier = ((JSONObject) children.get(k)).get("identifier").toString();
            childtermCode = childtermIdentifier.split("_");
            childData.put("" + (j + 2), new Object[]{childtermName, childtermCode[2]});
            createRowsAndColumns(childData, workbook.getSheetAt(i + 2), "childterms");
            cellnumber = cellnumber + 2;
            childData.clear();
            JSONObject temp = masterObj.readTerm(strFrameworkId, categoryCode, childtermCode[2], "readChildTerm");

            termList.add(j + 1, temp);


        }

        cellnumber = 3;
        childData.clear();
    }
    catch(Exception e){
        }
    }
    // Returns the term details
    public JSONObject getTermDetails(String strFrameworkId,String termCode){
        JSONObject termdetails = masterObj.readTerm(strFrameworkId,categoryCode,termCode,"termdetails");
           return termdetails;
    }
    // Checking for subterms
    public void checkChild(String strFrameworkId,int j,int i,String termCode){

        JSONObject termdetails = getTermDetails(strFrameworkId,termCode);
        JSONArray children = (JSONArray)termdetails.get("children");
        checkAssociation(termdetails);
        if(children != null && children.size() > 0) {
            writeChild(children,j,i,strFrameworkId);
    }
}
    // Checking for associations
    public void checkAssociation(JSONObject termDetails){
        JSONArray association = (JSONArray)termDetails.get("associations");
        if(association != null && association.size() > 0 && tempTermCode != termCode[2]) {
            associationData.put(""+(counter+1),new Object[]{categoryCode,termName,termCode[2]});
            createRowsAndColumns(associationData,sheet2,"ParentAssociation");
            associationData.clear();
            writeAssociation(association);
            tempTermCode = termCode[2];
        }
    }
    // Writing associations
    public void writeAssociation(JSONArray association){
          for(int k=0; k < association.size(); k++){
             associatedTermIdentifier = (((JSONObject)association.get(k)).get("identifier")).toString();
             associatedCategory = associatedTermIdentifier.split("_")[1];
             associatedTermCode = associatedTermIdentifier.split("_")[2];
              associatedTermName = (((JSONObject)association.get(k)).get("name")).toString();
              associationData.put(""+(counter+1),new Object[]{associatedCategory,associatedTermName,associatedTermCode});
              createRowsAndColumns(associationData,sheet2,"createAssociation");
              associationData.clear();
              counter++;
              cellnumberForAssociation =3;
          }
    }

}
