package com.example.demo;

import java.io.ByteArrayOutputStream;

import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.file.datalake.DataLakeDirectoryClient;
import com.azure.storage.file.datalake.DataLakeFileClient;
import com.azure.storage.file.datalake.DataLakeFileSystemClient;
import com.azure.storage.file.datalake.DataLakeServiceClient;
import com.azure.storage.file.datalake.DataLakeServiceClientBuilder;

public class DownloadHotelDataFromADLS {
	private static final String DIRECTORYNAME="RAW";
	private static final String SUBDIRECTORYNAME="TCA-API";
	private static final String SUBDIRECTORYNAME1="Hotels";
	private static final String SUBDIRECTORYNAME2="LatestData";
	private static final String FILENAME="AMR_Hotels_Latest.json";
	private static final String ENDPOINT="https://" + "wohalgpmsdldevsa" + ".dfs.core.windows.net";
	
	public static String downloadHotelData() {
		DataLakeServiceClient getDataLakeServiceClient = GetDataLakeServiceClient();
		
		DataLakeFileSystemClient fileSystemClient = getDataLakeServiceClient
				.getFileSystemClient("woh-alg-pms-datalake-dev");
		
		DataLakeDirectoryClient subDirectoryClient = fileSystemClient.getDirectoryClient(DIRECTORYNAME)
                .getSubdirectoryClient(SUBDIRECTORYNAME)
                .getSubdirectoryClient(SUBDIRECTORYNAME1)
                .getSubdirectoryClient(SUBDIRECTORYNAME2);
		
		DataLakeFileClient fileClient = subDirectoryClient.getFileClient(FILENAME);
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		fileClient.read(byteArrayOutputStream);
		String hotelData = byteArrayOutputStream.toString();
		
		System.out.println("Hotel data successfully downloaded from conatiner: "+"woh-alg-pms-datalake-dev");
		return hotelData;
	}
	
	private static DataLakeServiceClient GetDataLakeServiceClient() {

		StorageSharedKeyCredential sharedKeyCredential = new StorageSharedKeyCredential(
				"wohalgpmsdldevsa", "iSh4zGYXanmJdEhCSv/Qg1h+GF37rsfZAwIzzo0nByAgg6itXlDVQHFVe2gf5vK+3l4eFvtPWaVNj2P4f0wQow==");

		DataLakeServiceClientBuilder builder = new DataLakeServiceClientBuilder();

		builder.credential(sharedKeyCredential);
		builder.endpoint(ENDPOINT);

		return builder.buildClient();
	}
}
