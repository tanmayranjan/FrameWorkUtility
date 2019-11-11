
package org.sunbird;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.perf4j.StopWatch;
import org.perf4j.log4j.Log4JStopWatch;

public class Postman {
	
	private static HttpClient client;
	
	public static HttpClient getHttpClient() {
		if (client == null) {
			RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(300 * 1000).setSocketTimeout(300 * 1000).build();
			client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
		}
		return client;
	}
	
	public static String Post(Logger logger, String strApiUrl, String strApiBody, String strApiToken) {

        //String strApiUrl = strApiUrl;
        //strApiUrl = strApiUrl.replace("'", "");
        String userKey = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJ1WXhXdE4tZzRfMld5MG5PS1ZoaE5hU0gtM2lSSjdXU25ibFlwVVU0TFRrIn0.eyJqdGkiOiI3MTE2ZjE4NC0zODU4LTQ5NzMtYjIyMC0zMWMwN2Y3MWY1ZjQiLCJleHAiOjE1NDkzNzU4MzQsIm5iZiI6MCwiaWF0IjoxNTQ5MzMyNjM0LCJpc3MiOiJodHRwczovL2Rpa3NoYS5nb3YuaW4vYXV0aC9yZWFsbXMvc3VuYmlyZCIsImF1ZCI6ImFkbWluLWNsaSIsInN1YiI6IjBlMGI1Y2EyLWNkZDQtNDJlYi05YzkxLWY5MDllM2YxNzVmNyIsInR5cCI6IkJlYXJlciIsImF6cCI6ImFkbWluLWNsaSIsImF1dGhfdGltZSI6MCwic2Vzc2lvbl9zdGF0ZSI6ImI3NjlkNGQ3LTc4OTQtNDI4Zi1iNTk2LTUyMzI3MGZlNGY1NiIsImFjciI6IjEiLCJhbGxvd2VkLW9yaWdpbnMiOltdLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6e30sIm5hbWUiOiJEZW1vQ3JlYXRvcjIiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJkZW1vY3JlYXRvcjJAZWtzdGVwIiwiZ2l2ZW5fbmFtZSI6IkRlbW9DcmVhdG9yMiIsImVtYWlsIjoiZGVtb2NyZWF0b3IyQGVrc3RlcC5jb20ifQ.PZ0EEFSMc9iXAyZhHX45BeIQsr9-Z083c3dpAnZQ9TjAsEnnZCVQcnE5luKOdrIFLJ-RMApn6hSviMUc34sj1Ed4_x3R1dHHVyVv1fHVGIlyQ2hjFdfMApAT-5CGh6aK3C1tukOK-duUmbBLyCVnqMNrFIv7tFBV1O6sazzT_g11mcFEpIoEaanefNjM2j0VDfQG9DPJ2vUDEAifAEXFO9-8oZ3WawAw1KrJRFNFnMcsPaVQFxhfdkKyt_ykYjWtUeaELdCPWZtM_l8hzJVDMpN6NMvZCWAZ-uYolBPlTtTuWmNl_JCyWntuKgy3R_OrFyArynMbRiO-lzcD35cHHg";
        @SuppressWarnings("deprecation")
		DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(strApiUrl);
        httpPost.addHeader("Content-Type", "application/json");
        httpPost.addHeader("Authorization", "Bearer " + strApiToken);
        httpPost.addHeader("x-authenticated-user-token",userKey);
        System.out.println("inside post::"+strApiToken);
        

        StringEntity entity = new StringEntity(strApiBody, "UTF8");
        entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        httpPost.setEntity(entity);
        try {
            HttpResponse response = httpClient.execute(httpPost);
            System.out.println("response::"+response);
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
            }
            BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
            String output=null;
            while ((output = br.readLine()) != null) {
                System.out.println("response from post call::"+output);    
                return output;
            
            }
        } catch (ClientProtocolException e) {
            logger.warning(e.getMessage());
        } catch (IOException e) {
            logger.warning(e.getMessage());
        }

        return "";
    
    }
	public static String transceive(Logger logger, String strToken, String strUserToken, String strApiUrl, String strApiBody, String strChannelId) throws Exception
	{
		logger.info("In Postman --> transceive --> strApiUrl :: " + strApiUrl);
		logger.info("In Postman --> transceive --> strApiBody :: " + strApiBody);
		
		String strResponse=null;
		
        try {
        	StopWatch stopWatch = new Log4JStopWatch(strApiUrl);
        	
            HttpPost post = new HttpPost(strApiUrl);

            // Add API token:
    		post.setHeader("Authorization", "Bearer "+strToken);
            post.setHeader("Content-Type", "application/json; charset=UTF-8");
            post.setHeader("x-authenticated-user-token", strUserToken);
            post.setHeader("X-Channel-id", strChannelId);
            
            JSONParser parser = new JSONParser(); 
            JSONObject json = (JSONObject) parser.parse(strApiBody);
        	
            //setting json object to post request.
    		StringEntity entity = new StringEntity(json.toString(), "UTF8");
    		entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
    		post.setEntity(entity);
    		
    		//this is your response:
    		HttpResponse response = getHttpClient().execute(post);
    		
    		logger.info(stopWatch.stop());
    	
//    		logger.info("In Postman --> transceive --> Response Headers content-length:: " + response.getHeaders("content-length")[0]);
//    		logger.info("In Postman --> transceive --> Response Headers x-kong-proxy-latency:: " + response.getHeaders("x-kong-proxy-latency")[0]);
//    		logger.info("In Postman --> transceive --> Response Headers x-kong-upstream-latency:: " + response.getHeaders("x-kong-upstream-latency")[0]);
//    		logger.info("In Postman --> transceive --> Response Headers x-ratelimit-limit-hour:: " + response.getHeaders("x-ratelimit-limit-hour")[0]);
//    		logger.info("In Postman --> transceive --> Response Headers x-ratelimit-remaining-hour:: " + response.getHeaders("x-ratelimit-remaining-hour")[0]);
    		logger.info("In Postman --> transceive --> Response: " + response.getStatusLine());
    		
    		BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

    		String line = "";
    		while ((line = reader.readLine()) != null) {
             	strResponse = line;
             }
             reader.close();
             
             if(strResponse==null || strResponse.isEmpty())
             	strResponse = response.getStatusLine().getReasonPhrase();
    		
        } catch (Exception e) {
            logger.info("In Postman -->  transceive --> Exception occurred:: " + e.getMessage());
            throw e;
        }
        System.out.println("In Postman -->  transceive --> return Response:: " + strResponse);
        return strResponse;
	}
			
	

	public static String patch(Logger logger, String strToken, String strUserToken, String strApiUrl, String strApiBody, String strChannelId) throws Exception
	{
		logger.info("In Postman --> patch --> strApiUrl :: " + strApiUrl);
		logger.info("In Postman --> patch --> strApiBody :: " + strApiBody);
		String strResponse=null;
		
        try {
        	StopWatch stopWatch = new Log4JStopWatch(strApiUrl);
        	
        	
        	
    		HttpPatch patch = new HttpPatch(strApiUrl);

            // Add API token:
    		patch.setHeader("Authorization", "Bearer "+strToken);
//            patch.setHeader("Content-Type", "application/json; charset=utf-8");
            patch.setHeader("x-authenticated-user-token",strUserToken);
            patch.setHeader("X-Channel-id", strChannelId);
            
            JSONParser parser = new JSONParser();  
            JSONObject json = (JSONObject) parser.parse(strApiBody);
        	
            //setting json object to post request.
    		StringEntity entity = new StringEntity(json.toString(), "UTF8");
    		entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
    		patch.setEntity(entity);
    		
    		//this is your response:
    		HttpResponse response = getHttpClient().execute(patch);
    		logger.info(stopWatch.stop());
//    		logger.info("In Postman --> patch --> Response Headers content-length:: " + response.getHeaders("content-length")[0]);
//    		logger.info("In Postman --> patch --> Response Headers x-kong-proxy-latency:: " + response.getHeaders("x-kong-proxy-latency")[0]);
//    		logger.info("In Postman --> patch --> Response Headers x-kong-upstream-latency:: " + response.getHeaders("x-kong-upstream-latency")[0]);
//    		logger.info("In Postman --> patch --> Response Headers x-ratelimit-limit-hour:: " + response.getHeaders("x-ratelimit-limit-hour")[0]);
//    		logger.info("In Postman --> patch --> Response Headers x-ratelimit-remaining-hour:: " + response.getHeaders("x-ratelimit-remaining-hour")[0]);
    		logger.info("In Postman --> patch --> Response: " + response.getStatusLine());
    		
    		BufferedReader reader = new BufferedReader(
    		        new InputStreamReader(response.getEntity().getContent()));

    		String line = "";
    		while ((line = reader.readLine()) != null) {
             	strResponse = line;
             }
             reader.close();
             
             if(strResponse==null || strResponse.isEmpty())
             	strResponse = response.getStatusLine().getReasonPhrase();
    		
            
        } catch (Exception e) {
            logger.info("In Postman --> patch -->  Exception occurred:: " + e.getMessage());
            throw e;
        }

        return strResponse;
	}

	public static String transceive(Logger logger, String strApiUrl, String strApiBody, String strChannelId, String strToken) throws Exception
	{
		logger.info("In Postman --> transceive --> strApiUrl :: " + strApiUrl);
		logger.info("In Postman --> transceive --> strApiBody :: " + strApiBody);
		String strResponse=null;
		
        try {
        	StopWatch stopWatch = new Log4JStopWatch(strApiUrl);
        	
     		HttpPost post = new HttpPost(strApiUrl);

            // Add API token:
     		post.setHeader("Authorization", "Bearer "+strToken);
     		post.setHeader("Content-Type", "application/json; charset=UTF-8");
     		post.setHeader("X-Channel-id", strChannelId);
     		
            JSONParser parser = new JSONParser();  
            JSONObject json = (JSONObject) parser.parse(strApiBody);
        	
           //setting json object to post request.
    		StringEntity entity = new StringEntity(json.toString(), "UTF8");
    		entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
    		post.setEntity(entity);
    		
    		//this is your response:
    		HttpResponse response = getHttpClient().execute(post);
    		logger.info(stopWatch.stop());
//    		logger.info("In Postman --> transceive --> Response Headers content-length:: " + response.getHeaders("content-length")[0]);
//    		logger.info("In Postman --> transceive --> Response Headers x-kong-proxy-latency:: " + response.getHeaders("x-kong-proxy-latency")[0]);
//    		logger.info("In Postman --> transceive --> Response Headers x-kong-upstream-latency:: " + response.getHeaders("x-kong-upstream-latency")[0]);
//    		logger.info("In Postman --> transceive --> Response Headers x-ratelimit-limit-hour:: " + response.getHeaders("x-ratelimit-limit-hour")[0]);
//    		logger.info("In Postman --> transceive --> Response Headers x-ratelimit-remaining-hour:: " + response.getHeaders("x-ratelimit-remaining-hour")[0]);
    		logger.info("In Postman -->  transceive --> Response: " + response.getStatusLine());
    		
    		BufferedReader reader = new BufferedReader(
    		        new InputStreamReader(response.getEntity().getContent()));

    		String line = "";
    		while ((line = reader.readLine()) != null) {
             	strResponse = line;
             }
             reader.close();
             
             if(strResponse==null || strResponse.isEmpty())
             	strResponse = response.getStatusLine().getReasonPhrase();
    		
    		
        } catch (Exception e) {
            logger.info("In Postman -->  transceive --> Exception occurred:: " + e.getMessage());
            throw e;
        }

        return strResponse;
	}
			
	

	public static String patch(Logger logger, String strApiUrl, String strApiBody, String strToken) throws Exception
	{
		logger.info("In Postman --> patch --> strApiUrl :: " + strApiUrl);
		logger.info("In Postman --> patch --> strApiBody :: " + strApiBody);
		String strResponse=null;
		
        try {
        	StopWatch stopWatch = new Log4JStopWatch(strApiUrl);
        	
    		HttpPatch patch = new HttpPatch(strApiUrl);

            // Add API token:
            patch.setHeader("Content-Type", "application/json; charset=utf-8");
            patch.setHeader("Authorization", "Bearer " + strToken);
            
            JSONParser parser = new JSONParser();   
            JSONObject json = (JSONObject) parser.parse(strApiBody);
        	
            //setting json object to post request.
    		StringEntity entity = new StringEntity(json.toString(), "UTF8");
    		entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
    		patch.setEntity(entity);
    		
    		//this is your response:
    		HttpResponse response = getHttpClient().execute(patch);
    		logger.info(stopWatch.stop());
//    		logger.info("In Postman --> patch --> Response Headers content-length:: " + response.getHeaders("content-length")[0]);
//    		logger.info("In Postman --> patch --> Response Headers x-kong-proxy-latency:: " + response.getHeaders("x-kong-proxy-latency")[0]);
//    		logger.info("In Postman --> patch --> Response Headers x-kong-upstream-latency:: " + response.getHeaders("x-kong-upstream-latency")[0]);
//    		logger.info("In Postman --> patch --> Response Headers x-ratelimit-limit-hour:: " + response.getHeaders("x-ratelimit-limit-hour")[0]);
//    		logger.info("In Postman --> patch --> Response Headers x-ratelimit-remaining-hour:: " + response.getHeaders("x-ratelimit-remaining-hour")[0]);
    		logger.info("In Postman --> patch --> Response: " + response.getStatusLine());
    		
    		BufferedReader reader = new BufferedReader(
    		        new InputStreamReader(response.getEntity().getContent()));

    		String line = "";
    		while ((line = reader.readLine()) != null) {
             	strResponse = line;
             }
             reader.close();
             
             if(strResponse==null || strResponse.isEmpty())
             	strResponse = response.getStatusLine().getReasonPhrase();
    		
        } catch (Exception e) {
            logger.info("In Postman --> patch -->  Exception occurred:: " + e.getMessage());
            throw e;
        }

        return strResponse;
	}
	
	public static String getDetails(Logger logger, String strApiUrl, String strToken) throws Exception
	{
		logger.info("In Postman --> getDetails --> strApiUrl :: " + strApiUrl);
		String strResponse=null;
		
        try {
        	StopWatch stopWatch = new Log4JStopWatch(strApiUrl);
        	
      		HttpGet get = new HttpGet(strApiUrl);
           
    		// add request header
    		get.setHeader("Content-Type", "application/json; charset=utf-8");
    		get.setHeader("Authorization", "Bearer " + strToken);
//    		get.setHeader("user-id", "content-editor");
    		HttpResponse response = getHttpClient().execute(get);
    		logger.info(stopWatch.stop());
//    		logger.info("In Postman --> getDetails --> Response Headers content-length:: " + response.getHeaders("content-length")[0]);
//    		logger.info("In Postman --> getDetails --> Response Headers x-kong-proxy-latency:: " + response.getHeaders("x-kong-proxy-latency")[0]);
//    		logger.info("In Postman --> getDetails --> Response Headers x-kong-upstream-latency:: " + response.getHeaders("x-kong-upstream-latency")[0]);
//    		logger.info("In Postman --> getDetails --> Response Headers x-ratelimit-limit-hour:: " + response.getHeaders("x-ratelimit-limit-hour")[0]);
//    		logger.info("In Postman --> getDetails --> Response Headers x-ratelimit-remaining-hour:: " + response.getHeaders("x-ratelimit-remaining-hour")[0]);
    		logger.info("In Postman --> getDetails --> Response Code : " + response.getStatusLine().getStatusCode());

    		BufferedReader reader = new BufferedReader(
    			new InputStreamReader(response.getEntity().getContent()));
    		
            String line;
            
            while ((line = reader.readLine()) != null) {
            	strResponse = line;
            }
            reader.close();
            
            if(strResponse==null || strResponse.isEmpty())
            	strResponse = response.getStatusLine().getReasonPhrase();
            
        } catch (Exception e) {
        	logger.info("In Postman --> getDetails -->  Exception occurred:: " + e.getMessage());
            throw e;
        }

        return strResponse;
	}
	
	public static String getDetails(Logger logger, String strApiUrl) throws Exception
	{
		logger.info("In Postman --> getDetails --> strApiUrl :: " + strApiUrl);
		String strResponse=null;
		
        try {
        	StopWatch stopWatch = new Log4JStopWatch(strApiUrl);
        	
    		HttpGet get = new HttpGet(strApiUrl);
           
    		// add request header
    		get.setHeader("Content-Type", "application/json; charset=utf-8");
    		HttpResponse response = getHttpClient().execute(get);
    		logger.info(stopWatch.stop());
//    		logger.info("In Postman --> getDetails --> Response Headers content-length:: " + response.getHeaders("content-length")[0]);
//    		logger.info("In Postman --> getDetails --> Response Headers x-kong-proxy-latency:: " + response.getHeaders("x-kong-proxy-latency")[0]);
//    		logger.info("In Postman --> getDetails --> Response Headers x-kong-upstream-latency:: " + response.getHeaders("x-kong-upstream-latency")[0]);
//    		logger.info("In Postman --> getDetails --> Response Headers x-ratelimit-limit-hour:: " + response.getHeaders("x-ratelimit-limit-hour")[0]);
//    		logger.info("In Postman --> getDetails --> Response Headers x-ratelimit-remaining-hour:: " + response.getHeaders("x-ratelimit-remaining-hour")[0]);
    		logger.info("In Postman --> getDetails --> Response Code : " + response.getStatusLine().getStatusCode());

    		BufferedReader reader = new BufferedReader(
    			new InputStreamReader(response.getEntity().getContent()));
    		
            String line;
            
            while ((line = reader.readLine()) != null) {
            	strResponse = line;
            }
            reader.close();
            
            if(strResponse==null || strResponse.isEmpty())
            	strResponse = response.getStatusLine().getReasonPhrase();
            
        } catch (Exception e) {
        	logger.info("In Postman --> getDetails -->  Exception occurred:: " + e.getMessage());
            throw e;
        }

        return strResponse;
	}
	
	public static String getDetails(Logger logger, String strApiUrl, String strToken, String strUserToken) throws Exception
	{
		logger.info("In Postman --> getDetails --> strApiUrl :: " + strApiUrl);
		String strResponse=null;
		
        try {
        	StopWatch stopWatch = new Log4JStopWatch(strApiUrl);
        	
            HttpClient client = HttpClientBuilder.create().build();
    		HttpGet get = new HttpGet(strApiUrl);
           
    		// add request header
    		get.setHeader("Content-Type", "application/json; charset=utf-8");
    		get.setHeader("Authorization", "Bearer " + strToken);
//    		get.setHeader("user-id", "content-editor");
    		get.setHeader("x-authenticated-user-token", strUserToken);
    		
    		HttpResponse response = client.execute(get);
    		logger.info(stopWatch.stop());
    		logger.info("In Postman --> getDetails --> Response Code : " + response.getStatusLine().getStatusCode());

    		BufferedReader reader = new BufferedReader(
    			new InputStreamReader(response.getEntity().getContent()));
    		
            String line;
            
            while ((line = reader.readLine()) != null) {
            	strResponse = line;
            }
            reader.close();
            
            if(strResponse==null || strResponse.isEmpty())
            	strResponse = response.getStatusLine().getReasonPhrase();
            
        } catch (Exception e) {
        	logger.info("In Postman --> getDetails -->  Exception occurred:: " + e.getMessage());
            throw e;
        }

        return strResponse;
	}
	
	
}