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
package uk.ac.leedsbeckett.ltitoolset.jwks;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Key;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.leedsbeckett.lti.jwks.Jwk;
import uk.ac.leedsbeckett.lti.jwks.Jwks;
import uk.ac.leedsbeckett.lti.jwks.JwksSigningKeyResolver;
import uk.ac.leedsbeckett.ltitoolset.backchannel.JwksBackchannel;
import uk.ac.leedsbeckett.ltitoolset.store.Store;

/**
 * Fetches JWKS from trusted sources, caches them. If instantiated, must
 * be shut down so its scheduler is closed.
 * 
 * @author maber01
 */
public class JwksStore extends Store<String,JwksEntry> implements Runnable, JwksSigningKeyResolver
{
  static final Logger logger = Logger.getLogger( JwksStore.class.getName() );

  Path basepath;
  JwksBackchannel jwksbc;
  
  ScheduledExecutorService executorService;

  private final ArrayList<String> uriList = new ArrayList<>();
  private int currentRefresh = 0;
  
  ObjectMapper mapper = new ObjectMapper();
  
  
  public JwksStore( Path basepath, JwksBackchannel jwksbc )
  {
    super( "jwks" );
    this.basepath = basepath;
    this.jwksbc = jwksbc;
  }

  public void updateUriList()
  {
    synchronized( uriList )
    {
      File[] files = basepath.toFile().listFiles();
      uriList.clear();
      for ( File f : files )
      {
        if ( !f.exists() ) continue;
        if ( !f.isFile() ) continue;
        if ( !f.getName().endsWith(  "json" ) ) continue;
        if ( !f.getName().startsWith(  "http" ) ) continue;
        String url = URLDecoder.decode( f.getName(), StandardCharsets.UTF_8 );
        addUri( url );
      }
    }    
  }

  public void registerUri( String uri )
  {
    // Force creation of file with no keys in it
    get( uri, true );
    // Refresh it now
    refreshAnother( uri );
  }
  
  private void addUri( String uri )
  {
    synchronized( uriList )
    {
      uriList.add( uri );
    }
  }

  public void startRefreshing()
  {
    executorService = Executors.newSingleThreadScheduledExecutor();
    executorService.scheduleAtFixedRate( this, 1, 5, TimeUnit.MINUTES );
  }
  
  public void stopRefreshing()
  {
    logger.fine( "Stopping the scheduled JWKS refresher." );
    executorService.shutdown();
    try
    {
      if (!executorService.awaitTermination(1000, TimeUnit.MILLISECONDS))
        executorService.shutdownNow();
    }
    catch ( InterruptedException e )
    {
        executorService.shutdownNow();
    }    
  }
  
  
  private synchronized void refreshAnother( String uri )
  {
    logger.log(Level.FINE, "Refreshing {0}", uri);
    
    try
    {
      String s = jwksbc.resolveJwks( uri );
      logger.fine( s );
      Jwks jwks = mapper.readValue( s, Jwks.class );
      JwksEntry entry = this.get( uri, true );
      boolean needsupdate = false;
      Jwks currentjwks = entry.getJwks();
      if ( currentjwks != null )
        for ( Jwk jwk : jwks.getKeys() )
          if ( currentjwks.getKey( jwk.getKid() ) == null )
          {
            needsupdate = true;
            break;
          }

      if ( currentjwks == null || needsupdate )
      {
        entry.setTimeStamp( System.currentTimeMillis() );
        entry.setJwks( jwks );
        update( entry );
      }
    }
    catch ( IOException ex )
    {
      logger.log(Level.SEVERE, "Unable to fetch JWKS.", ex );
    }    
  }
  
  
  @Override
  public void run()
  {
    logger.fine( "Running store refresher." );
    updateUriList();
    int size = uriList.size();
    for ( int n=0; n<size; n++ )
    {
      String uri;
      synchronized( uriList )
      {
        if ( currentRefresh >= uriList.size() )
          currentRefresh = 0;
        uri = uriList.get( n );
      }
      if ( uri != null )
        refreshAnother( uri );
      try
      {
        Thread.sleep( 2000 );
      }
      catch ( InterruptedException ex )
      {
        logger.log(Level.FINE, "JWKS fetcher thread interrupted." );
        return;
      }
    }
  }
  
  
  
  @Override
  public JwksEntry create( String key )
  {
    return new JwksEntry( key );
  }

  @Override
  public Class<JwksEntry> getEntryClass()
  {
    return JwksEntry.class;
  }

  @Override
  public Path getPath( String key )
  {
    return basepath.resolve( URLEncoder.encode( key, StandardCharsets.UTF_8 ) );
  }  

  @JsonIgnore
  public Jwk getJwk( String uri, String kid ) throws IOException
  {
    JwksEntry entry = get( uri, false );
    if ( entry == null ) return null;
    if ( entry.getJwks() == null ) return null;
    return entry.getJwks().getKey( kid );
  }

  @Override
  public Key resolveSigningKey( String jwksUrl, String kid )
  {
    try
    {
      Jwk jwk = getJwk( jwksUrl, kid );
      if ( jwk == null )
        return null;
      return jwk.getKey();
    }
    catch ( IOException ex )
    {
      logger.log( Level.SEVERE, null, ex );
      return null;
    }
  }
}
