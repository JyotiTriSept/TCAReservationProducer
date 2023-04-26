package com.example.demo.exception;

import java.io.ByteArrayInputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.Constants;
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
import com.azure.storage.file.datalake.options.FileParallelUploadOptions;

public class StoreReservExceptionToBlob {
	private static final String DIRECTORYNAME="RAW";
	private static final String SUBDIRECTORYNAME="TCA-API";
	private static final String SUBDIRECTORYNAME1="PipelineErrors";
	private static final String SUBDIRECTORYNAME2="LatestData";
	private static final String ARCHIVE="Archive";

	public static void storingExceptionInArchiveLocation(String exceptionMessage, String moduleName, String folderName) {
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
	String fileName = folderName+"_"+moduleName+"_"+year+"-"+month+"-"+day+"_"+hour+"-"+minute+"-"+second+".json";
	
		DataLakeDirectoryClient subdirectoryClient = fileSystemClient.createDirectoryIfNotExists(DIRECTORYNAME)
				                                    .createSubdirectoryIfNotExists("TCA-API")
				                                    .createSubdirectoryIfNotExists("PipelineErrors")
				                                    .createSubdirectoryIfNotExists("Archive")
				                                    .createSubdirectoryIfNotExists("Archive_"+year+"-"+month+"-"+day)
				                                    .createSubdirectoryIfNotExists("Archive_"+year+"-"+month+"-"+day+"_"+hour+"-"+minute+"-"+second+UUID.randomUUID().toString())
				                                    .createSubdirectoryIfNotExists(moduleName)
				                                    .createSubdirectoryIfNotExists(folderName);
		DataLakeFileClient fileClient = subdirectoryClient.createFileIfNotExists(fileName);
		String msg = exceptionMessage.trim();
		fileClient.upload(BinaryData.fromString(msg), true);
		System.out.println("Error stored in path: "+fileClient.getFilePath()+" and filename is: "+fileName);
		//writeToDataLakeBlobStorageWithResponse(msg.length(),fileClient);
	}
	
	private static DataLakeServiceClient GetDataLakeServiceClient (String accountName, String accountKey){

	    StorageSharedKeyCredential sharedKeyCredential =
	        new StorageSharedKeyCredential(accountName, accountKey);

	    DataLakeServiceClientBuilder builder = new DataLakeServiceClientBuilder();

	    builder.credential(sharedKeyCredential);
	    builder.endpoint("https://" + accountName + ".dfs.core.windows.net");

	    return builder.buildClient();
	}
	
	private static boolean writeToDataLakeBlobStorageWithResponse(long i, DataLakeFileClient fileClient) {
		FileRange range = new FileRange(1024, 2048L);
		 DownloadRetryOptions options = new DownloadRetryOptions().setMaxRetryRequests(5);
		 byte[] contentMd5 = new byte[0]; // Replace with valid md5
		 boolean retainUncommittedData = false;
		 boolean close = false;
		 PathHttpHeaders httpHeaders = new PathHttpHeaders()
		     .setContentLanguage("en-US")
		     .setContentType("binary");
		 DataLakeRequestConditions requestConditions = new DataLakeRequestConditions();


		 Response<PathInfo> response = fileClient.flushWithResponse(i, retainUncommittedData, close, httpHeaders,
			     requestConditions, null, Context.NONE);
		 if(response.getStatusCode() == 200) {
			 System.out.printf("Flush data completed with status %d%n", response.getStatusCode());
			 return true;
		 } else {
			 
			 return false;
		 }
	}

	public static void updateToLatestFolder(String exceptionMessage, String moduleName, String fileName) {
		DataLakeServiceClient getDataLakeServiceClient = GetDataLakeServiceClient("wohalgpmsdldevsa", "iSh4zGYXanmJdEhCSv/Qg1h+GF37rsfZAwIzzo0nByAgg6itXlDVQHFVe2gf5vK+3l4eFvtPWaVNj2P4f0wQow==");

		DataLakeFileSystemClient fileSystemClient = getDataLakeServiceClient
				.getFileSystemClient("woh-alg-pms-datalake-dev");
		
		DataLakeDirectoryClient subdirectoryClient = fileSystemClient.createDirectoryIfNotExists("RAW")
                .createSubdirectoryIfNotExists("TCA-API")
                .createSubdirectoryIfNotExists("PipelineErrors")
                .createSubdirectoryIfNotExists("Latest")
                .createSubdirectoryIfNotExists(UUID.randomUUID().toString())
                .createSubdirectoryIfNotExists(moduleName);
		fileName = fileName+"_"+moduleName+".json";
		DataLakeFileClient fileClient = subdirectoryClient.createFile(fileName,true);
		String msg = exceptionMessage.trim();
		fileClient.upload(BinaryData.fromString(msg), true);
		System.out.println("Thread: "+Thread.currentThread().getName()+"Error message uploaded successfully in path : "+fileClient.getFilePath()+ " Filename: "+fileName);
		//uploadToADLSStorageWithResponse(msg, fileClient);
		//writeToDataLakeBlobStorageWithResponse(msg.length(),fileClient);
	}

	private static void uploadToADLSStorageWithResponse(String msg, DataLakeFileClient fileClient) {

		  PathHttpHeaders headers = new PathHttpHeaders()
		     .setContentMd5("data".getBytes(StandardCharsets.UTF_8))
		     .setContentLanguage("en-US")
		     .setContentType("binary");

		 Map<String, String> metadata = Collections.singletonMap("metadata", "value");
		 DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
			 .setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD);
		     //.setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));
		 Long blockSize = 100L * 1024L * 1024L; // 100 MB;
		 ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions()
				                                           .setBlockSizeLong(blockSize);

		 try {
			 fileClient.uploadWithResponse(new FileParallelUploadOptions(new ByteArrayInputStream(msg.getBytes()))
		         .setParallelTransferOptions(parallelTransferOptions).setHeaders(headers)
		         .setMetadata(metadata).setRequestConditions(requestConditions)
		         .setPermissions("permissions").setUmask("umask"), null, new Context("key", "value"));
		     System.out.println("Upload from file succeeded");
		 } catch (UncheckedIOException ex) {
		     System.err.printf("Failed to upload from file %s%n", ex.getMessage());
		 }

	}
}
