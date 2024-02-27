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
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 
 * @author maber01
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CourseMembershipV1Input
{
  private final String childCourseId;
  private final String dataSourceId;
  private final Availability availability;
  private final String courseRoleId;

  /**
   * Used to instantiate from Java code.
   * 
   * @param childCourseId The course the user should be in. (May not be a child)
   * @param dataSourceId The datasource to link to the new data object
   * @param availability The availability of the course to the new member.
   * @param courseRoleId The role of the member.
   */
  @JsonCreator
  public CourseMembershipV1Input( @JsonProperty(value = "childCourseId", required = true) String childCourseId, 
                                  @JsonProperty(value = "dataSourceId",  required = true) String dataSourceId, 
                                  @JsonProperty(value = "availability",  required = true) Availability availability, 
                                  @JsonProperty(value = "courseRoleId",  required = true) String courseRoleId )
  {
    this.childCourseId = childCourseId;
    this.dataSourceId = dataSourceId;
    this.availability = availability;
    this.courseRoleId = courseRoleId;
  }

  @JsonProperty
  @JsonInclude( JsonInclude.Include.NON_NULL )
  public String getChildCourseId()
  {
    return childCourseId;
  }

  @JsonProperty
  @JsonInclude( JsonInclude.Include.NON_NULL )
  public String getDataSourceId()
  {
    return dataSourceId;
  }

  @JsonProperty
  @JsonInclude( JsonInclude.Include.NON_NULL )
  public Availability getAvailability()
  {
    return availability;
  }

  @JsonProperty
  @JsonInclude( JsonInclude.Include.NON_NULL )
  public String getCourseRoleId()
  {
    return courseRoleId;
  }
}
