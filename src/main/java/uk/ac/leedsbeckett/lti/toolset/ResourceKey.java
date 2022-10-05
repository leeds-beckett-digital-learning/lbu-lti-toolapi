/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.ac.leedsbeckett.lti.toolset;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import uk.ac.leedsbeckett.lti.util.TwoStringKey;

/**
 *
 * @author jon
 */
  public class ResourceKey extends TwoStringKey implements Serializable
  {
    public ResourceKey( 
            @JsonProperty("platformId") String platformId, 
            @JsonProperty("resourceId") String resourceId )
    {
      super( platformId, resourceId );
    }
    
    public String getPlatformId()
    {
      return getA();
    }

    public String getResourceId()
    {
      return getB();
    }
  }
  
