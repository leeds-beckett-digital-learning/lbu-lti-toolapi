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
package uk.ac.leedsbeckett.ltitoolset.websocket;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author maber01
 */
public interface ToolMessageName
{
  static final Logger logger = Logger.getLogger( ToolMessageName.class.getName() );  
  @SuppressWarnings( "unchecked" )
  public static Class<? extends ToolMessageName> forName( String name )
  {
    try
    {
      Class<?> c = Class.forName( name );
      return (Class<? extends ToolMessageName>)c;
    }
    catch ( ClassNotFoundException ex )
    {
      logger.log( Level.SEVERE, "Requested class was not found", ex );
    }
    catch ( Exception e )
    {
      logger.log( Level.SEVERE, "Could not cast class to Class<? extends ToolMessageName>", e );      
    }
    return null;
  }
  
  public String getName();
  public Class<?> getPayloadClass();
  
}
