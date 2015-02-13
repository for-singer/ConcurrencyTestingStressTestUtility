package com.oshurpik.component;

import java.io.IOException;
import static org.apache.http.HttpHeaders.USER_AGENT;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

public class HttpRequestHelper {
    public static HttpResponse sendRequest(String url) { 
	HttpClient client = HttpClientBuilder.create().build();
	HttpGet request = new HttpGet(url);
 
	request.addHeader("User-Agent", USER_AGENT);
        HttpResponse response = null;
        try {
            response = client.execute(request);
        }
        catch(IOException ex) {
            ex.printStackTrace();
        }
 
        return response;
    }
}
