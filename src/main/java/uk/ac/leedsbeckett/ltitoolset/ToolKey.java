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
package uk.ac.leedsbeckett.ltitoolset;

import uk.ac.leedsbeckett.ltitoolset.annotations.ToolMapping;
import uk.ac.leedsbeckett.ltitoolset.util.TwoStringKey;

/**
 * A key for tools based on LTI launch type and tool name passed
 * as parameters in launch.
 * 
 * @author maber01
 */
public class ToolKey extends TwoStringKey
{
  /**
   * Construct key based on two strings.
   * 
   * @param type The type of the tool - e.g. placement type.
   * @param id The name of the tool.
   */
  public ToolKey( String type, String id )
  {
    super( type, id );
  } 
  
  /**
   * Construct based on a tool mapping.
   * 
   * @param mapping A mapping that encapsulates type and name.
   */
  public ToolKey( ToolMapping mapping )
  {
    super( mapping.type(), mapping.id() );
  }
  
  /**
   * Getter for the type value.
   * 
   * @return Type of LTI launch - e.g. placement type.
   */
  public String getType() { return getA(); }
  
  /**
   * Getter for the name value.
   * 
   * @return Name of the tool.
   */
  public String getId() { return getB(); }
}
