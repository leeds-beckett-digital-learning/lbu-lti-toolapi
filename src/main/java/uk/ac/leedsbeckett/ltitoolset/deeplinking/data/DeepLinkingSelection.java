/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.ac.leedsbeckett.ltitoolset.deeplinking.data;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a user's requirement for specifying a tool selection.
 * For use in websocket messages.
 * 
 * @author jon
 */
public class DeepLinkingSelection
{
  final String toolId;
  final String toolType;
  final String resourceTitle;
  final String resourceDescription;
  
  final String toolResourceId;

  /**
   * Construct object from properties.
   * 
   * @param toolId The ID of the tool
   * @param toolType The type of the tool
   * @param resourceTitle The title of the new resource
   * @param resourceDescription Description of the new resource
   * @param toolResourceId The ID of an existing resource
   */
  public DeepLinkingSelection( 
          @JsonProperty("toolId") String toolId, 
          @JsonProperty("toolType") String toolType, 
          @JsonProperty("toolResourceTitle") String resourceTitle,
          @JsonProperty("toolResourceDescription") String resourceDescription,
          @JsonProperty("toolResourceId") String toolResourceId
          )
  {
    this.toolId = toolId;
    this.toolType = toolType;
    this.resourceTitle = resourceTitle;
    this.resourceDescription = resourceDescription;
    this.toolResourceId = toolResourceId;
  }

  /**
   * Standard POJO getter
   * @return The ID.
   */
  public String getToolId()
  {
    return toolId;
  }

  /**
   * Standard POJO getter
   * @return The type.
   */
  public String getToolType()
  {
    return toolType;
  }

  /**
   * Standard POJO getter
   * @return The title for the resource.
   */
  public String getResourceTitle()
  {
    return resourceTitle;
  }

  /**
   * Standard POJO getter
   * @return The description
   */
  public String getResourceDescription()
  {
    return resourceDescription;
  }

  /**
   * Standard POJO getter
   * @return The ID of an existing resource.
   */
  public String getToolResourceId()
  {
    return toolResourceId;
  }
}
