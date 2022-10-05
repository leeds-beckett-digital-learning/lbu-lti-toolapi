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

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import uk.ac.leedsbeckett.ltitoolset.util.TwoStringKey;


/**
 * A two part key that identifies resources based on the ID of the 
 * launching platform and that platform's own resource ID.
 * 
 * @author jon
 */
public class ResourceKey extends TwoStringKey implements Serializable
{
  /**
   * The standard constructor.
   * 
   * @param platformId Annotated to help with serialization as JSON.
   * @param resourceId Annotated to help with serialization as JSON.
   */
  public ResourceKey( 
          @JsonProperty("platformId") String platformId, 
          @JsonProperty("resourceId") String resourceId )
  {
    super( platformId, resourceId );
  }

  /**
   * Get the ID of the platform that created this identifier.
   * 
   * @return The platform ID.
   */
  public String getPlatformId()
  {
    return getA();
  }

  /**
   * Get the resource ID that the platform generated for this resource.
   * 
   * @return The resource ID.
   */
  public String getResourceId()
  {
    return getB();
  }
}
  
