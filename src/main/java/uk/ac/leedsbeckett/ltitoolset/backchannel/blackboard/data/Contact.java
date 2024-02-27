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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 *
 * @author maber01
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Contact implements Serializable
{
  private final String email;
  private final String institutionEmail;

  @JsonCreator
  public Contact( @JsonProperty(value = "email",            required = true)  String email, 
                  @JsonProperty(value = "institutionEmail", required = false) String institutionEmail )
  {
    this.email            = email;
    this.institutionEmail = institutionEmail;
  }
  
  @JsonProperty
  public String getEmail()
  {
    return email;
  }

  @JsonProperty
  public String getInstitutionEmail()
  {
    return institutionEmail;
  }  
}
