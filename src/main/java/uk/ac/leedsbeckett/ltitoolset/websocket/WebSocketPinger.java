/*
 * Copyright 2024 maber01.
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
package uk.ac.leedsbeckett.ltitoolset.websocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.MessageHandler;
import javax.websocket.PongMessage;
import javax.websocket.Session;

/**
 *
 * @author maber01
 */
public class WebSocketPinger implements Runnable
{
  static final Logger logger = Logger.getLogger( WebSocketPinger.class.getName() );
  private static long serial = 0x10000000;
  
  ScheduledExecutorService executorService;

  final HashMap<String,PongHandler> map = new HashMap<>();
  
  public void startRefreshing()
  {
    executorService = Executors.newSingleThreadScheduledExecutor();
    executorService.scheduleAtFixedRate( this, 1, 66, TimeUnit.SECONDS );
  }
  
  public void stopRefreshing()
  {
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

  public void addSession( Session s )
  {
    logger.log(Level.FINE, "WebSocket adding session {0}", s.getId());
    synchronized ( map )
    {
      PongHandler ppr = new PongHandler();
      ppr.s = s;
      ppr.ping = 0;
      ppr.pong = 0;
      ppr.b = ByteBuffer.allocate( Long.BYTES * 2 );
      map.put( s.getId(), ppr );
      s.addMessageHandler( ppr );
    }
  }
  
  public void removeSession( Session s )
  {
    logger.log(Level.FINE, "WebSocket removing session {0}", s.getId());
    synchronized ( map )
    {
      map.remove( s.getId() );
    }    
  }
  
  
  /**
   * Runs repeatedly on schedule. Each time ping every open websocket peer.
   */
  @Override
  public void run()
  {
    synchronized ( map )
    {
      for ( PongHandler ppr : map.values() )
      {
        ppr.ping = 0;
        ppr.pong = 0;
        ppr.b.put( Long.toHexString( serial++ ).getBytes( StandardCharsets.UTF_8 ) );
        if ( ppr.s.isOpen() )
        {
          logger.log(Level.FINE, "WebSocket ping to session {0}", ppr.s.getId());
          try
          {
            ppr.s.getBasicRemote().sendPing( ppr.b );
            ppr.ping = System.currentTimeMillis();
          }
          catch ( IOException | IllegalArgumentException ex ) {}
        }
      }
    }
  }

  class PongHandler implements MessageHandler.Whole<PongMessage>
  {
    Session s;
    public ByteBuffer b;
    public long ping;
    public long pong;
    
    @Override
    public void onMessage( PongMessage message )
    {
      logger.fine( "WebSocket pong message received on session " + s.getId() );
    }
  }
}
