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

import javax.servlet.ServletContext;
import uk.ac.leedsbeckett.lti.claims.LtiClaims;

/**
 * The interface that tools must implement.
 * 
 * @author maber01
 */
public abstract class Tool
{
  /**
   * All tool implementations are initalized with a ServletContext.
   * 
   * @param ctx The ServletContext
   */
  public abstract void init( ServletContext ctx );
  
  /**
   * Each tool must know how to create a ToolLaunchState using its preferred
   * subclass.
   * 
   * @return A tool launch state to place in the LTI state object.
   */
  public abstract ToolLaunchState supplyToolLaunchState();
  
  /**
   * Initialises the ToolLaunchState. Subclasses that override this method
   * should start by using super to call this method implementation first.
   * 
   * @param toolstate The tool state that needs to be initialised.
   * @param lticlaims Claims from the LTI launch process.
   * @param state General LTI state.
   */
  public void initToolLaunchState( ToolLaunchState toolstate, LtiClaims lticlaims, ToolSetLtiState state )
  {
    toolstate.setPersonId( state.getPersonId() );
    toolstate.setPersonName( state.getPersonName() );
    toolstate.setCourseId( lticlaims.getLtiContext().getId() );
    toolstate.setCourseTitle( lticlaims.getLtiContext().getLabel() );
    ResourceKey rk = new ResourceKey( state.getPlatformName(), lticlaims.getLtiResource().getId() );
    toolstate.setResourceKey( rk );    
  }
}
