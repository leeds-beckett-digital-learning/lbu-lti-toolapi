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

  /**
   * Construct object from properties.
   * 
   * @param toolId The ID of the tool
   * @param toolType The type of the tool
   * @param resourceTitle The title of the new resource
   */
  public DeepLinkingSelection( 
          @JsonProperty("toolId") String toolId, 
           @JsonProperty("toolType") String toolType, 
            @JsonProperty("toolResourceTitle") String resourceTitle )
  {
    this.toolId = toolId;
    this.toolType = toolType;
    this.resourceTitle = resourceTitle;
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
}
