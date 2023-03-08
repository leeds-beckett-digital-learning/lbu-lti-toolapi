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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Represents an authorization token obtained from an Authorization server
 * which can be used to access a platform directly (i.e. not via the users'
 * web browsers.
 * 
 * @author maber01
 */
public class AuthToken
{
  String token;
  long expires;
  String[] scope;

  public static AuthToken load( String json )
  {
    AuthToken instance = new AuthToken();
    
    try
    {
      ObjectMapper mapper = new ObjectMapper();
      JsonFactory factory = mapper.getFactory();
      JsonParser parser = factory.createParser( json );
      JsonNode node = mapper.readTree(parser);
      if ( node.isObject() )
      {
        instance.token = node.get( "access_token" ).asText();
      }    
    }
    catch ( Exception e )
    {
      
    }
    
    return instance;
  }

  private AuthToken()
  {
  }

  
  public String getToken()
  {
    return token;
  }

  public long getExpires()
  {
    return expires;
  }

  public boolean hasExpired()
  {
    return expires < System.currentTimeMillis();
  }
  
  public String[] getScope()
  {
    return scope;
  }  
}
