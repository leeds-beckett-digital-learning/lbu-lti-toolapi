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
public class UserV1
{
  private final String id;
  private final String uuid;
  private final String externalId;
  private final Name name;
  private final Contact contact;
  private final String[] institutionRoleIds;

  public UserV1(  @JsonProperty( value="id",         required=true  ) String id,
                  @JsonProperty( value="uuid",       required=true  ) String uuid,
                  @JsonProperty( value="externalId", required=false ) String externalId,
                  @JsonProperty( value="name",       required=false ) Name name,
                  @JsonProperty( value="contact",    required=false ) Contact contact,
                  @JsonProperty( value="institutionRoleIds", required=false ) String[] institutionRoleIds )
  {
    this.id = id;
    this.uuid = uuid;
    this.externalId = externalId;
    this.name = name;
    this.contact = contact;
    this.institutionRoleIds = institutionRoleIds;
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
  public Name getName()
  {
    return name;
  }

  @JsonProperty
  public Contact getContact()
  {
    return contact;
  }

  @JsonProperty
  public String[] getInstitutionRoleIds()
  {
    return institutionRoleIds;
  }
}
