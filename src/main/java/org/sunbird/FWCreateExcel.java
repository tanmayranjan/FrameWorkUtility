package org.sunbird;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class FWCreateExcel {
    static int rownumber = 1;
    static  int cellnumber = 3;
    JSONArray termList = new JSONArray();
    String termIdentifier,termName,termCode[];
    String categoryCode,categoryName,categoryIdentifier,categoryResponse,childtermIdentifier,childtermName,childtermCode[];
    Path currentRelativePath = Paths.get("");
    String strFilePath = currentRelativePath.toAbsolutePath().toString();
    IniFile loadConfig = new IniFile(strFilePath + "/configLive.ini");
    FWNewMasterFile masterObj = new FWNewMasterFile(loadConfig);
    XSSFWorkbook workbook = new XSSFWorkbook();
    JSONArray allCategoriesArray = new JSONArray();
    Map<String, Object[]> data = new TreeMap<String, Object[]>();
    Map<String, Object[]> childData = new TreeMap<String, Object[]>();
    FWCreateExcel() throws  Exception{
    }
     public void createExcel(String strFrameworkId) throws Exception{

        XSSFSheet sheet1 = workbook.createSheet("Categories");
        JSONObject categoryObj;
        String strFwStatus = masterObj.readFramework(strFrameworkId);
        if(strFwStatus.equalsIgnoreCase("successful")){
           JSONArray categoryList = masterObj.readFramework(strFrameworkId,"createExcel");
         //  String[] a = new String[categoryList.size()];

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
            createRowsAndColumns(data,sheet1,"categories");
            data.clear();
       //     data.put("1", new Object[]{"Category Code","Parent Term","Parent Code","Child Term-1 Name","Child Term-1 Code"});
              for(int i = 0 ; i < allCategoriesArray.size() ; i++ ){
                  categoryCode = (((JSONObject)(allCategoriesArray.get(i))).get("categoryCode")).toString();
                 categoryResponse = masterObj.readCategory(strFrameworkId,categoryCode);
                 if(categoryResponse.equalsIgnoreCase("successful")){
                    termList = masterObj.readCategory(strFrameworkId,categoryCode,"createExcel");
                     data.put("1", new Object[]{"Category Code","Parent Term","Parent Code","Child Term-1 Name","Child Term-1 Code"});
                     createRowsAndColumns(data,workbook.getSheetAt(i+1),"TermSheetHeading");
                     data.clear();
                     for(int j = 0 ; j < termList.size() ; j++){
                         writeParent((JSONObject) termList.get(j),j,i);
                      /*  termName=((JSONObject)termList.get(j)).get("name").toString();
                        termIdentifier=((JSONObject)termList.get(j)).get("identifier").toString();
                        termCode = termIdentifier.split("_");
                        if(j == 0){
                            data.put(""+(j+2),new Object[]{categoryCode,termName,termCode[2]});
                        }
                        else{
                            data.put(""+(j+2),new Object[]{"",termName,termCode[2]});

                        }
                        createRowsAndColumns(data,workbook.getSheetAt(i+1),"parentTerms");
                        data.clear();*/
                        checkChild(strFrameworkId,j,i);
                       /* JSONObject termdetails = masterObj.readTerm(strFrameworkId,categoryCode,termCode[2],"termdetails");
                       JSONArray children = (JSONArray)termdetails.get("children");
                       */
                       /*if(children != null && children.size() > 0) {
                           for (int k = 0; k < children.size(); k++) {
                               childtermName = ((JSONObject) children.get(k)).get("name").toString();
                               childtermIdentifier = ((JSONObject) children.get(k)).get("identifier").toString();
                               childtermCode = childtermIdentifier.split("_");
                               childData.put("" + (j + 2), new Object[]{childtermName, childtermCode[2]});
                                createRowsAndColumns(childData, workbook.getSheetAt(i + 1), "childterms");
                                 cellnumber = cellnumber + 2;
                                childData.clear();


                           }

                           cellnumber = 3;
                           childData.clear();
                           //  JSONArray associations = (JSONArray)termdetails.get("associations");
                       } */

                         rownumber++;
                    }
                     rownumber = 1;
   }
              }


            XSSFSheet sheet2 = workbook.createSheet("Associations");
            FileOutputStream out = new FileOutputStream(new File("writingExcelDemo.xlsx"));
            workbook.write(out);
            out.close();
        }
    }
    public void createRowsAndColumns(Map<String, Object[]> data,XSSFSheet sheet,String action){
        Set<String> keyset = data.keySet();
        int rownum,cellnum;
        if(action.equalsIgnoreCase("categories") || action.equalsIgnoreCase("TermSheetHeading")){
             rownum = 0;

        }
        else
        {
            rownum = rownumber;
        }
        for (String key : keyset)
        {

            Row row = sheet.getRow(rownum);
            if(row == null ){
             row = sheet.createRow(rownum);
            }
            Object [] objArr = data.get(key);
            if(action.equalsIgnoreCase("categories") || action.equalsIgnoreCase("parentTerms") || action.equalsIgnoreCase("TermSheetHeading")){
                cellnum = 0;
                rownum++;

            } else {
                cellnum = cellnumber;
            }
            for (Object obj : objArr)
            {
                Cell cell = row.createCell(cellnum++);
                if(obj instanceof String)
                    cell.setCellValue((String)obj);
                else if(obj instanceof Integer)
                    cell.setCellValue((Integer)obj);
            }
        }
    }
    public void writeParent(JSONObject termObj,int j,int i){
        termName=((termObj).get("name")).toString();
        termIdentifier=((JSONObject)termList.get(j)).get("identifier").toString();
        termCode = termIdentifier.split("_");
        if(j == 0){
            data.put(""+(j+2),new Object[]{categoryCode,termName,termCode[2]});
        }
        else{
            data.put(""+(j+2),new Object[]{"",termName,termCode[2]});

        }
        createRowsAndColumns(data,workbook.getSheetAt(i+1),"parentTerms");
        data.clear();
    }
    public void writeChild(JSONArray children,int j,int i){

            for (int k = 0; k < children.size(); k++) {
                childtermName = ((JSONObject) children.get(k)).get("name").toString();
                childtermIdentifier = ((JSONObject) children.get(k)).get("identifier").toString();
                childtermCode = childtermIdentifier.split("_");
                childData.put("" + (j + 2), new Object[]{childtermName, childtermCode[2]});
                createRowsAndColumns(childData, workbook.getSheetAt(i + 1), "childterms");
                cellnumber = cellnumber + 2;
                childData.clear();


            }

        cellnumber = 3;
        childData.clear();

    }
    public void checkChild(String strFrameworkId,int j,int i){
        // yes -> writeChild();
        JSONObject termdetails = masterObj.readTerm(strFrameworkId,categoryCode,termCode[2],"termdetails");
        JSONArray children = (JSONArray)termdetails.get("children");
        if(children != null && children.size() > 0) {
            writeChild(children,j,i);
    }
}
}
