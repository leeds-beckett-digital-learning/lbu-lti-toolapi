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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author maber01
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GroupV2
{
  private final String id;
  private final String uuid;
  private final String externalId;
  private final String groupSetId;
  private final String name;
  private final String description;

  @JsonCreator
  public GroupV2( 
          @JsonProperty(value="id",          required = true  ) String id, 
          @JsonProperty(value="uuid",        required = true  ) String uuid, 
          @JsonProperty(value="externalId",  required = true  ) String externalId, 
          @JsonProperty(value="groupSetId",  required = false ) String groupSetId, 
          @JsonProperty(value="name",        required = true  ) String name, 
          @JsonProperty(value="description", required = false ) String description )
  {
    this.id = id;
    this.uuid = uuid;
    this.externalId = externalId;
    this.groupSetId = groupSetId;
    this.name = name;
    this.description = description;
  }

  @JsonProperty
  public String getId()
  {
    return id;
  }

  @JsonProperty
  public String getUuid()
  {
    return uuid;
  }

  @JsonProperty
  public String getExternalId()
  {
    return externalId;
  }

  @JsonProperty
  public String getGroupSetId()
  {
    return groupSetId;
  }

  @JsonProperty
  public String getName()
  {
    return name;
  }

  @JsonProperty
  public String getDescription()
  {
    return description;
  }
  
  @JsonIgnore
  public boolean isInGroupSet()
  {
    return !StringUtils.isBlank( groupSetId );
  }  
}
