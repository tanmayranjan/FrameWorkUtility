
package org.sunbird;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import sun.awt.FwDispatcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FWNewExcelReader {

    static ArrayList<String> errorList = new ArrayList<>();
    public static void readExcel(File inputFile,IniFile configFile,String strChannel,String strFrameworkId) {

        String strFileExtn = inputFile.getName().substring(inputFile.getName().lastIndexOf(".")+1);
        JSONObject jo = new JSONObject();
        String temp="";
        String temp2="";
        String parentCategory="",parentTermResponse="",childTermResponse="",childTermResponseArray[],parentTermResponseArray[],parentTermCode="";
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
                    XSSFCell termCell;
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
                            if(sheetname.equalsIgnoreCase("categories")){

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
                                /*else if(j != 0) {
                                    // create the categories terms
                                    parentTerm = cell.toString();
                                    masternew.createTerm(strFrameworkId,strChannel,parentCategoryCode,parentTerm);
                                }*/
                            }
                            else if (sheetname.equalsIgnoreCase("associations")){

                                if(cell != null){

                                    if(j == 0 && temp == ""){
                                        temp = cell.toString();
                                    }
                                    if(j==1){
                                        continue;
                                    }
                                    if(j == 2 && temp2 == ""){
                                        temp2 =cell.toString();
                                    }
                                    header = sheet.getRow(0).getCell(j);
                                    heading = header.toString().toLowerCase().replaceAll("\\s+","");
                                    if(heading.equalsIgnoreCase("parentcategorycode")){
                                        if(cell.toString() != null){
                                            parentCategoryForAssociation = cell.toString();

                                        }
                                       // parentCategoryForAssociation = cell.toString();
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
                                    else if(heading.equalsIgnoreCase("parenttermcode")){
                                       if(cell.toString() != null){
                                           parentTermForAssociation = cell.toString();
                                       }
                                      //  parentTermForAssociation = cell.toString();
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
                                    else if(heading.equalsIgnoreCase("associatedcategorycode")){
                                        categories.add(cell.toString());
                                    }
                                    else if(heading.equalsIgnoreCase("associatedtermcode")){
                                        terms.add(cell.toString());
                                    }

                                }
                            }
                            else{
                                FWNewMasterFile.termCode="";
                                if(j == 0) {
                                    // get the category code
                                    if(cell.toString() != null){
                                        parentCategoryCode = cell.toString();
                                    }
                                   // parentCategoryCode = sheetname;
                                    if(parentCategoryCode.replaceAll("\\s+", "").equalsIgnoreCase("gradelevel")){
                                        parentCategoryCode = "gradeLevel";
                                    }
                                }
                                else if(j == 1){
                                    parentTerm=cell.toString();
                                    // create the parent term
                                    if(row.getCell(2) == null){
                                        parentTermResponse = masternew.createTerm(strFrameworkId,strChannel,parentCategoryCode,parentTerm,"parent");
                                    }
                                    else{
                                        FWNewMasterFile.termCode = (row.getCell(2)).toString();
                                        parentTermResponse = masternew.createTerm(strFrameworkId,strChannel,parentCategoryCode,parentTerm,"parent");

                                    }
                                    parentTermResponseArray =  parentTermResponse.split("_");
                                    if(parentTermResponseArray[0].equalsIgnoreCase("successful")){
                                        parentTermCode = parentTermResponseArray[1];
                                        updateSheet(is,wb,iIndex,i,j,parentTerm,parentTermCode,numberofrows);
                                    }
                                    if(parentTermResponseArray[0].equalsIgnoreCase("alreadycreated")){
                                        parentTermCode = FWNewMasterFile.termCode;
                                    }
                                }
                                else if((j%2==1) && cell.toString() != ""){
                                    if(!(row.getCell(j+1) == null)){
                                        termCell = row.getCell(j+1);
                                        FWNewMasterFile.termCode = termCell.toString();
                                    }
                                    childTermResponse =  masternew.createTerm(strFrameworkId,strChannel,parentCategoryCode,cell.toString(),"child");
                                    childTermResponseArray = childTermResponse.split("_");
                                    // Create the child term
                                    // Add the child term to list
                                    if(childTermResponseArray[0].equalsIgnoreCase("successful")){
                                        updateSheet(is,wb,iIndex,i,j,cell.toString(),childTermResponseArray[1],numberofrows);

                                        childTerm.add(childTermResponseArray[1]);

                                    }
                                }
                                if(j == numberofColumns - 1){
                                    FileOutputStream out = new FileOutputStream(new File(FWMainFile.xlsfile));
                                    wb.write(out);
                                    out.close();
                                }
                            }


                        }
                        // Assign parent child relationship of terms
                        if(childTerm.size() > 0 && parentTermCode != ""){
                            masternew.createParentChildRelation(strFrameworkId,strChannel,parentCategoryCode,parentTermCode,childTerm);
                            parentTermCode = "";
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
            System.exit(1);
        }

//		System.out.println("FWExcel Reader --> lContentList.size:: " + lContentList.size());

        //   return lContentList;
    }
public static void updateSheet(InputStream file,XSSFWorkbook wb,int sheetIndex,int row,int column,String termName,String termCode,int numberofRows) throws Exception{
        // term code in term list
    XSSFCell cell;
   setCell((wb.getSheetAt(sheetIndex)).getRow(row),column,termCode,"allterms");
//if term is a child and exist as a parent also
    for(int i=row+1 ;i < numberofRows ; i++){
        cell= ((wb.getSheetAt(sheetIndex)).getRow(i)).getCell(1);
        if(termName.equalsIgnoreCase(cell.toString())){
            setCell((wb.getSheetAt(sheetIndex)).getRow(i),1,termCode,"childTerm");

        }
    }
    // sheet of association if term is found
setAssociationSheetCell(wb,termName,termCode);
}
public static void setCell(XSSFRow row,int index,String value,String action){
        XSSFCell cell;
    cell= (row).getCell(index+1);
    if(cell == null){
        if(action.equalsIgnoreCase("childTerm")){
            index=1;
        }
        cell = (row).createCell(index+1);
        cell.setCellValue(value);
    }
}
public static void setAssociationSheetCell(XSSFWorkbook wb,String termName,String termCode) {

    XSSFSheet sheet = wb.getSheet("Associations");
    XSSFRow row;
    XSSFCell cell;
    int numberofrows = sheet.getPhysicalNumberOfRows();
    for (int i = 1; i < numberofrows; i++) {
        row = sheet.getRow(i);
        if (row == null) {
            //do something with an empty row
            continue;
        }
        int numberofColumns = row.getLastCellNum();
        for (int j = 1; j < numberofColumns; j++) {
            if((j == 1 || j == 4)){
                cell = row.getCell(j);
                if(cell != null && cell.toString().equalsIgnoreCase(termName)){
                    setCell(row,j,termCode,"setAssociation");

                }
            }
        }
    }
        }
}
