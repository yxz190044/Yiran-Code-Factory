package com.amazon.a4kcontentmanagement.tools.search.backfill;

import amazon.odin.awsauth.OdinAWSCredentialsProvider;
import amazon.platform.config.AppConfig;
import com.amazon.a4kcontentmanagement.tools.model.A4KCatalogModel;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import http://org.apache.poi.ss.usermodel.Cell;
import http://org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class ExcelDao {
	private static final String REGION = "CustomerState.S3.Region";
	final private A4KCatalogModel a4KCatalogModel;
	private static final String S3_MATERIAL_SET = "CustomerState.S3.MaterialSet";
	@Inject
	ExcelDao(final A4KCatalogModel a4KCatalogModel) {
		this.a4KCatalogModel = a4KCatalogModel;
	}
	//    private final AmazonS3 amazonS3Client = AmazonS3ClientBuilder.standard().build();
	//
	//    private String loadStringFromS3() {
	//        try {
	//            return amazonS3Client.getObjectAsString(BUCKET_NAME, FILE_NAME);
	//        } catch (final IOException e) {
	//            e.printStackTrace();}
	//            return null;
	//        }
	
	public Stream<A4KCatalogModel> getExcelInfo() {
		
		try {
			String bucket = AppConfig.findString("bucket");
			String key = AppConfig.findString("key");
			//            GetObjectRequest request = new GetObjectRequest(bucket, key);
			List<A4KCatalogModel> models = new ArrayList<>();
			AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
			.withRegion(AppConfig.findString(REGION))
			.withCredentials(new OdinAWSCredentialsProvider(AppConfig.findString(S3_MATERIAL_SET)))
			.build();
			String filePath = s3Client.getObjectAsString(bucket, key);
			File file = new File(filePath);   //creating a new file instance
			FileInputStream files = new FileInputStream(file);   //obtaining bytes from the file
			//creating Workbook instance that refers to .xlsx file
			XSSFWorkbook wb = new XSSFWorkbook(files);
			XSSFSheet sheet = wb.getSheetAt(0);
			//creating a Sheet object to retrieve object
			Iterator<Row> itr = sheet.iterator();    //iterating over Excel file
			
			while (itr.hasNext()) {
				Row row = itr.next();
				A4KCatalogModel model = new A4KCatalogModel();//每一个row都创建了一个model
				Iterator<Cell> cellIterator = row.cellIterator();   //iterating over each column
				int count = 0;
				while (cellIterator.hasNext()) {
					Cell cell = cellIterator.next();
					if (count % 4 == 0) {
						model.setCoreItemId(cell.getStringCellValue());
					} else if (count % 4 == 1) {
						model.setEncryptedMarketplaceId(cell.getStringCellValue());
					} else if (count % 4 == 2) {
						model.setTitle(cell.getStringCellValue());
					} else {
						model.setVideoType(cell.getStringCellValue());
					}
					count++;
					
				}
				models.add(model);
			}
			return models.stream();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null; //????
	}
}
