package com.example.demo.service.impl;

import java.util.Arrays;
import java.util.LinkedHashMap;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.demo.exception.StoreLoginExceptionToADLS;
import com.example.demo.model.LoginModel;
import com.example.demo.service.TCAReservationsLoginService;

@Service
public class TCAReservationsLoginServiceImpl implements TCAReservationsLoginService{

	@Override
	public String getLoginToken() throws Exception {
		LoginModel loginReq = new LoginModel("AMR", "Y#QFgJKns%c3-yh-");
		RestTemplate restTemp = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
	      headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
	      headers.setContentType(MediaType.APPLICATION_JSON);
	      HttpEntity<LoginModel> entity = new HttpEntity<LoginModel>(loginReq,headers);
	      
	      ResponseEntity<Object> responseEntity = restTemp.exchange(
	         "https://cs-lab.amr.innsist.tca-ss.com/api/LoginAPI", HttpMethod.POST, entity, Object.class);
	      LinkedHashMap<String, Object> responseBody = (LinkedHashMap<String, Object>) responseEntity.getBody();
	      String accessToken = (String) responseBody.get("access_token");
	      
	      return accessToken;
	}

	@Override
	public void storeExceptionIntoBlob(Exception e) {
		StoreLoginExceptionToADLS.updateToLatestFolder(e.toString());
		StoreLoginExceptionToADLS.storingExceptionInArchiveLocation(e.toString(),"ReservationsLogin");		
	}

}
