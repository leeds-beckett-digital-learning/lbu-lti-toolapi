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
import java.util.Set;

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
    part.setRawData( value );
    // Use ByteBuffer to join a four byte ID and the source byte array
    ByteBuffer tagged = ByteBuffer.allocate( value.length + 4 + 4 );
    tagged.order( ByteOrder.LITTLE_ENDIAN );
    tagged.putInt( (int)part.getId() );
    tagged.putInt( value.length );
    tagged.put( value );
    // Match up the tagged byte array with the ID
    part.setTaggedData( tagged.array() );
    // Must not mess with the ByteBuffer now so don't save a reference to it.
    // Make a record of the binary data object so it can be sent in a 
    // binary message after the JSON has been sent.
    binaryPartMap.put( part.getPlaceholder(), part );
    gen.writeString( part.getPlaceholder() );
  }
  
}
