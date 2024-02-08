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
  @JsonProperty( "id" )         private final String id = null;
  @JsonProperty( "uuid" )       private final String uuid = null;
  @JsonProperty( "externalId" ) private final String externalId = null;
  @JsonProperty( "name" )       private final Name name = null;
  @JsonProperty( "contact" )    private final Contact contact = null;

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

  public Name getName()
  {
    return name;
  }

  public Contact getContact()
  {
    return contact;
  }
}
