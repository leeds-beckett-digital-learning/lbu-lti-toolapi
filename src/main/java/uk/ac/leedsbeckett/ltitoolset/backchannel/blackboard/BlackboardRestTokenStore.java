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
package uk.ac.leedsbeckett.ltitoolset.backchannel.blackboard;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.leedsbeckett.ltitoolset.backchannel.HttpClient;

/**
 *
 * @author maber01
 */
public class BlackboardRestTokenStore implements Runnable
{
  static final Logger logger = Logger.getLogger(BlackboardRestTokenStore.class.getName() );
  
  
  private final HashMap<String,PlatformTokenStore> map = new HashMap<>();
  
  private BlackboardConfiguration config;
  private Thread checker = null;
  private boolean stopping = false;
  
  public BlackboardRestTokenStore( BlackboardConfiguration config )
  {
    this.config = config;
  }
  
  public String getPlatformToken( String platform )
  {
    PlatformTokenStore store = null;
    
    synchronized ( map )
    {
      store = map.get( platform );
      if ( store == null )
      {
        store = new PlatformTokenStore( platform );
        map.put( platform, store );
      }
    }
    
    return store.getToken();
  }

  public void start()
  {
    checker = new Thread( this );
    checker.start();    
  }
  
  public void stop()
  {
    stopping = true;
    checker.interrupt();
  }
  
  /**
   * Load or refresh tokens for platforms periodically but taking care not
   * to interfere with foreground threads that are working on specific
   * platforms.
   */
  @Override
  public void run()
  {
    PlatformTokenStore[] stores;
    while ( !stopping )
    {
      // get copy
      synchronized ( map )
      {
        try
        {
          Thread.sleep( 60000 );
        }
        catch ( InterruptedException ex )
        {
          stopping = true;
          break;
        }
        stores = (PlatformTokenStore[])map.entrySet().toArray();
      }
      
      for ( PlatformTokenStore store : stores )
        if ( !store.foregroundupdate )
          store.update();
    }
  }
  
  
  
  private class PlatformTokenStore
  {
    boolean foregroundupdate=false;
    private final String platform;
    private String accessToken=null;
    private String tokenType=null;
    private String scope=null;
    private long expires=0L;

    public PlatformTokenStore( String platform )
    {
      this.platform = platform;
    }

    
    public String getToken()
    {
      synchronized( this )
      {
        foregroundupdate=true;
        try
        {
          update();
        }
        finally
        {
          foregroundupdate=false;
        }
        return accessToken;
      }
    }

    public synchronized void update()
    {
      try
      {
        long now = System.currentTimeMillis();
        if ( expires > now )
          return;

        String url = "https://" + platform + "/learn/api/public/v1/oauth2/token";
        String json = HttpClient.postBlackboardRestTokenRequest( url, config.getId(), config.getSecret() );
        
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = mapper.getFactory();
        JsonParser parser = factory.createParser( json );
        JsonNode node = mapper.readTree(parser);
        int expires_in = 0;
        if ( node.isObject() )
        {
          if ( node.has( "access_token" ) && node.get( "access_token" ).isTextual() )
            accessToken = node.get( "access_token" ).asText();
          if ( node.has( "token_type" ) && node.get( "token_type" ).isTextual() )
            tokenType = node.get( "token_type" ).asText();
          if ( node.has( "scope" ) && node.get( "scope" ).isTextual() )
            scope = node.get( "scope" ).asText();
          if ( node.has( "expires_in" ) && node.get( "expires_in" ).isInt() )
            expires_in = node.get( "expires_in" ).asInt();
        }
        if ( expires_in == 0 )
          expires_in = 60;
        expires = expires_in*1000 + System.currentTimeMillis();
      }
      catch ( Exception ex )
      {
        logger.log( Level.SEVERE, "Unable to fetch auth token for blackboard REST API.", ex );
      }
    }
    
  }
}
