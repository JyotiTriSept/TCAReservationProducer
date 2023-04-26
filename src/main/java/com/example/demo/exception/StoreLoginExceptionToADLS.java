package com.example.demo.exception;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.file.datalake.DataLakeDirectoryClient;
import com.azure.storage.file.datalake.DataLakeFileClient;
import com.azure.storage.file.datalake.DataLakeFileSystemClient;
import com.azure.storage.file.datalake.DataLakeServiceClient;
import com.azure.storage.file.datalake.DataLakeServiceClientBuilder;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.DownloadRetryOptions;
import com.azure.storage.file.datalake.models.FileRange;
import com.azure.storage.file.datalake.models.PathHttpHeaders;
import com.azure.storage.file.datalake.models.PathInfo;

public class StoreLoginExceptionToADLS {
	private static final String DIRECTORYNAME="RAW";
	private static final String SUBDIRECTORYNAME="TCA-API";
	private static final String SUBDIRECTORYNAME1="PipelineErrors";
	private static final String SUBDIRECTORYNAME2="LatestData";
	private static final String ARCHIVE="Archive";
	private static final String LATESTFILENAME="LatestException.json";
	private static final String CONTENTLANGUAGE="en-US";
	private static final String CONTENTTYPE="binary";
	private static final String ENDPOINT="https://" + "wohalgpmsdldevsa" + ".dfs.core.windows.net";

	public static void storingExceptionInArchiveLocation(String exceptionMessage, String moduleName) {
		DataLakeServiceClient getDataLakeServiceClient = GetDataLakeServiceClient("wohalgpmsdldevsa", "iSh4zGYXanmJdEhCSv/Qg1h+GF37rsfZAwIzzo0nByAgg6itXlDVQHFVe2gf5vK+3l4eFvtPWaVNj2P4f0wQow==");

		DataLakeFileSystemClient fileSystemClient = getDataLakeServiceClient
				.getFileSystemClient("woh-alg-pms-datalake-dev");
		
	ZonedDateTime currentInstant = ZonedDateTime.now();
	int year = currentInstant.getYear();
	int month = currentInstant.getMonthValue();
	int day = currentInstant.getDayOfMonth();
	int hour = currentInstant.getHour();
	int minute = currentInstant.getMinute();
	int second = currentInstant.getSecond();
	String fileName = "AMR"+"_"+moduleName+"_"+year+"-"+month+"-"+day+".json";
	
		DataLakeDirectoryClient subdirectoryClient = fileSystemClient.getDirectoryClient(DIRECTORYNAME)
				                                    .getSubdirectoryClient(SUBDIRECTORYNAME)
				                                    .getSubdirectoryClient(SUBDIRECTORYNAME1)
				                                    .getSubdirectoryClient(ARCHIVE)
				                                    .getSubdirectoryClient(ARCHIVE+"_"+year+"-"+month+"-"+day)
				                                    .getSubdirectoryClient(ARCHIVE+"_"+year+"-"+month+"-"+day+"_"+hour+"-"+minute+"-"+second)
				                                    .getSubdirectoryClient(moduleName);
		DataLakeFileClient fileClient = subdirectoryClient.createFile(fileName);
		fileClient.append(BinaryData.fromString(exceptionMessage), 0);
		writeToDataLakeBlobStorageWithResponse(exceptionMessage.length(),fileClient);
	}
	
	private static DataLakeServiceClient GetDataLakeServiceClient (String accountName, String accountKey){

	    StorageSharedKeyCredential sharedKeyCredential =
	        new StorageSharedKeyCredential(accountName, accountKey);

	    DataLakeServiceClientBuilder builder = new DataLakeServiceClientBuilder();

	    builder.credential(sharedKeyCredential);
	    builder.endpoint(ENDPOINT);

	    return builder.buildClient();
	}
	
	private static boolean writeToDataLakeBlobStorageWithResponse(int i, DataLakeFileClient fileClient) {
		FileRange range = new FileRange(1024, 2048L);
		 DownloadRetryOptions options = new DownloadRetryOptions().setMaxRetryRequests(5);
		 byte[] contentMd5 = new byte[0]; // Replace with valid md5
		 boolean retainUncommittedData = false;
		 boolean close = false;
		 PathHttpHeaders httpHeaders = new PathHttpHeaders()
		     .setContentLanguage(CONTENTLANGUAGE)
		     .setContentType(CONTENTTYPE);
		 DataLakeRequestConditions requestConditions = new DataLakeRequestConditions();


		 Response<PathInfo> response = fileClient.flushWithResponse(i, retainUncommittedData, close, httpHeaders,
			     requestConditions, null, Context.NONE);
		 if(response.getStatusCode() == 200) {
			 System.out.printf("Flush of login exception completed with status %d%n", response.getStatusCode());
			 return true;
		 } else {
			 
			 return false;
		 }
	}

	public static void updateToLatestFolder(String exceptionMessage) {
		DataLakeServiceClient getDataLakeServiceClient = GetDataLakeServiceClient("wohalgpmsdldevsa", "iSh4zGYXanmJdEhCSv/Qg1h+GF37rsfZAwIzzo0nByAgg6itXlDVQHFVe2gf5vK+3l4eFvtPWaVNj2P4f0wQow==");

		DataLakeFileSystemClient fileSystemClient = getDataLakeServiceClient
				.getFileSystemClient("woh-alg-pms-datalake-dev");
		
		DataLakeDirectoryClient subdirectoryClient = fileSystemClient.getDirectoryClient(DIRECTORYNAME)
                .getSubdirectoryClient(SUBDIRECTORYNAME)
                .getSubdirectoryClient(SUBDIRECTORYNAME1)
                .getSubdirectoryClient(SUBDIRECTORYNAME2);
		
		DataLakeFileClient fileClient = subdirectoryClient.createFile(LATESTFILENAME);
		fileClient.append(BinaryData.fromString(exceptionMessage), 0);
		writeToDataLakeBlobStorageWithResponse(exceptionMessage.length(),fileClient);
	}
}
