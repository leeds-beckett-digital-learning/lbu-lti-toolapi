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

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;

/**
 * A little utility that provides a key for hashmaps based on a pair of
 * strings. Subclasses must override the constructor.
 * 
 * @author maber01
 */
public class FourStringKey implements Serializable
{
  @JsonIgnore
  private final String a;
  
  @JsonIgnore
  private final String b;
  
  @JsonIgnore
  private final String c;
  
  @JsonIgnore
  private final String d;
  
  protected FourStringKey( String a, String b, String c, String d )
  {
    assert( a != null && b != null && c != null && d != null );
    this.a = a;
    this.b = b;
    this.c = c;
    this.d = d;
  }

  
  protected String getA()
  {
    return a;
  }

  protected String getB()
  {
    return b;
  }
  
  protected String getC()
  {
    return c;
  }
  
  protected String getD()
  {
    return d;
  }
  
  @Override
  public int hashCode()
  {
    return a.hashCode() | b.hashCode() | c.hashCode() | d.hashCode();
  }

  @Override
  public String toString()
  {
    return a + " " + b + " " + c + " " + d;
  }

  @Override
  public boolean equals( Object obj )
  {
    if ( obj.getClass() != this.getClass() )
      return false;
    FourStringKey other = (FourStringKey)obj;
    return this.a.equals( other.a ) && this.b.equals( other.b ) && this.c.equals( other.c ) && this.d.equals( other.d );
  }  
}
