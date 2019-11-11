
package org.sunbird;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FWNewExcelReader {

    static ArrayList<String> errorList = new ArrayList<>();
    public static void readExcel(File inputFile,IniFile configFile,String strChannel,String strFrameworkId) {

        String strFileExtn = inputFile.getName().substring(inputFile.getName().lastIndexOf(".")+1);
        List lContentList = new ArrayList();
        JSONObject jo = new JSONObject();
        int iHeaderLength = 0;
        String ctcode;
        String temp="";
        String temp2="";
        String parentCategory="";
        String parentCategoryCode="";
        String parentCategoryName="";
        String parentCategoryForAssociation="";
        String categoryDescription;
        String parentTermForAssociation="";
        String parentTerm="";
        String heading;
        ArrayList<String> childTerm = new ArrayList<String>();
        ArrayList<String> categories = new ArrayList<String>();
        ArrayList<String> terms = new ArrayList<String>();
        ArrayList<Object> childTermForAssociation = new ArrayList<Object>();
        try {
            FWNewMasterFile masternew = new FWNewMasterFile(configFile);
            if(strFileExtn.equalsIgnoreCase("xlsx") || strFileExtn.equalsIgnoreCase("xls"))
            {

                InputStream is = new FileInputStream(inputFile);

                // Get the workbook instance for XLSX/XLS file
                XSSFWorkbook wb = new XSSFWorkbook(is);

                int iNumOfSheets = wb.getNumberOfSheets();
//				System.out.println("FWExcelSheets --> iNumOfSheets:: " + iNumOfSheets);
                for(int iIndex=0; iIndex<iNumOfSheets; iIndex++)
                {
                    int iRow=0;
//					System.out.println("FWExcelSheets --> iIndex:: " + iIndex);
                    // Get first sheet from the workbook
                    XSSFSheet sheet = wb.getSheetAt(iIndex);

                    XSSFRow row;
                    XSSFCell cell;
                    XSSFCell tempName;
                    XSSFCell header;
                    List lContentDataList = null;
                    int numberofrows= sheet.getPhysicalNumberOfRows();
                    int numberofrows2 = sheet.getLastRowNum()+1;
                    for (int i = 1; i < numberofrows; i++) {
                        parentCategory = "";
                        parentTerm = "";
                        childTerm.clear();
                        row = sheet.getRow(i);
                        if (row == null) {
                            //do something with an empty row
                            continue;
                        }
                        String sheetname = sheet.getSheetName().toLowerCase();
                        int numberofColumns = row.getLastCellNum();
                        for (int j = 0; j < numberofColumns; j++) {
                            cell = row.getCell(j);
                            if(cell == null ){
                                errorList.add("Empty cell on Sheet " +sheetname+ " on row "+ (i+1) +" on column " + (j+1) + "\n");
                                continue;
                            }
                            if(sheetname.equalsIgnoreCase("categories&terms")){

                                if(j == 1){
                                    tempName = row.getCell(j-1);
                                    if(tempName == null){
                                        parentCategoryName = "";
                                    }
                                    else{
                                        parentCategoryName = tempName.toString();
                                    }
                                    parentCategoryCode = cell.toString();
                                    if(parentCategoryCode.replaceAll("\\s+", "").equalsIgnoreCase("gradelevel")){
                                        parentCategoryCode = "gradeLevel";
                                    }
                                    // create category
                                    masternew.createCategory(strFrameworkId,strChannel,parentCategoryCode,parentCategoryName);


                                }
                                else if(j != 0) {
                                    // create the categories terms
                                    parentTerm = cell.toString();
                                    masternew.createTerm(strFrameworkId,strChannel,parentCategoryCode,parentTerm);
                                }
                            }
                            else if (sheetname.equalsIgnoreCase("associations")){

                                if(cell != null){

                                    if(j == 0 && temp == ""){
                                        temp = cell.toString();
                                    }
                                    if(j == 1 && temp2 == ""){
                                        temp2 =cell.toString();
                                    }
                                    header = sheet.getRow(0).getCell(j);
                                    heading = header.toString().toLowerCase().replaceAll("\\s+","");
                                    if(heading.equalsIgnoreCase("parentcategory")){
                                        parentCategoryForAssociation = cell.toString();
                                        if(!temp.equalsIgnoreCase(parentCategoryForAssociation)){
                                            // Association
                                            jo.put("categories",categories);
                                            jo.put("terms",terms);
                                            childTermForAssociation.add(jo);
                                            masternew.createAssociations(strFrameworkId,strChannel,temp,parentTermForAssociation,childTermForAssociation);
                                            categories.clear();
                                            terms.clear();
                                            childTermForAssociation.clear();
                                            temp = parentCategoryForAssociation;
                                        }
                                    }
                                    else if(heading.equalsIgnoreCase("parentterm")){
                                        parentTermForAssociation = cell.toString();
                                        if(!temp2.equalsIgnoreCase(parentTermForAssociation)){
                                            // Association
                                            jo.put("categories",categories);
                                            jo.put("terms",terms);
                                            childTermForAssociation.add(jo);
                                            masternew.createAssociations(strFrameworkId,strChannel,parentCategoryForAssociation,temp2,childTermForAssociation);
                                            categories.clear();
                                            terms.clear();
                                            childTermForAssociation.clear();
                                            temp2 = parentTermForAssociation;
                                        }

                                    }
                                    else if(heading.equalsIgnoreCase("associatedcategory")){
                                        categories.add(cell.toString());
                                    }
                                    else if(heading.equalsIgnoreCase("associatedterm")){
                                        terms.add(cell.toString());
                                    }
                                 /*   header = sheet.getRow(0).getCell(j);
                                    if(parentTerm != "" && parentCategory != ""){
                                        // do association with parentTerm and parentCategory
                                        jo.put("category",header.toString());
                                        jo.put("term",cell.toString());
                                        childTermForAssociation.add(jo);
                                      }
                                   if(parentTerm == ""){
                                       parentTerm = cell.toString();
                                   }
                                    if(parentCategory == ""){
                                        parentCategory = header.toString();
                                    }

                                 */
                                }
                            }
                            else{
                                if(j == 0) {
                                    // get the category code
                                    parentCategoryCode = sheetname;
                                    if(parentCategoryCode.replaceAll("\\s+", "").equalsIgnoreCase("gradelevel")){
                                        parentCategoryCode = "gradeLevel";
                                    }
                                }
                                else if(j == 1){
                                    // get the parent term
                                    parentTerm=cell.toString();
                                }
                                else {
                                    // get the child term
                                    childTerm.add(cell.toString());
                                    //   masternew.createParentChildRelation(strFrameworkId,strChannel,sheetname,parentTerm,childTerm);
                                }
                            }


                        }
                        // Assign parent child relationship of terms
                        if(childTerm.size() > 0){
                            masternew.createParentChildRelation(strFrameworkId,strChannel,parentCategoryCode,parentTerm,childTerm);
                        }


                    }
                    // Association
                    if(categories.size() > 0 && terms.size() > 0){
                        jo.put("categories",categories);
                        jo.put("terms",terms);
                        childTermForAssociation.add(jo);
                        masternew.createAssociations(strFrameworkId,strChannel,parentCategoryForAssociation,parentTermForAssociation,childTermForAssociation);

                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("FWExcel Reader --> Exception :" + e.getMessage());
        }

//		System.out.println("FWExcel Reader --> lContentList.size:: " + lContentList.size());

        //   return lContentList;
    }

}
