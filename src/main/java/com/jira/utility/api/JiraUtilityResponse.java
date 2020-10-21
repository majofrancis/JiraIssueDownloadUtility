package com.jira.utility.api;

public class JiraUtilityResponse {

private String responseBody;
private int responseCode;

public JiraUtilityResponse(int responseCode, String responseBody) {
    this.responseBody = responseBody;
    this.responseCode = responseCode;
}

public int getResponseCode() {
    return responseCode;
}

public String getResponseBody() {
    return responseBody;
}
}
