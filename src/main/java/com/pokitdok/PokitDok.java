/**	
	Client access library to the PokitDok APIs.
*/

package com.pokitdok;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class PokitDok {
  private String clientId;
  private String clientSecret;
  private String accessToken;
  private JSONParser parser;

  private String API_URL = "http://localhost:5002";

  public PokitDok(String clientId, String clientSecret) throws Exception {
  	this.clientId 		= clientId;
  	this.clientSecret = clientSecret;

    connect();
    parser = new JSONParser();
  }

  private void connect() throws Exception {
    HttpPost request = new HttpPost(API_URL + "/oauth2/token");
    List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
    urlParameters.add(new BasicNameValuePair("grant_type", "client_credentials"));
    request.setEntity(new UrlEncodedFormEntity(urlParameters)); 

    String auth = clientId + ":" + clientSecret;
    byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
    String authHeader = "Basic " + new String(encodedAuth);
    request.setHeader(HttpHeaders.AUTHORIZATION, authHeader);

    CloseableHttpClient client = HttpClients.createDefault();
    HttpResponse response = client.execute(request);
    accessToken = EntityUtils.toString(response.getEntity());
    client.close();
  }

  private Map executeAndParse(HttpRequestBase request) throws IOException {
    request.setHeader(HttpHeaders.AUTHORIZATION, this.accessToken);
    CloseableHttpClient client = HttpClients.createDefault();
    HttpResponse response = client.execute(request);

    Map parsedResponse = null;
    try {
      parsedResponse = (JSONObject) parser.parse(EntityUtils.toString(response.getEntity()));
    }
    catch (ParseException pe) {
      System.out.println("Error while parsing response: " + pe.toString());
    }
    finally {
      client.close();      
    }

    return parsedResponse;
  }

  private Map get(String url, Map params) throws IOException {
    HttpGet getRequest = new HttpGet(API_URL + url);
    
    return executeAndParse(getRequest);
  }

  private Map post(String url, Map params) throws IOException {
    HttpPost postRequest = new HttpPost(API_URL + url);
    
    return executeAndParse(postRequest); 
  }

  /** Invokes the activities endpoint, with a HashMap of parameters. */
  public Map activities(Map params) throws IOException { 
    return get("activities/", params);
  }

  /** Invokes the cash prices endpoint, with a HashMap of parameters. */
  public Map cashPrices(Map params) throws IOException {
    return get("prices/cash", params);
  }
   
  /** Invokes the insurance prices endpoint, with a HashMap of parameters. */
  public Map insurancePrices(Map params) throws IOException { 
    return get("prices/insurance", params);
  }

  /** Invokes the claims endpoint, with a HashMap of parameters. */
  public Map claims(Map params) throws IOException {
    return post("claims/", params);
  }

  /** Invokes the claim status endpoint, with a HashMap of parameters. */
  public Map claimStatus(Map params) throws IOException {
    return post("claims/status", params);
  }

  /** Invokes the eligibility endpoint, with a HashMap of parameters. */
  public Map eligibility(Map params) throws IOException {
    return post("eligibility/", params);
  }

  /** Invokes the enrollment endpoint, with a HashMap of parameters. */
  public Map enrollment(Map params) throws IOException { 
    return post("enrollment", params);
  }

  /** 
    Uploads an EDI file to the files endpoint.
    
    @param trading_partner_id the trading partner to transmit to
    
    @param filename the path to the file to transmit
  */
  public Map files(String tradingPartnerId, String filename) throws IOException {
    return new HashMap();
  }

  /** Invokes the payers endpoint, with a HashMap of parameters. */
  public Map payers(Map params) throws IOException {
    return get("payers", params);
  }

  /** Invokes the providers endpoint, with a HashMap of parameters. */
  public Map providers(Map params) throws IOException {
    return get("providers", params);
  }
}
