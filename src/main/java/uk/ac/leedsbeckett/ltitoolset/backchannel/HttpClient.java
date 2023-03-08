/*
 * Copyright 2023 maber01.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.leedsbeckett.ltitoolset.backchannel;


import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.conn.DefaultRoutePlanner;
import org.apache.http.message.BasicNameValuePair;





/**
 *
 * @author maber01
 */
public class HttpClient
{
  static final Logger logger = Logger.getLogger( HttpClient.class.getName() );

  static String httpsproxyurl = null;
  static HttpRoutePlanner routePlanner = null;
          
  public static void setHttpsProxyUrl( String url )
  {
    httpsproxyurl = url;
    if ( StringUtils.isBlank( httpsproxyurl ) )
    {
      logger.log(Level.INFO, "No route planner - no proxy." );
      routePlanner = null;
      return;
    }
    
    try
    {
      logger.log(Level.INFO, "Setting up a route planner." );
      HttpHost host = HttpHost.create( url );
      logger.log(Level.INFO, "Scheme {0}", host.getSchemeName() );
      logger.log(Level.INFO, "Host   {0}", host.getHostName() );
      logger.log(Level.INFO, "Port   {0}", host.getPort() );
      routePlanner = new DefaultProxyRoutePlanner( host );
    }
    catch ( Throwable th )
    {
      logger.log( Level.SEVERE, "Unable to set up route planner.", th );
    }
  }
  
  public static String postAuthTokenRequest( String url, String assertion ) throws IOException
  {
    final HttpPost httpPost = new HttpPost( url );
    final List<NameValuePair> params = new ArrayList<NameValuePair>();
    params.add(new BasicNameValuePair("grant_type", "client_credentials" ));
    params.add(new BasicNameValuePair("client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer" ));
    params.add(new BasicNameValuePair("client_assertion", assertion ));
    params.add(new BasicNameValuePair("scope", "https://purl.imsglobal.org/spec/lti-nrps/scope/contextmembership.readonly" ));
    
    httpPost.setEntity(new UrlEncodedFormEntity(params));

    logger.log( Level.INFO, "Executing POST on {0}", url );
    HttpClientBuilder clientBuilder = HttpClients.custom();
    if ( routePlanner != null )
      clientBuilder = clientBuilder.setRoutePlanner( routePlanner );
    try (CloseableHttpClient client = clientBuilder.build();
        CloseableHttpResponse response = (CloseableHttpResponse) client
            .execute(httpPost))
    {
        final int statusCode = response.getStatusLine().getStatusCode();
        logger.log( Level.INFO, "Rxed status code {0}", statusCode );
        logger.log( Level.INFO, "Rxed reason      {0}", response.getStatusLine().getReasonPhrase() );
        logger.log( Level.INFO, "Content type = {0}", response.getEntity().getContentType() );
        String value = IOUtils.toString( response.getEntity().getContent(), "ASCII" );
        logger.log( Level.INFO, "Server returned {0}", value );
        if ( statusCode != HttpStatus.SC_OK )
          return null;
        return value;
    }
  }
  
  public static String getNamesRoles( String url, String token ) throws IOException
  {
    String bearer = "Bearer " + token;
    logger.log( Level.INFO, "Authorization header set to {0}", bearer );
    final HttpGet httpGet = new HttpGet( url );
    httpGet.addHeader( "Accept", "application/vnd.ims.lti-nrps.v2.membershipcontainer+json" );
    httpGet.addHeader( "Authorization", bearer );
    logger.log( Level.INFO, "Executing POST on {0}", url );
    HttpClientBuilder clientBuilder = HttpClients.custom();
    if ( routePlanner != null )
      clientBuilder = clientBuilder.setRoutePlanner( routePlanner );
    try (CloseableHttpClient client = clientBuilder.build();
        CloseableHttpResponse response = (CloseableHttpResponse) client
            .execute(httpGet))
    {
        final int statusCode = response.getStatusLine().getStatusCode();
        logger.log( Level.INFO, "Rxed status code {0}", statusCode );
        logger.log( Level.INFO, "Rxed reason      {0}", response.getStatusLine().getReasonPhrase() );
        logger.log( Level.INFO, "Content type = {0}", response.getEntity().getContentType() );
        String value = IOUtils.toString( response.getEntity().getContent(), "ASCII" );
        logger.log( Level.INFO, "Server returned {0}", value );
        if ( statusCode != HttpStatus.SC_OK )
          return null;
        return value;
    }
    catch ( IOException iex )
    {
      logger.log( Level.SEVERE, "IO problem when fetching data from platform.", iex );
      return null;
    }
  }  
  
}
