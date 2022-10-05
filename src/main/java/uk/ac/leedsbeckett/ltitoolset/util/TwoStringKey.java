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
package uk.ac.leedsbeckett.ltitoolset.util;

import java.io.Serializable;

/**
 * A little utility that provides a key for hashmaps based on a pair of
 * strings.
 * 
 * @author maber01
 */
public class TwoStringKey implements Serializable
{
  private final String a;
  private final String b;
  
  public TwoStringKey( String a, String b )
  {
    assert( a != null && b != null );
    this.a = a;
    this.b = b;
  }

  public String getA()
  {
    return a;
  }

  public String getB()
  {
    return b;
  }
  
  @Override
  public int hashCode()
  {
    return a.hashCode() | b.hashCode();
  }

  @Override
  public String toString()
  {
    return a + " " + b;
  }

  @Override
  public boolean equals( Object obj )
  {
    if ( !(obj instanceof TwoStringKey) )
      return false;
    TwoStringKey other = (TwoStringKey)obj;
    return this.a.equals( other.a ) && this.b.equals( other.b );
  }  
}
