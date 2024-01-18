/*
 * Copyright 2024 maber01.
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
package uk.ac.leedsbeckett.ltitoolset.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import uk.ac.leedsbeckett.ltitoolset.util.TwoStringKey;

/**
 *
 * @author maber01
 */
public class PlatformConfigurationKey extends TwoStringKey implements Serializable
{
  public static PlatformConfigurationKey byIssuerAndPlatformGuid( String issuer, String platformGuid )
  {
    return new PlatformConfigurationKey( issuer, "guid_" + platformGuid );
  }
  
  public static PlatformConfigurationKey byIssuerAndPlatformUrl( String issuer, String platformUrl )
  {
    return new PlatformConfigurationKey( issuer, "url_" + platformUrl );
  }
  
  /**
   * The standard constructor.
   * 
   * @param issuer The auth server that auths the platform
   * @param platformId Annotated to help with serialization as JSON.
   */
  public PlatformConfigurationKey( 
          @JsonProperty("issuer") String issuer,
          @JsonProperty("platformId") String platformId  )
  {
    super( issuer, platformId );
  }

  /**
   * Get the ID of the platform that created this identifier.
   * 
   * @return The platform ID.
   */
  public String getIssuer()
  {
    return getA();
  }

  /**
   * Get the ID of the platform that created this identifier.
   * 
   * @return The platform ID.
   */
  public String getPlatformId()
  {
    return getB();
  }

  
}
