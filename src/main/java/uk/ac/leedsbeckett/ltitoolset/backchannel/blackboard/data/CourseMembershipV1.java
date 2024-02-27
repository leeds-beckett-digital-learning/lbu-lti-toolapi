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
@JsonIgnoreProperties( ignoreUnknown = true )
public class CourseMembershipV1
{
  private final String id;
  private final String userId;
  private final String courseId;
  private final String childCourseId;
  private final String dataSourceId;
  private final String courseRoleId;

  /**
   * By default missing fields will be set to null by Jackson.
   * 
   * @param id The ID of the membership record.
   * @param userId The ID of the user
   * @param courseId The ID of the course which the child course belongs to. Could be null?
   * @param childCourseId The course that the user is a member of.
   * @param dataSourceId The dataSource that created the membership.
   * @param courseRoleId The role of the user in the course.
   */
  @JsonCreator
  public CourseMembershipV1( @JsonProperty(value = "id",            required = true)  String id, 
                             @JsonProperty(value = "userId",        required = false) String userId, 
                             @JsonProperty(value = "courseId",      required = false) String courseId, 
                             @JsonProperty(value = "childCourseId", required = false) String childCourseId, 
                             @JsonProperty(value = "dataSourceId",  required = false) String dataSourceId, 
                             @JsonProperty(value = "courseRoleId",  required = false) String courseRoleId )
  {
    this.id = id;
    this.userId = userId;
    this.courseId = courseId;
    this.childCourseId = childCourseId;
    this.dataSourceId = dataSourceId;
    this.courseRoleId = courseRoleId;
  }
  
  @JsonProperty
  public String getId()
  {
    return id;
  }

  @JsonProperty
  public String getUserId()
  {
    return userId;
  }

  @JsonProperty
  public String getCourseId()
  {
    return courseId;
  }

  @JsonProperty
  public String getChildCourseId()
  {
    return childCourseId;
  }

  @JsonProperty
  public String getDataSourceId()
  {
    return dataSourceId;
  }

  @JsonProperty
  public String getCourseRoleId()
  {
    return courseRoleId;
  }  
}
