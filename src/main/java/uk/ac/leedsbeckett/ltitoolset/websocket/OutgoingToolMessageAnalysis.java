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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author maber01
 */
public class OutgoingToolMessageAnalysis
{
  private HashSet<Long> clashingIds = null;
  private int byteArrayCount=0;

  public boolean hasClashingIds()
  {
    return clashingIds != null && !clashingIds.isEmpty();
  }
  public Set<Long> getClashingIds()
  {
    return clashingIds;
  }

  public void addClashingId( long id )
  {
    if ( clashingIds == null )
      clashingIds = new HashSet<>();
    clashingIds.add( id );
  }
  
  public boolean containsClashingId( long id )
  {
    return clashingIds != null && clashingIds.contains( id );
  }
  
  public void incrementByteArrayCount()
  {
    byteArrayCount++;
  }
  
  public int getByteArrayCount()
  {
    return byteArrayCount;
  }
  
  public boolean hasByteArrays()
  {
    return byteArrayCount != 0;
  }
}
