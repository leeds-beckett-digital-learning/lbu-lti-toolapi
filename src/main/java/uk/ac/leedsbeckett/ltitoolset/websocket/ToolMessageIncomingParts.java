/*
 * Copyright 2025 maber01.
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.DecodeException;
import javax.websocket.EncodeException;
import javax.websocket.Session;
import uk.ac.leedsbeckett.ltitoolset.blobex.BlobExchanger;

/**
 *
 * @author maber01
 */
public class ToolMessageIncomingParts
{
  static final Logger logger = Logger.getLogger(ToolMessageIncomingParts.class.getName() );

  final ToolMessage toolMessage;
  final HashMap<String,BinaryPart> binaryPartMap = new HashMap<>();

  boolean valid=true;
  boolean haspayload=false;
  String text;
  long textTimestamp;
  
  // Constructor will read the header lines from this
  // and leave the pointer at the payload if there is one.
  // Payload will be read later after binary inclusions
  // have all been received.
  final BufferedReader reader;  


  
  public ToolMessageIncomingParts( Session session, String text ) throws DecodeException
  {
    toolMessage = new ToolMessage( session, text );
    this.text = text;
    textTimestamp = System.currentTimeMillis();
    reader = new BufferedReader( new StringReader( text ) );      
    
    try
    {
      String line = reader.readLine();
      if ( !ToolMessage.HEADER.equals( line ) )
      {
        valid = false;
        return;
      }
      
      while ( !haspayload && (line = reader.readLine()) != null )
      {
        int n = line.indexOf( ':' );
        if ( n<-1 ) throw new DecodeException( text, "Missing colon in header." );
        String name = line.substring( 0, n );
        String value = line.substring( n+1 );
        switch ( name )
        {
          case "id":
            toolMessage.setId( value );
            break;
          case "replytoid":
            toolMessage.setReplyToId( value );
            break;
          case "messagetype":
            toolMessage.setMessageType( value );
            break;
          case "payloadtype":
            toolMessage.setPayloadType( value );
            break;
          case "payload":
           haspayload = true;
            break;
          case "binaryinsert":
            try
            {
              BinaryPart bp = new BinaryPart( value );
              binaryPartMap.put( value, bp );
            }
            catch ( NumberFormatException nfe )
            {
              throw new DecodeException( text, "Non-numeric data in inclusion header." );
            }
            break;
        }
      }
      
      if ( haspayload )
      {
        String classname = toolMessage.getPayloadType();
        if ( classname == null )
          throw new DecodeException( text, "Unknown type of payload in message." );
        if ( !ToolMessageTypeSet.isAllowed( classname ) )
          throw new DecodeException( text, "Disallowed type of payload in message. " + classname );
      }
    }
    catch ( IOException ex )
    {
      logger.log( Level.SEVERE, "IOException occured while decoding payload in message.", ex );
      throw new DecodeException( text, "IOException occured while decoding payload in message." );
    }
  }

  public Collection<BinaryPart> getBinaryParts()
  {
    return binaryPartMap.values();
  }
  
  public boolean hasId( String placeholder )
  {
    return binaryPartMap.containsKey( placeholder );
  }
  
  public void parsePayload() throws DecodeException, IOException
  {
    if ( !haspayload )
      return;

    String classname = toolMessage.getPayloadType();
    if ( classname == null )
      throw new DecodeException( text, "Unknown type of payload in message." );
    if ( !ToolMessageTypeSet.isAllowed( classname ) )
      throw new DecodeException( text, "Disallowed type of payload in message. " + classname );
    Class<?> c;
    try { c = Class.forName( classname ); }
    catch ( ClassNotFoundException ex )
    {
      throw new DecodeException( text, "Unknown type of payload in message. " + classname );
    }

    ObjectMapper mapper = new ObjectMapper();
    SimpleModule module = new SimpleModule();
    module.addDeserializer( byte[].class, new ByteArrayDeserializer( binaryPartMap ) );
    mapper.registerModule( module );
    Object o = mapper.readValue( reader, c );
    if ( o == null )
      throw new DecodeException( text, "Unable to decode payload in message." );
    toolMessage.setPayload( o );
  }
  
  
  public ToolMessage getToolMessage()
  {
    return toolMessage;
  }

  public boolean isValid()
  {
    return valid;
  }
  
  
  public static void main( String[] args )
  {
    System.out.println( "Hello" );
    ByteBuffer bb = ByteBuffer.allocate( 4 );
    bb.putInt( -1 );
    bb.rewind();
    for ( int i=0; i<4; i++ )
      System.out.println( bb.get() );
    bb.rewind();
    System.out.println( bb.getInt() );
    bb.rewind();
    long x = (long)bb.getInt() & 0xffffffffL;
    System.out.println( Long.toHexString( x ) );
    
    int n = (int)x;
    System.out.println( n );    
    System.out.println( Integer.toHexString( n ) );
  }
}
