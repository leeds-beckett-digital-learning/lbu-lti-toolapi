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
public enum TestObjectMessageName implements ToolMessageName
{
  Test( "Test", TestObject.class );

  /**
   * Each constant has a name which can be used in encoded messages passing
   * through the web socket.
   */
  private final String name;
  
  /**
   * The class of the payload for this message. Note that multiple
   * message names can use the same payload class.
   */
  private final Class payloadClass;

  /**
   * Simple constructor. Not public - only used to create the defined
   * constants.
   * 
   * @param name The name.
   * @param payloadClass The payload class.
   */
  TestObjectMessageName( String name, Class payloadClass )
  {
    this.name = name;
    this.payloadClass = payloadClass;
  }

  /**
   * Get the name of the message.
   * 
   * @return The name.
   */
  @Override
  public String getName()
  {
    return name;
  }

  /**
   * Get the payload class for this message.
   * 
   * @return The payload class.
   */
  @Override
  public Class getPayloadClass()
  {
    return payloadClass;
  }  
}
