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

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.io.IOException;
import java.security.PrivateKey;
import java.util.Calendar;
import java.util.HashMap;
import java.util.logging.Level;
import uk.ac.leedsbeckett.lti.services.LtiServiceScope;

/**
 *
 * @author maber01
 */
public abstract class LtiBackchannel extends Backchannel
{
  final String authtokenurl;
  final String clientid;
  final String signingkeyid;
  final PrivateKey signingkey;
  
  private final HashMap<String,OAuth2Token> platformAuthTokenMap = new HashMap<>();
  
  public LtiBackchannel( 
          BackchannelKey key, 
          String authtokenurl, 
          String clientid,
          String signingkeyid,
          PrivateKey signingkey )
  {    
    this.authtokenurl = authtokenurl;
    this.clientid = clientid;
    this.signingkeyid = signingkeyid;
    this.signingkey = signingkey;
  }
  
  public synchronized OAuth2Token getPlatformAuthToken( LtiServiceScope scope ) throws IOException
  {
    OAuth2Token platformAuthToken = platformAuthTokenMap.get( scope.getSpecification() );
    if ( platformAuthToken != null )
      return platformAuthToken;
    
    // Build a JSON object and sign it
    Calendar c = Calendar.getInstance();
    c.setTimeInMillis( System.currentTimeMillis() );

    JwtBuilder jwtbuilder = Jwts.builder();
    jwtbuilder.setIssuer( "lbu-lti-tools" );
    jwtbuilder.setSubject( clientid );
    jwtbuilder.setHeaderParam( "kid", signingkeyid );
    jwtbuilder.setAudience( authtokenurl );
    jwtbuilder.setIssuedAt( c.getTime() );
    c.add( Calendar.MINUTE, 5 );
    jwtbuilder.setExpiration( c.getTime() );
    jwtbuilder.setId( "shouldberandom_" + Long.toHexString( System.currentTimeMillis() ) );
    jwtbuilder.signWith( signingkey, SignatureAlgorithm.RS256 );
    
    String jwtstring = jwtbuilder.compact();
    logger.log(Level.INFO, "Compacted             = {0}", jwtstring );
    
    // Call the the authorization server by backchannel using HTTP cli.ent.
    // POST to url using form encoding and send these parameters
    // grant_type = 'client_credentials'
    // client_assertion_type = 'urn:ietf:params:oauth:client-assertion-type:jwt-bearer'
    // client_assertion = jwtstring
    // scope = Hmmm...
    JsonResult jresult = postAuthTokenRequest( authtokenurl, scope.getSpecification(), jwtstring );

    if ( jresult.isSuccessful() )
    {
      platformAuthToken = (OAuth2Token)jresult.getResult();
      platformAuthTokenMap.put( scope.getSpecification(), platformAuthToken );
      return platformAuthToken;
    }
    throw new IOException( "Unable to fetch security token from the authorisation server.");
  }
}
