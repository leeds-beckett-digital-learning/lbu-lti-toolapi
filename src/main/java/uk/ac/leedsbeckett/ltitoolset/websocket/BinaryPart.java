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

import java.util.Set;

/**
 *
 * @author maber01
 */
public class BinaryPart
{
  static long nextId = 0L;
  
  public static long getNextId( OutgoingToolMessageAnalysis analysis )
  {
    do
    {
      nextId++;
      if ( nextId >= 0x100000000L )
        nextId = 0;
    }
    while ( analysis.containsClashingId( nextId ) );
    return nextId;
  }
  
  long id;
  long length;
  byte[] rawData;
  
  byte[] taggedData;

  final String placeholder;
  
  /**
   * For sending byte array to peer.
   * 
   * @param analysis Analysis object that includes potential clashing IDs
   */
  public BinaryPart( OutgoingToolMessageAnalysis analysis )
  {
    this.id = getNextId( analysis );
    this.placeholder = "binary_" + id;
  }
  
  /**
   * For receiving byte array from peer. Instantiated when the
   * text part of the message is received but before binary parts
   * have arrived.
   * 
   * @param id The id of the binary message
   */
  public BinaryPart( long id )
  {
    this.id = id;
    this.placeholder = "binary_" + id;
  }

  public long getId()
  {
    return id;
  }

  public String getPlaceholder()
  {
    return placeholder;
  }
  
  public byte[] getRawData()
  {
    return rawData;
  }

  public void setRawData( byte[] rawData )
  {
    this.rawData = rawData;
  }

  public byte[] getTaggedData()
  {
    return taggedData;
  }

  public void setTaggedData( byte[] taggedData )
  {
    this.taggedData = taggedData;
  }  
}
