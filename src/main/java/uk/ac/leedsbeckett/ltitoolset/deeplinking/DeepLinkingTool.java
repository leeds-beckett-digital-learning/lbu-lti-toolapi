/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.ac.leedsbeckett.ltitoolset.deeplinking;

import javax.servlet.ServletContext;
import uk.ac.leedsbeckett.lti.claims.LtiClaims;
import uk.ac.leedsbeckett.lti.claims.LtiDeepLinkingSettings;
import uk.ac.leedsbeckett.ltitoolset.Tool;
import uk.ac.leedsbeckett.ltitoolset.ToolLaunchState;
import uk.ac.leedsbeckett.ltitoolset.ToolSetLtiState;
import uk.ac.leedsbeckett.ltitoolset.config.PlatformConfiguration;
import uk.ac.leedsbeckett.ltitoolset.websocket.ToolEndpoint;

/**
 * A subclass of Tool for deep linking but is not annotated as a tool for launching as a
 * resource. Used when processing a deep linking request message as distinct from a launch
 * message.
 * 
 * @author jon
 */
public class DeepLinkingTool extends Tool
{
  @Override
  public void init( ServletContext ctx )
  {
  }

  @Override
  public ToolLaunchState supplyToolLaunchState()
  {
    return new DeepLinkingLaunchState();
  }

  @Override
  public boolean allowDeepLink( DeepLinkingLaunchState deepstate )
  {
    return false;
  }

  @Override
  public Class<? extends ToolEndpoint> getEndpointClass()
  {
    return DeepLinkingEndpoint.class;
  }  

  @Override
  public void initToolLaunchState( PlatformConfiguration platformConfiguration, ToolLaunchState toolstate, LtiClaims lticlaims, ToolSetLtiState state )
  {
    super.initToolLaunchState( platformConfiguration, toolstate, lticlaims, state );
    LtiDeepLinkingSettings deepsettings = lticlaims.getLtideeplinkingsettings();
    DeepLinkingLaunchState deepstate = (DeepLinkingLaunchState)toolstate;
    deepstate.deepLinkReturnUrl = deepsettings.getDeepLinkReturnUrl();
    deepstate.platform_issuer   = lticlaims.getIssuer();
    deepstate.data              = deepsettings.getData();
    deepstate.deployment_id     = lticlaims.get( "https://purl.imsglobal.org/spec/lti/claim/deployment_id" );    
    deepstate.rc                = lticlaims.getLtiRoles();
  }
}
