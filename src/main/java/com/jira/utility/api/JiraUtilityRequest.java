package com.jira.utility.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;
import com.jira.utility.api.JiraUtilityResponse;

public class JiraUtilityRequest implements Callable<JiraUtilityResponse> {

		private HttpURLConnection con;
		private URL obj;
		private String response;

		private String url;
		private String username;
		private String password;

		public JiraUtilityRequest(String url, String username, String password) {
		    this.url = url;
		    this.username = username;
		    this.password = password;
		}

		@Override
		public JiraUtilityResponse call() {
		    try {
		        obj = new URL(url);
		        con = (HttpURLConnection) obj.openConnection();
		        String userCredentials = username + ":" + password;
		        String basicAuth = "Basic " + java.util.Base64.getEncoder().encodeToString(userCredentials.getBytes());
		        con.setRequestProperty ("Authorization", basicAuth);
		        con.setRequestMethod("GET");

		        int responseCode = con.getResponseCode();
		        if(responseCode == 200) {
		            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		            String inputLine;
		            StringBuffer stringBuffer = new StringBuffer();

		            while ((inputLine = in.readLine()) != null) {
		                stringBuffer.append(inputLine);
		            }
		            in.close();
		            response = stringBuffer.toString();
		            return new JiraUtilityResponse(responseCode, response);
		        }
		        else {
		            response = "{\"response\":\"some error occurred\"}";
		            return new JiraUtilityResponse(responseCode, response);
		        }

		    } catch (IOException e) {
		        response = "{\"output\":\"some error occurred\"}";
		        return new JiraUtilityResponse(404, response);
		    }
		}
		

}

