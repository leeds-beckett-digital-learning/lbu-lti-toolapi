/*
 * Copyright 2022 Leeds Beckett University.
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import uk.ac.leedsbeckett.lti.config.ClientLtiConfigurationKey;
import uk.ac.leedsbeckett.lti.state.LtiState;

/**
 * This API's customised subclass of LtiState which can store additional
 * information which is specific to the kind of tools the API supports.
 * 
 * @author jon
 */
public class ToolSetLtiState extends LtiState implements Serializable
{
  /**
   * Data that relates to tool/facet/resource selection and launch.
   */
  private ToolLaunchState   toolLaunchState = null;
  
  private ToolFacetKey toolFacetKey = null;

  private String toolResourceId = null;
  
  /**
   * Constructor of this state must make sure the superclass constructor
   * is called.
   * 
   * @param clientKey The key to an LTI client.
   */
  public ToolSetLtiState( ClientLtiConfigurationKey clientKey )
  {
    super( clientKey );
  }

  /**
   * Get the course content launch state from within this LTI state.
   * 
   * @return The course content state.
   */
  public ToolLaunchState getToolLaunchState()
  {
    return toolLaunchState;
  }

  /**
   * Set the course content state.
   * 
   * @param launchState The course content state.
   */
  public void setToolLaunchState( ToolLaunchState launchState )
  {
    this.toolLaunchState = launchState;
  }

  public ToolFacetKey getToolFacetKey()
  {
    return toolFacetKey;
  }

  public void setToolFacetKey( ToolFacetKey toolKey )
  {
    this.toolFacetKey = toolKey;
  }

  public String getToolResourceId()
  {
    return toolResourceId;
  }

  public void setToolResourceId( String toolResourceId )
  {
    this.toolResourceId = toolResourceId;
  }

  @JsonIgnore
  public String getToolId()
  {
    if ( toolFacetKey == null ) return null;
    return toolFacetKey.getToolId();
  }

  @JsonIgnore
  public String getToolFacetId()
  {
    if ( toolFacetKey == null ) return null;
    return toolFacetKey.getFacetId();
  }
}
