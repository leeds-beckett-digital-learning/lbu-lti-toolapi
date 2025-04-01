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

import uk.ac.leedsbeckett.ltitoolset.annotations.ToolProperties;
import uk.ac.leedsbeckett.ltitoolset.util.TwoStringKey;
import uk.ac.leedsbeckett.ltitoolset.annotations.ToolFacet;

/**
 * A key for tools based on LTI launch type and tool name passed
 * as parameters in launch.
 * 
 * @author maber01
 */
public class ToolFacetKey extends TwoStringKey
{
  /**
   * Construct key based on two strings.
   * 
   * @param toolId The ID of the tool.
   * @param facetId The ID of the mapping.
   */
  public ToolFacetKey( String toolId, String facetId )
  {
    super( toolId, facetId );
  } 
  
  /**
   * Construct based on a tool mapping.
   * 
   * @param props Properties of the tool.
   * @param facet A launch mapping for the tool.
   */
  public ToolFacetKey( ToolProperties props, ToolFacet facet )
  {
    super( props.id(), facet.id() );
  }
  
  /**
   * Getter for part A of key.
   * 
   * @return The ID of the tool.
   */
  public String getToolId() { return getA(); }
  
  /**
   * Getter for part B of key.
   * 
   * @return The ID of the mapping.
   */
  public String getFacetId() { return getB(); }
}
