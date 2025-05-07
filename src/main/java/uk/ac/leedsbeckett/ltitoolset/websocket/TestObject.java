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

/**
 *
 * @author maber01
 */
public class TestObject
{
  String a = "A string.";
  int b = 1234;
  byte[] c = null;
  byte[] d = { 11, 22, 33, 44, 55 };
  String e = "binary_1";
  String f = null;

  public String getA()
  {
    return a;
  }

  public void setA( String a )
  {
    this.a = a;
  }

  public int getB()
  {
    return b;
  }

  public void setB( int b )
  {
    this.b = b;
  }

  public byte[] getC()
  {
    return c;
  }

  public void setC( byte[] c )
  {
    this.c = c;
  }

  public byte[] getD()
  {
    return d;
  }

  public void setD( byte[] d )
  {
    this.d = d;
  }

  public String getE()
  {
    return e;
  }

  public void setE( String e )
  {
    this.e = e;
  }

  public String getF()
  {
    return f;
  }

  public void setF( String f )
  {
    this.f = f;
  }
}
