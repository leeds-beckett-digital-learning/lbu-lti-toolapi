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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;

/**
 * This serializer will put a placeholder string into the JSON value but
 will also add the actual ByteBuffer to a binaryPartMap so it can be sent to the
 peer separately.
 * 
 * @author maber01
 */
public class ByteArraySerializer extends JsonSerializer<byte[]>
{
  final Map<String,BinaryPart> binaryPartMap;
  final OutgoingToolMessageAnalysis analysis;
  
  public ByteArraySerializer( Map<String,BinaryPart> map, OutgoingToolMessageAnalysis analysis )
  {
    this.binaryPartMap = map;
    this.analysis = analysis;
  }

  @Override
  public void serialize( byte[] value, JsonGenerator gen, SerializerProvider serializers ) throws IOException
  {
    if ( value == null )
    {
      gen.writeNull();
      return;
    }
    // Create a binary part which has an ID that will give a placeholder
    // string that does not not clash with string literals in the JSON.
    BinaryPart part = new BinaryPart( analysis );
    part.setData( value );
    binaryPartMap.put( part.getId(), part );
    gen.writeString( part.getId() );
  }
  
}
