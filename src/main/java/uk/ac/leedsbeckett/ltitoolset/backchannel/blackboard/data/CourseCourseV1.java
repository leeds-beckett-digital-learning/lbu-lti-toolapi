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

/**
 *
 * @author maber01
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CourseCourseV1
{
  private final String id;
  private final String dataSourceId;
  private final String parentId;

  @JsonCreator
  public CourseCourseV1( @JsonProperty(value = "id",           required = true ) String id, 
                   @JsonProperty(value = "dataSourceId", required = false) String dataSourceId, 
                   @JsonProperty(value = "parentId",     required = false) String parentId)
  {
    this.id = id;
    this.dataSourceId = dataSourceId;
    this.parentId = parentId;
  }

  @JsonProperty
  public String getId()
  {
    return id;
  }

  @JsonProperty
  public String getDataSourceId()
  {
    return dataSourceId;
  }

  @JsonProperty
  public String getParentId()
  {
    return parentId;
  }  
}
