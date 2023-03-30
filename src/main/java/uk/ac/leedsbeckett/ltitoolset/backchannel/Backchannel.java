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
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.message.BasicNameValuePair;
import uk.ac.leedsbeckett.lti.services.data.ServiceStatus;
import uk.ac.leedsbeckett.lti.services.nrps.data.NrpsMembershipContainer;
import uk.ac.leedsbeckett.ltitoolset.backchannel.blackboard.OAuth2Error;





/**
 *
 * @author maber01
 */
public abstract class Backchannel
{
  static final Logger logger = Logger.getLogger(Backchannel.class.getName() );

  protected final HashSet<BackchannelOwner> owners = new HashSet<>();
  
  protected String httpsproxyurl = null;
  protected HttpRoutePlanner routePlanner = null;

  protected final ArrayList<OAuth2Token> tokenList = new ArrayList<>();
  
  
  public void addOwner( BackchannelOwner owner )
  {
    owners.add( owner );
  }
  
  public void removeOwner( BackchannelOwner owner )
  {
    owners.remove( owner );
  }
  
  public boolean isOwner( BackchannelOwner owner )
  {
    return owners.contains( owner );
  }
  
  public boolean hasOwners()
  {
    return !owners.isEmpty();
  }
  
  public void setHttpsProxyUrl( String url )
  {
    httpsproxyurl = url;
    if ( StringUtils.isBlank( httpsproxyurl ) )
      routePlanner = null;
    else
      try
      {
        HttpHost host = HttpHost.create( url );
        routePlanner = new DefaultProxyRoutePlanner( host );
      }
      catch ( Throwable th )
      {
        logger.log( Level.SEVERE, "Unable to set up route planner.", th );
      }
  }
  
  public JsonResult postAuthTokenRequest( 
          String url, 
          String assertion ) throws IOException
  {
    final HttpPost httpPost = new HttpPost( url );
    final List<NameValuePair> params = new ArrayList<>();
    params.add(new BasicNameValuePair("grant_type", "client_credentials" ));
    params.add(new BasicNameValuePair("client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer" ));
    params.add(new BasicNameValuePair("client_assertion", assertion ));
    params.add(new BasicNameValuePair("scope", "https://purl.imsglobal.org/spec/lti-nrps/scope/contextmembership.readonly" ));
    
    httpPost.setEntity( new UrlEncodedFormEntity( params ) );
    logger.log( Level.INFO, "Executing POST on {0}", url );
    HttpClientBuilder clientBuilder = HttpClients.custom();
    if ( routePlanner != null )
      clientBuilder = clientBuilder.setRoutePlanner( routePlanner );
    try (CloseableHttpClient client = clientBuilder.build();
        CloseableHttpResponse response = (CloseableHttpResponse) client
            .execute(httpPost))
    {
      return new JsonResult( 
              response,
              OAuth2Token.class,
              OAuth2Error.class );
    }
  }

  public JsonResult postBlackboardRestTokenRequest( 
          String url, 
          String uname, 
          String secret ) throws IOException
  {
    HttpHost host = HttpHost.create( url );
    
    final BasicCredentialsProvider provider = new BasicCredentialsProvider();
    
    UsernamePasswordCredentials creds = new UsernamePasswordCredentials( uname, secret );
    BasicScheme scheme = new BasicScheme();

    // Add AuthCache to the execution context
    final HttpClientContext context = HttpClientContext.create();    
    final HttpPost httpPost = new HttpPost( url );
    try { httpPost.addHeader( scheme.authenticate( creds, httpPost, context ) ); }
    catch ( AuthenticationException ex ) { throw new IOException( "Unable to set auth header.", ex ); }
    
    final List<NameValuePair> params = new ArrayList<NameValuePair>();
    params.add(new BasicNameValuePair("grant_type", "client_credentials" ));
    httpPost.setEntity(new UrlEncodedFormEntity(params));
    
    logger.log( Level.INFO, "Executing POST on {0}", url );
    HttpClientBuilder clientBuilder = HttpClients.custom();
    if ( routePlanner != null )
      clientBuilder = clientBuilder.setRoutePlanner( routePlanner );
    try (CloseableHttpClient client = clientBuilder.build();
        CloseableHttpResponse response = (CloseableHttpResponse) client
            .execute(httpPost,context))
    {
      return new JsonResult( 
              response,
              OAuth2Token.class,
              OAuth2Error.class );
    }
  }
  
  public JsonResult getBlackboardRest( 
          String url, 
          String token, 
          List<NameValuePair> params,
          Class<?> successClass,
          Class<?> failClass ) throws IOException
  {
    URI target;
    URIBuilder urib;
    try
    {
      urib = new URIBuilder( url );
      urib.addParameters( params );
      target = urib.build();
    }
    catch ( URISyntaxException ex )
    {
      throw new IOException( "Unable to build uri", ex );
    }
    

    final HttpGet httpGet = new HttpGet( target );
    httpGet.addHeader( "Authorization", "Bearer " + token );
    
    
    logger.log( Level.INFO, "Executing GET on {0}", target );
    HttpClientBuilder clientBuilder = HttpClients.custom();
    if ( routePlanner != null )
      clientBuilder = clientBuilder.setRoutePlanner( routePlanner );
    try (CloseableHttpClient client = clientBuilder.build();
        CloseableHttpResponse response = (CloseableHttpResponse) client
            .execute(httpGet))
    {
      return new JsonResult( 
              response,
              successClass,
              failClass );
    }
  }

  public JsonResult putBlackboardRest( 
          String url, 
          String token, 
          String data,
          Class<?> successClass,
          Class<?> failClass ) throws IOException
  {
    URI target;
    URIBuilder urib;
    try
    {
      urib = new URIBuilder( url );
      target = urib.build();
    }
    catch ( URISyntaxException ex )
    {
      throw new IOException( "Unable to build uri", ex );
    }
    

    final HttpPut httpPut = new HttpPut( target );
    httpPut.addHeader( "Authorization", "Bearer " + token );
    httpPut.setHeader("Accept", "application/json");
    httpPut.setHeader("Content-type", "application/json; charset=utf-8");
    StringEntity stringEntity = new StringEntity( data, StandardCharsets.UTF_8 );
    httpPut.setEntity( stringEntity );
    
    logger.log( Level.INFO, "Executing PUT on {0}", target );
    HttpClientBuilder clientBuilder = HttpClients.custom();
    if ( routePlanner != null )
      clientBuilder = clientBuilder.setRoutePlanner( routePlanner );
    try (CloseableHttpClient client = clientBuilder.build();
        CloseableHttpResponse response = (CloseableHttpResponse) client
            .execute( httpPut ))
    {
      return new JsonResult( 
              response,
              successClass,
              failClass );
    }
  }

  
  public JsonResult getNamesRoles( String url, String token ) throws IOException
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
      Header[] headers = response.getAllHeaders();
      logger.log(Level.FINE, "HTTP Response Headers:" );
      for ( Header h : headers )
      {
        logger.log(Level.FINE, "{0} = {1}", new Object[ ]{h.getName(), h.getValue()});
        for ( HeaderElement he : h.getElements() )
        {
          logger.log(Level.FINE, "Header element {0} = {1}", new Object[ ]{he.getName(), he.getValue()});
          for ( NameValuePair nvp : he.getParameters() )
            logger.log(Level.FINE, "       Parameter {0} = {1}", new Object[ ]{nvp.getName(), nvp.getValue()});
        }
      }
      logger.log(Level.FINE, "End of headers." );
      return new JsonResult( 
              response,
              NrpsMembershipContainer.class,
              ServiceStatus.class );
    }
    catch ( IOException iex )
    {
      logger.log( Level.SEVERE, "IO problem when fetching data from platform.", iex );
      return null;
    }
  }  
  
}
