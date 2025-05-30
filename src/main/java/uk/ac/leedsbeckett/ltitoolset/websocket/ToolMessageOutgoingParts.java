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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.DecodeException;

/**
 *
 * @author maber01
 */
public class ToolMessageOutgoingParts
{
  final ToolMessage toolMessage;
  HashMap<String,BinaryPart> binaryPartMap = null;
  final OutgoingToolMessageAnalysis analysis = new OutgoingToolMessageAnalysis();
  
  String intermediateJson;
  String payloadJson;
  
  String text;
  
  public ToolMessageOutgoingParts( ToolMessage toolMessage ) throws DecodeException, JsonProcessingException
  {
    this.toolMessage = toolMessage;
    
    // This run of serialization is only for compiling potential clashing Ids
    // and counting byte arrays.
    ObjectMapper analyticalMapper = new ObjectMapper();
    SimpleModule analyticalModule = new SimpleModule();
    analyticalModule.addSerializer( String.class, new AnalyticalStringSerializer( analysis ) );
    analyticalModule.addSerializer( byte[].class, new AnalyticalByteArraySerializer( analysis ) );
    analyticalMapper.registerModule( analyticalModule );
    intermediateJson = analyticalMapper.writerWithDefaultPrettyPrinter().writeValueAsString( toolMessage.payload );
    
    // If there are byte array inclusions rebuild the JSON
    // while inserting placeholders.
    if ( analysis.hasByteArrays() )
    {
      binaryPartMap = new HashMap<>();
      ObjectMapper mapper = new ObjectMapper();
      SimpleModule module = new SimpleModule();
      module.addSerializer( byte[].class, new ByteArraySerializer( binaryPartMap, analysis ) );
      mapper.registerModule( module );
      payloadJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString( toolMessage.payload );
    }
    else
    {
      // Otherwise, the earlier JSON is fine as is.
      payloadJson = intermediateJson;
    }
    
    // Now create whole text message by adding headers
    text = encodeText();
  }
  
  
  private String encodeText()
  {
    StringBuilder sb = new StringBuilder();
    sb.append( ToolMessage.HEADER );
    sb.append( "\n" );
    sb.append( "id:" );
    sb.append( toolMessage.getId() );
    sb.append( "\n" );
    if ( toolMessage.getReplyToId() != null )
    {
      sb.append( "replytoid:" );
      sb.append( toolMessage.getReplyToId() );
      sb.append( "\n" );
    }
    if ( toolMessage.isControl() )
      sb.append( "control:true\n" );
    sb.append( "messagetype:" );
    sb.append( toolMessage.getMessageType() );
    sb.append( "\n" );
    
    if ( toolMessage.getPayload() != null )
    {
      if ( this.hasBinaryParts() )
        for ( BinaryPart bp : binaryPartMap.values() )
        {
          sb.append( "binaryinsert:" );
          sb.append( bp.id );
          sb.append( "\n" );
        }
      sb.append( "payloadtype:" );
      sb.append( toolMessage.getPayloadType() );
      sb.append( "\n" );
      sb.append( "payload:\n" );
      sb.append( payloadJson );
    }
    
    return sb.toString();
  }

  public String getText()
  {
    return text;
  }

  public boolean hasBinaryParts()
  {
    return binaryPartMap != null && !binaryPartMap.isEmpty();
  }
  
  public Collection<BinaryPart> getBinaryParts()
  {
    return binaryPartMap.values();
  }
  
  private OutgoingToolMessageAnalysis getAnalysis()
  {
    return analysis;
  }

  private String getIntermediateJson()
  {
    return intermediateJson;
  }
  
  private String getPayloadJson()
  {
    return payloadJson;
  }
  
  public boolean hasPlaceholder( String placeholder )
  {
    return binaryPartMap.containsKey( placeholder );
  }
  
  
    
  public ToolMessage getToolMessage()
  {
    return toolMessage;
  }  
  
  
  public static void main( String[] args )
  {
    System.out.println( "Hello" );
    TestObject payload = new TestObject();
    ToolMessage toolMessage = new ToolMessage( null, TestObjectMessageName.Test, payload );
    
    try
    {
      ToolMessageOutgoingParts parts = new ToolMessageOutgoingParts( toolMessage );
      System.out.println( parts.getIntermediateJson() );
      OutgoingToolMessageAnalysis analysis = parts.getAnalysis();
      System.out.println( "Byte array count = " + analysis.getByteArrayCount() );
      for ( String id : analysis.getClashingIds() )
        System.out.println( "clashing ID " + id );
      System.out.println( parts.getPayloadJson() );
      System.out.println( "---------------" );
      System.out.println( parts.getText() );
      System.out.println( "---------------" );
      if ( parts.hasBinaryParts() )
      {
        for ( BinaryPart bp : parts.getBinaryParts() )
        {
          System.out.println( bp.id );
          for ( int i=0; i<bp.data.length; i++ )
            System.out.println( Integer.toHexString( Byte.toUnsignedInt( bp.data[i] ) ) );
          System.out.println( "---------------" );
          for ( int i=0; i<bp.data.length; i++ )
            System.out.println( Integer.toHexString( Byte.toUnsignedInt( bp.data[i] ) ) );
          System.out.println( "---------------" );
        }
      }
    }
    catch ( DecodeException ex )
    {
      Logger.getLogger( ToolMessageOutgoingParts.class.getName() ).log( Level.SEVERE, null, ex );
    }
    catch ( JsonProcessingException ex )
    {
      Logger.getLogger( ToolMessageOutgoingParts.class.getName() ).log( Level.SEVERE, null, ex );
    }
  }
}
