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
@JsonIgnoreProperties({ "dataSourceId", "userName", "studentId", "educationLevel",
                        "gender", "pronouns", "birthDate", "created", "modified",
                        "lastLogin", "institutionRoleIds", "systemRoleIds",
                        "availability", "name", "job", "address", "locale",
                        "avatar", "pronounciation", "pronounciationAudio"
                      })
public class UserV1
{
  private final String id;
  private final String uuid;
  private final String externalId;
  private final Contact contact;

  public UserV1( 
          @JsonProperty( "id" )           String id, 
          @JsonProperty( "uuid" )         String uuid, 
          @JsonProperty( "externalId" )   String externalId, 
          @JsonProperty( "contact" )      Contact contact )
  {
    this.id = id;
    this.uuid = uuid;
    this.externalId = externalId;
    this.contact = contact;
  }

  public String getId()
  {
    return id;
  }

  public String getUuid()
  {
    return uuid;
  }

  public String getExternalId()
  {
    return externalId;
  }

  public Contact getContact()
  {
    return contact;
  }
}
