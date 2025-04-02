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

import uk.ac.leedsbeckett.ltitoolset.resources.PlatformResourceKey;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.websocket.server.ServerEndpoint;
import uk.ac.leedsbeckett.lti.claims.LtiClaims;
import uk.ac.leedsbeckett.lti.claims.LtiRoleClaims;
import uk.ac.leedsbeckett.lti.resourcelink.LtiResourceLinkIFrame;
import uk.ac.leedsbeckett.ltitoolset.annotations.ToolFacet;
import uk.ac.leedsbeckett.ltitoolset.deeplinking.data.ToolInformation;
import uk.ac.leedsbeckett.ltitoolset.annotations.ToolProperties;
import uk.ac.leedsbeckett.ltitoolset.config.PlatformConfiguration;
import uk.ac.leedsbeckett.ltitoolset.deeplinking.DeepLinkingLaunchState;
import uk.ac.leedsbeckett.ltitoolset.deeplinking.data.ToolFacetInformation;
import uk.ac.leedsbeckett.ltitoolset.websocket.ToolEndpoint;

/**
 * The class that tools must extend.
 * 
 * @author maber01
 */
public abstract class Tool
{
  static final Logger logger = Logger.getLogger( Tool.class.getName() );

  ToolProperties toolProperties;
  ToolFacet[] toolFacets;
  //ToolInformation toolInformation = new ToolInformation();

  
  /**
   * The constructor needs to scan annotations to fill in tool information.
   * Subclasses must not interfere with this.
   */
  public Tool()
  {
    ToolProperties[] allProperties = this.getClass().getAnnotationsByType( ToolProperties.class );
    if ( allProperties.length == 1 )
      toolProperties = allProperties[0];
    toolFacets = this.getClass().getAnnotationsByType( ToolFacet.class );
  }

  /**
   * All tool implementations are initalized with a ServletContext.
   * 
   * @param ctx The ServletContext
   */
  public abstract void init( ServletContext ctx );
  
  
  public String getTitle()
  {
    if ( toolProperties == null ) return "unknown";
    return toolProperties.title();
  }
  
  public String getDefaultFacetId()
  {
    if ( toolProperties == null ) return null;
    return toolProperties.defaultFacetId();
  }
  
  public ToolFacet[] getFacets()
  {
    return toolFacets;
  }
  
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
   * @param platformConfiguration The tool set configuration for the launching platform.
   * @param toolstate The tool state that needs to be initialised.
   * @param lticlaims Claims from the LTI launch process.
   * @param state General LTI state.
   */
  public void initToolLaunchState( PlatformConfiguration platformConfiguration, ToolLaunchState toolstate, LtiClaims lticlaims, ToolSetLtiState state )
  {
    toolstate.setToolFacetId( state.getToolFacetId() );
    toolstate.setPersonId( state.getPersonId() );
    toolstate.setPersonName( state.getPersonName() );
    toolstate.setCourseId( lticlaims.getLtiContext().getId() );
    toolstate.setCourseTitle( lticlaims.getLtiContext().getLabel() );
    if ( state.getPlatformName() != null )
    {
      toolstate.setPlatformId( state.getPlatformName() );
      if ( lticlaims.getLtiResource() != null )
      {
        PlatformResourceKey rk = new PlatformResourceKey( state.getPlatformName(), lticlaims.getLtiResource().getId() );
        toolstate.setPlatformResourceKey( rk );
      }
    }
    if ( state.getToolResourceId() != null )
      toolstate.setToolResourceId( state.getToolResourceId() );
    
    Annotation a = getEndpointClass().getAnnotation( ServerEndpoint.class );
    if ( a != null && a instanceof ServerEndpoint )
    {
      ServerEndpoint se = (ServerEndpoint)a;
      StringBuilder sb = new StringBuilder();
      sb.append( se.value() );
      sb.append( "?state_id=" );
      sb.append( state.getId() );
      // The nonce isn't added here because we don't know the right one yet.
      toolstate.setRelativeWebSocketUri( sb.toString() );
    }
    if ( state.getRoles().isInRole( LtiRoleClaims.SYSTEM_ADMINISTRATOR_ROLE ) )
    {
      if ( platformConfiguration.isUserPermittedToConfigure( state.getPersonId() ) )
        toolstate.setAllowedToConfigure( true );
    }
  }
  
  public abstract boolean allowDeepLink( String facetID, DeepLinkingLaunchState deepstate );
  
  public LtiResourceLinkIFrame getDeepLinkingIFrameOptions()
  {
    return null;
  }

  public boolean createToolResource( String toolResourceId, String facetId, DeepLinkingLaunchState dlls )
  {
    return false;
  }
  
  public abstract Class<? extends ToolEndpoint> getEndpointClass();
  
  /**
   * Does the tool need to use Blackboard REST API? If one or more tools in the
   * set do, then the tool coordinator should authenticate, get a token and
   * regularly update the token.
   * 
   * @return True is one or more tools need Blackboard REST API.
   */
  public boolean usesBlackboardRest()
  {
    return false;
  }

  
  public final ToolInformation getDeepLinkingToolInformation( DeepLinkingLaunchState deepstate )
  {
    ArrayList<ToolFacetInformation> allowedFacets = new ArrayList<>();
    
    // Check facets
    for ( ToolFacet tf : toolFacets )
    {
      logger.log( Level.INFO, "Tool ID {0}, facet ID {1}", new Object[] {getTitle(), tf.id()} );
      if ( allowDeepLink( tf.id(), deepstate ) )
      {
        logger.log( Level.INFO, "Allowed" );
        allowedFacets.add( new ToolFacetInformation( tf.id(), tf.title(), tf.instantiationLevel(), tf.launchURI() ) );
      }
    }
    
    return new ToolInformation( 
            toolProperties.id(), 
            toolProperties.title(),
            allowedFacets.toArray(ToolFacetInformation[]::new)
    );
  }
}
