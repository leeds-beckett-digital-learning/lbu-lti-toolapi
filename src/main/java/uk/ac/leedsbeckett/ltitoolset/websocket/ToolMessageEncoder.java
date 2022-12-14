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
package uk.ac.leedsbeckett.ltitoolset.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

/**
 * A utility for use with endpoints which encodes POJO messages as plain text 
 * using Jackson JSON processing.
 *
 * @author maber01
 */
public class ToolMessageEncoder implements Encoder.Text<ToolMessage>
{
  static final Logger logger = Logger.getLogger( ToolMessageEncoder.class.getName() );

  ObjectMapper mapper = new ObjectMapper();
  
  /**
   * Take and outgoing message and encode as plain text.
   * 
   * @param tm The message.
   * @return The raw text encoded message.
   * @throws EncodeException If the encoding goes wrong.
   */
  @Override
  public String encode( ToolMessage tm ) throws EncodeException
  {
    StringBuilder sb = new StringBuilder();
    sb.append( ToolMessage.HEADER );
    sb.append( "\n" );
    sb.append( "id:" );
    sb.append( tm.getId() );
    sb.append( "\n" );
    if ( tm.getReplyToId() != null )
    {
      sb.append( "replytoid:" );
      sb.append( tm.getReplyToId() );
      sb.append( "\n" );
    }
    sb.append( "messagetype:" );
    sb.append( tm.getMessageType() );
    sb.append( "\n" );
    
    if ( tm.getPayload() != null )
    {
      try
      {
        sb.append( "payloadtype:" );
        sb.append( tm.getPayloadType() );
        sb.append( "\n" );
        sb.append( "payload:\n" );
        sb.append( mapper.writerWithDefaultPrettyPrinter().writeValueAsString( tm.payload ) );
      }
      catch ( JsonProcessingException ex )
      {
        logger.log( Level.SEVERE, null, ex );
        throw new EncodeException( tm, "Unable to encode object. " + ex.getMessage() );
      }
    }
    
    return sb.toString();
  }

  /**
   * Init the decoder - does nothing.
   * @param config Ignored.
   */
  @Override
  public void init( EndpointConfig config )
  {
  }

  /**
   * Destroy the decoder - does nothing.
   */
  @Override
  public void destroy()
  {
  }
  
}
