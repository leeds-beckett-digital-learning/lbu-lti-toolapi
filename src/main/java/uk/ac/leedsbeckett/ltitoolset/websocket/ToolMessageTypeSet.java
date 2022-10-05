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

import java.util.HashSet;

/**
 * A utility map of message Java classes.
 * Used internally to keep set of types that will be accepted in
 * incoming messages. Restriction is needed for security since client
 * could trigger instantiation of arbitary classes otherwise.
 * 
 * @author maber01
 */
public class ToolMessageTypeSet
{
  static HashSet<String> set = new HashSet<>();
  
  public static void addType( String classname )
  {
    set.add( classname );
  }
  
  public static boolean isAllowed( String classname )
  {
    return set.contains( classname );
  }
}
