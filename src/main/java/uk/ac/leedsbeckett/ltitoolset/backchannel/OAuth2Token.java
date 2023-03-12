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

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 * Represents an authorization token obtained from an Authorization server
 * which can be used to access a platform directly (i.e. not via the users'
 * web browsers.
 * 
 * @author maber01
 */
public class OAuth2Token implements Serializable
{
  final String accessToken;
  final String tokenType;
  final long expiresIn;
  final String refreshToken;
  final String scope=null;

  final long expires;
  
  public OAuth2Token( 
          @JsonProperty( "access_token" )  String accessToken, 
          @JsonProperty( "token_type" )    String tokenType, 
          @JsonProperty( "expires_in" )    long expiresIn, 
          @JsonProperty( "refresh_token" ) String refreshToken )
  {
    this.accessToken = accessToken;
    this.tokenType = tokenType;
    this.expiresIn = expiresIn;
    this.refreshToken = refreshToken;
    this.expires = System.currentTimeMillis() + this.expiresIn*1000;
  }

  public String getAccessToken()
  {
    return accessToken;
  }

  public String getTokenType()
  {
    return tokenType;
  }

  public long getExpiresIn()
  {
    return expiresIn;
  }

  public String getRefreshToken()
  {
    return refreshToken;
  }

  public String getScope()
  {
    return scope;
  }
  
  public boolean hasExpired()
  {
    return expires < System.currentTimeMillis();
  }  
}
