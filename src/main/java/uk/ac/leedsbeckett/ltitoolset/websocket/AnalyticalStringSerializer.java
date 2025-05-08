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

/**
 *
 * @author maber01
 */
public class AnalyticalStringSerializer extends JsonSerializer<String>
{
  final OutgoingToolMessageAnalysis analysis;

  public AnalyticalStringSerializer( OutgoingToolMessageAnalysis analysis )
  {
    this.analysis = analysis;
  }
  
  @Override
  public void serialize( String value, JsonGenerator gen, SerializerProvider serializers ) throws IOException
  {
    if ( value == null )
    {
      gen.writeNull();
      return;
    }
    // Send the string as output...
    gen.writeString( value );
    
    if ( !value.startsWith( "binary_" ) )
      return;
    for ( int i=7; i<value.length(); i++ )
    {
      int cp = value.codePointAt( i );
      if ( cp < 0x30 || cp > 0x39 )
        return;
    }
    // If reached here this non-byte array field might clash with
    // a binary placeholder so make a record of it.
    analysis.addClashingId( value );
  }
}
