/*
 * Copyright 2023 maber01.
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
package uk.ac.leedsbeckett.ltitoolset.backchannel.blackboard.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author maber01
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Name
{
  private final String given;
  private final String family;
  private final String preferredDisplayName;

  public Name( 
          @JsonProperty( value = "given",                required = true ) String given, 
          @JsonProperty( value = "family",               required = true ) String family, 
          @JsonProperty( value = "preferredDisplayName", required = true ) String preferredDisplayName )
  {
    this.given = given;
    this.family = family;
    this.preferredDisplayName = preferredDisplayName;
  }

  @JsonProperty
  public String getGiven()
  {
    return given;
  }

  @JsonProperty
  public String getFamily()
  {
    return family;
  }

  @JsonProperty
  public String getPreferredDisplayName()
  {
    return preferredDisplayName;
  }
}
