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
import java.util.HashMap;
import java.util.logging.Level;
import javax.websocket.DecodeException;
import javax.websocket.EncodeException;
import static uk.ac.leedsbeckett.ltitoolset.websocket.ToolMessageDecoder.logger;

/**
 *
 * @author maber01
 */
public class ToolMessageIncomingParts
{
  final ToolMessage toolMessage;
  final HashMap<String,BinaryPart> binaryPartMap = new HashMap<>();

  boolean allPartsReceived = false;
  boolean valid=true;
  boolean haspayload=false;
  String text;
  long textTimestamp;
  
  // Constructor will read the header lines from this
  // and leave the pointer at the payload if there is one.
  // Payload will be read later after binary inclusions
  // have all been received.
  final BufferedReader reader;  


  
  public ToolMessageIncomingParts( String text ) throws DecodeException
  {
    toolMessage = new ToolMessage( text );
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
              BinaryPart bp = new BinaryPart( Integer.parseUnsignedInt( value ) );
              binaryPartMap.put( bp.getPlaceholder(), bp );
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
        if ( binaryPartMap.isEmpty() )
          allPartsReceived = true;
      }
      else
        allPartsReceived = true;
    }
    catch ( IOException ex )
    {
      logger.log( Level.SEVERE, "IOException occured while decoding payload in message.", ex );
      throw new DecodeException( text, "IOException occured while decoding payload in message." );
    }
  }

  
  public boolean hasPlaceholder( String placeholder )
  {
    return binaryPartMap.containsKey( placeholder );
  }
  
  /**
   * Offer a binary part to this message.It might belong to another message.
   * 
   * @param placeholder The placeholder for this binary part
   * @param data The data being communicated.
   */
  public void addBinary( String placeholder, byte[] data )
  {
    // Find expected binary part by placeholder
    BinaryPart bp = binaryPartMap.get( placeholder );
    // if none, this part is not for this message
    if ( bp == null )
      return;
    
    // Record the byte array
    bp.setRawData( data );
    
    // Have we got all the expected byte arrays for this
    // tool message now?
    for ( BinaryPart bpi : binaryPartMap.values() )
      if ( bpi.getRawData() == null )
        return; // No
    
    // Yes
    allPartsReceived = true;
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

  public boolean isAllPartsReceived()
  {
    return allPartsReceived;
  }

  public boolean isStale()
  {
    if ( allPartsReceived ) return true;
    long age = System.currentTimeMillis() - this.textTimestamp;
    return age > 1000 * 10;
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
