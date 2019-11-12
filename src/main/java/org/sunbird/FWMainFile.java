package org.sunbird;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Logger;


public class FWMainFile {
	
	public static void main(String args[]) throws GeneralSecurityException, SecurityException, IOException
	{
		Path currentRelativePath = Paths.get("");
		String strFilePath = currentRelativePath.toAbsolutePath().toString();
		
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Enter 1 to create/update framework using excel \nEnter 2 to Publish an existing framework");
            int opt = Integer.parseInt(br.readLine());
            switch (opt) {
				case 1:
					String strFrameworkDescr = "";
					System.out.print("Enter Full Path of Excel file including name:: ");
					//   String xlsfile = "D:\\TaxonomyFiles(copy)\\Taxonomy Files\\NCERTtestAditya.xlsx";
					String xlsfile = br.readLine();
					if (xlsfile == null || xlsfile.isEmpty()) {
						System.out.println("Excel Input is mandatory");
						System.exit(0);
					}
					if (!(xlsfile.contains(".xls") || xlsfile.contains(".xlsx"))) {
						System.out.println("Invalid file format \n Please Upload only .xlsx or .xls files");
						System.exit(0);
					}

					System.out.print("Enter Framework Name :: ");

					//	String strFrameworkName = "testNCERT";

					String strFrameworkName = br.readLine(); //Ex: 98e09d6e-b95b-4832-bfab-421e63d36aa7

					if (strFrameworkName == null || strFrameworkName.isEmpty()) {
						System.out.println("Framework Name is mandatory");
						System.exit(0);
					}
					System.out.print("Enter Framework Id:: ");

					//	String strFrameworkId = "testNCERT6";
					String strFrameworkId = br.readLine();

					if (strFrameworkId == null || strFrameworkId.isEmpty()) {
						System.out.println("Framework Id is mandatory");
						System.exit(0);
					}
					System.out.print("Enter Framework description :: ");
					strFrameworkDescr = br.readLine();

					//		System.out.print("Enter Channel:: ");


					System.out.print("Enter channel :: ");
					//	String strChannel = "0128381801670574080";
					String strChannel = br.readLine();

					if (strChannel == null || strChannel.isEmpty()) {
						System.out.println("Channel is mandatory");
						System.exit(0);
					}
					IniFile loadConfig = new IniFile(strFilePath + "/configLive.ini");

					FWNewMasterFile newmaster = new FWNewMasterFile(loadConfig);
					newmaster.createFramework(xlsfile, strFrameworkName, strFrameworkId, strFrameworkDescr, strChannel);
					System.out.println("Changes will be reflected when you publish this Framework \n Press 1 to publish this framework or Press any other key to continue");
					int publishoption = Integer.parseInt(br.readLine());
					if (publishoption == 1) {
						String publishStatus = newmaster.publishFramework(strFrameworkId, strChannel);
						if (publishStatus.equals("successful")) {
							System.out.println("Framework published Successfully");
						}
					}
				//	newmaster.errorList();
					break;
				case 2 :
					System.out.println("Enter framework id to publish ::");
					 strFrameworkId = br.readLine();
					if (strFrameworkId == null || strFrameworkId.isEmpty()) {
						System.out.println("Framework Id is mandatory");
						System.exit(0);
					}
					 loadConfig = new IniFile(strFilePath + "/configLive.ini");

					FWNewMasterFile fwNewMasterFile = new FWNewMasterFile(loadConfig);
					String readFwResponse = fwNewMasterFile.readFramework(strFrameworkId);
					if(readFwResponse.equals("successful")){
						System.out.println("Enter channel");
						strChannel = br.readLine();
						if (strChannel == null || strChannel.isEmpty()) {
							System.out.println("Channel is mandatory");
							System.exit(0);
						}
                    String publishFWResponse = fwNewMasterFile.publishFramework(strFrameworkId,strChannel);
                    if(publishFWResponse.equals("successful")){
                    	System.out.println("Framework published successfully");
					}
					}
					break;
				default:
					System.out.println("Enter valid option");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			
			String dateInString = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss").format(new Date());
			dateInString = dateInString.replaceAll(":", "-");
			Logger logger = Logger.getLogger("MainFileFrameworkCreationLog");  
		    FileHandler fh;   
	        // This block configure the logger with handler and formatter  
	        logger.info(e.toString());
	        logger.info(e.getMessage());
			e.printStackTrace();
		}
	}
}