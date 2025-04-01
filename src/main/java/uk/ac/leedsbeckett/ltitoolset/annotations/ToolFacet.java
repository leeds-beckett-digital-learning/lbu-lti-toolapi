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
package uk.ac.leedsbeckett.ltitoolset.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation maps a tool to an LTI type and a tool name.
 * 
 * @author maber01
 */
@Repeatable( ToolFacets.class )
@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.TYPE )
public @interface ToolFacet
{
  /**
   * ID of the mapping.
   * 
   * @return The name.
   */
  public String id();
  
  /**
   * The human readable title.
   * 
   * @return The title.
   */
  public String title();

  /**
   * The instantiation level of this mapping.
   * 
   * @return The selected level.
   */
  public ToolInstantiationLevel instantiationLevel();
  
  /**
   * The URI which the launch process should send the user to.
   * 
   * @return The URI.
   */
  public String launchURI();
}
