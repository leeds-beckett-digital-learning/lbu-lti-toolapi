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
public class CourseV2
{
  private final String id;
  private final String uuid;
  private final String externalId;
  private final String dataSourceId;
  private final String courseId;
  private final String name;
  private final String description;
  private final String parentId;

  @JsonCreator
  public CourseV2( @JsonProperty(value = "id",           required = true ) String id, 
                   @JsonProperty(value = "uuid",         required = false) String uuid, 
                   @JsonProperty(value = "externalId",   required = false) String externalId, 
                   @JsonProperty(value = "dataSourceId", required = false) String dataSourceId, 
                   @JsonProperty(value = "courseId",     required = true ) String courseId, 
                   @JsonProperty(value = "name",         required = true ) String name, 
                   @JsonProperty(value = "description",  required = false) String description,
                   @JsonProperty(value = "parentId",     required = false) String parentId)
  {
    this.id = id;
    this.uuid = uuid;
    this.externalId = externalId;
    this.dataSourceId = dataSourceId;
    this.courseId = courseId;
    this.name = name;
    this.description = description;
    this.parentId = parentId;
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
  public String getDataSourceId()
  {
    return dataSourceId;
  }

  @JsonProperty
  public String getCourseId()
  {
    return courseId;
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

  @JsonProperty
  public String getParentId()
  {
    return parentId;
  }  
}
