package org.sunbird;

import org.apache.poi.ss.usermodel.Cell;
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
    public void createExcel(String strFrameworkId) throws Exception{

        Path currentRelativePath = Paths.get("");
        String strFilePath = currentRelativePath.toAbsolutePath().toString();
        IniFile loadConfig = new IniFile(strFilePath + "/configLive.ini");

        FWNewMasterFile masterObj = new FWNewMasterFile(loadConfig);
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet1 = workbook.createSheet("Categories");
        String categoryCode,categoryName,categoryIdentifier,categoryResponse,termIdentifier,termName;
        JSONObject categoryObj;
        String strFwStatus = masterObj.readFramework(strFrameworkId);
        if(strFwStatus.equalsIgnoreCase("successful")){
           JSONArray categoryList = masterObj.readFramework(strFrameworkId,"createExcel");
         //  String[] a = new String[categoryList.size()];
           JSONArray allCategoriesArray = new JSONArray();
           JSONArray termList = new JSONArray();
            Map<String, Object[]> data = new TreeMap<String, Object[]>();
            data.put("1", new Object[] {"Category Name", "Category Code"});
           for(int i = 0 ; i < categoryList.size() ; i++)
           {
                categoryObj = (JSONObject) categoryList.get(i);
                categoryIdentifier = ((categoryObj.get("identifier")).toString());
                categoryName = ((categoryObj.get("name")).toString());
                categoryCode = categoryIdentifier.substring(categoryIdentifier.indexOf('_')+ 1);
            //   System.out.println(categoryCode + " " + categoryName );
               data.put(""+(i+2),new Object[]{categoryName,categoryCode});
               JSONObject allCategories = new JSONObject();
               allCategories.put("categoryCode",categoryCode);
               allCategories.put("sheet", workbook.createSheet(categoryName));
               allCategoriesArray.add(allCategories);
              // List<Map<String,Object>> allCategories = new ArrayList<>();
           }
            createRowsAndColumns(data,sheet1);
            data.clear();
            data.put("1", new Object[]{"Category Code","Parent Term"});
              for(int i = 0 ; i < allCategoriesArray.size() ; i++ ){
                  categoryCode = (((JSONObject)(allCategoriesArray.get(i))).get("categoryCode")).toString();
                 categoryResponse = masterObj.readCategory(strFrameworkId,categoryCode);
                 if(categoryResponse.equalsIgnoreCase("successful")){
                    termList = masterObj.readCategory(strFrameworkId,categoryCode,"createExcel");
                    for(int j = 0 ; j < termList.size() ; j++){
                        termName=((JSONObject)termList.get(j)).get("name").toString();
                        if(j == 0){
                            data.put(""+(j+2),new Object[]{categoryCode,termName});

                        }
                        else{
                            data.put(""+(j+2),new Object[]{"",termName});

                        }
                    }

                     createRowsAndColumns(data,workbook.getSheetAt(i+1));
                     data.clear();
                     data.put("1", new Object[]{"Category Code","Parent Term"});
                 }
              }
            XSSFSheet sheet2 = workbook.createSheet("Associations");
            FileOutputStream out = new FileOutputStream(new File("writingExcelDemo.xlsx"));
            workbook.write(out);
            out.close();
        }
    }
    public void createRowsAndColumns(Map<String, Object[]> data,XSSFSheet sheet){
        Set<String> keyset = data.keySet();
        int rownum = 0;
        for (String key : keyset)
        {
            Row row = sheet.createRow(rownum++);
            Object [] objArr = data.get(key);
            int cellnum = 0;
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
}
