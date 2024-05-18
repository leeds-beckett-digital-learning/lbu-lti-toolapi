/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.ac.leedsbeckett.ltitoolset.resources;

import java.io.Serializable;
import uk.ac.leedsbeckett.lti.claims.LtiContextClaim;
import uk.ac.leedsbeckett.lti.claims.LtiResourceClaim;

/**
 * For our tools that can be instantiated multiple times this records information about
 * a particular instance. It's resourseID uniquely identifies the resource at launch time.
 * 
 * @author jon
 */
public class ToolResourceRecord implements Serializable
{
  String resourceId;
  String toolName;
  String toolType;
  
  LtiResourceClaim platformResource;         // Is null until first launch
  LtiResourceClaim platformLinkingResource;  // Null if not created via deep linking request
  LtiContextClaim platformContext;           // Should not be null

  public ToolResourceRecord( String resourceId )
  {
    this.resourceId = resourceId;
  }

  public String getResourceId()
  {
    return resourceId;
  }

  public String getToolName()
  {
    return toolName;
  }

  public void setToolName( String toolName )
  {
    this.toolName = toolName;
  }

  public String getToolType()
  {
    return toolType;
  }

  public void setToolType( String toolType )
  {
    this.toolType = toolType;
  }

  public LtiResourceClaim getPlatformResource()
  {
    return platformResource;
  }

  public void setPlatformResource( LtiResourceClaim platformResource )
  {
    this.platformResource = platformResource;
  }

  public LtiResourceClaim getPlatformLinkingResource()
  {
    return platformLinkingResource;
  }

  public void setPlatformLinkingResource( LtiResourceClaim platformLinkingResource )
  {
    this.platformLinkingResource = platformLinkingResource;
  }

  public LtiContextClaim getPlatformContext()
  {
    return platformContext;
  }

  public void setPlatformContext( LtiContextClaim platformContext )
  {
    this.platformContext = platformContext;
  }  
}
