/*
 * Copyright 2022 maber01.
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
package uk.ac.leedsbeckett.lti.toolset.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

/**
 *
 * @author maber01
 */
public class ToolMessageDecoder implements Decoder.Text<ToolMessage>
{
  static final Logger logger = Logger.getLogger( ToolMessageDecoder.class.getName() );
  
  ObjectMapper mapper = new ObjectMapper();

  @Override
  public ToolMessage decode( String s ) throws DecodeException
  {
    logger.log( Level.FINE, "Decoding tool message [{0}]", s );

    ToolMessage tm = new ToolMessage( s );
    if ( s == null )
    {
      tm.setValid( false );
      return tm;
    }
    
    boolean haspayload = false;
    try
    {
      BufferedReader reader = new BufferedReader( new StringReader( s ) );      
      String line = reader.readLine();
      if ( !ToolMessage.HEADER.equals( line ) )
      {
        tm.setValid( false );
        return tm;
      }
      
      while ( !haspayload && (line = reader.readLine()) != null )
      {
        int n = line.indexOf( ':' );
        if ( n<-1 ) throw new DecodeException( s, "Missing colon in header." );
        String name = line.substring( 0, n );
        String value = line.substring( n+1 );
        switch ( name )
        {
          case "id":
            tm.setId( value );
            break;
          case "replytoid":
            tm.setReplyToId( value );
            break;
          case "messagetype":
            tm.setMessageType( value );
            break;
          case "payloadtype":
            tm.setPayloadType( value );
            break;
          case "payload":
            haspayload = true;
        }
      }
      
      if ( haspayload )
      {
        String classname = tm.getPayloadType();
        if ( classname == null )
          throw new DecodeException( s, "Unknown type of payload in message." );
        if ( !ToolMessageTypeSet.isAllowed( classname ) )
          throw new DecodeException( s, "Disallowed type of payload in message." );
        Class<?> c;
        try { c = Class.forName( classname ); }
        catch ( ClassNotFoundException ex )
        {
          throw new DecodeException( s, "Unknown type of payload in message." );
        }
        Object o = mapper.readValue( reader, c );
        if ( o == null )
          throw new DecodeException( s, "Unable to decode payload in message." );
        tm.setPayload( o );
      }
    }
    catch ( IOException ex )
    {
      logger.log( Level.SEVERE, "IOException occured while decoding payload in message.", ex );
      throw new DecodeException( s, "IOException occured while decoding payload in message." );
    }
    
    return tm;
  }

  @Override
  public boolean willDecode( String s )
  {
    return true;
  }

  @Override
  public void init( EndpointConfig config )
  {
  }

  @Override
  public void destroy()
  {
  }
  
}
