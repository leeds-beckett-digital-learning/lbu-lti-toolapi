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

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author maber01
 */
public class ByteArrayDeserializer extends JsonDeserializer<byte[]>
{
  static final Logger logger = Logger.getLogger(ByteArrayDeserializer.class.getName() );
  
  final Map<String,BinaryPart> binaryPartMap;

  public ByteArrayDeserializer( Map<String, BinaryPart> binaryPartMap )
  {
    this.binaryPartMap = binaryPartMap;
  }

  @Override
  public byte[] deserialize( JsonParser p, DeserializationContext ctxt ) throws IOException, JacksonException
  {
    logger.log(Level.INFO, "Deserializing {0}", p.currentValue());
    if ( p.currentValue() != null )
      logger.log(Level.INFO, "Deserializing class {0}", p.currentValue().getClass());
    String s = ctxt.readValue( p, String.class );
    logger.log(Level.INFO, "Deserializing {0}", s );
    if ( s != null )
    {
      BinaryPart bp = binaryPartMap.get( s );
      if ( bp != null )
        return bp.getRawData();
    }
    return null;
  }  
}
