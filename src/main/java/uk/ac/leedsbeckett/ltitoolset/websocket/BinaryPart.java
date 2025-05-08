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
  
  public static String getNextId( OutgoingToolMessageAnalysis analysis )
  {
    String strId;
    do
    {
      nextId++;
      if ( nextId >= 0x100000000L )
        nextId = 0;
      strId = "binary_" + nextId;
    }
    while ( analysis.containsClashingId( strId ) );
    return strId;
  }
  
  String id;
  byte[] data;
    
  /**
   * For sending byte array to peer.
   * 
   * @param analysis Analysis object that includes potential clashing IDs
   */
  public BinaryPart( OutgoingToolMessageAnalysis analysis )
  {
    this.id = getNextId( analysis );
  }
  
  /**
   * For receiving byte array from peer.Instantiated when the
 text part of the message is received but before binary parts
 have arrived.
   * 
   * @param id The id of the binary message
   */
  public BinaryPart( String id )
  {
    this.id = id;
  }

  public String getId()
  {
    return id;
  }

  public byte[] getData()
  {
    return data;
  }

  public void setData( byte[] data )
  {
    this.data = data;
  }
}
