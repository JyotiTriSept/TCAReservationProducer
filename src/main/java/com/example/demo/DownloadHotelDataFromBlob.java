package com.example.demo;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;

public class DownloadHotelDataFromBlob {
	private static final String blobStorageContainerName = "ehub-poc-blob-storage";
	
	public static String downloadBlobData() {
		BlobServiceClient client = new BlobServiceClientBuilder()
			    .connectionString("DefaultEndpointsProtocol=https;AccountName=wohalgpmsdldevsa;AccountKey=iSh4zGYXanmJdEhCSv/Qg1h+GF37rsfZAwIzzo0nByAgg6itXlDVQHFVe2gf5vK+3l4eFvtPWaVNj2P4f0wQow==;EndpointSuffix=core.windows.net")
			    .buildClient();
		
		BlobContainerClient blobContainerClient = client.getBlobContainerClient(blobStorageContainerName);
		BlobClient blobClient = blobContainerClient.getBlobClient("HotelData_Latest.json");
		String hotelData = blobClient.downloadContent().toString();
		System.out.println("Hotel data successfully downloaded from conatiner: "+blobStorageContainerName);
		return hotelData;
	}
}
