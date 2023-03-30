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
@JsonIgnoreProperties({ "user", "created", "modified", "availability", 
                        "bypassCourseAvailabilityUntil", "lastAccessed" })
public class CourseMembershipV1
{
  private final String id;
  private final String userId;
  private final String courseId;
  private final String childCourseId;
  private final String dataSourceId;
  private final String courseRoleId;

  public CourseMembershipV1( 
          @JsonProperty( "id" )            String id, 
          @JsonProperty( "userId" )        String userId, 
          @JsonProperty( "courseId" )      String courseId, 
          @JsonProperty( "childCourseId" ) String childCourseId, 
          @JsonProperty( "dataSourceId" )  String dataSourceId, 
          @JsonProperty( "courseRoleId" )  String courseRoleId )
  {
    this.id = id;
    this.userId = userId;
    this.courseId = courseId;
    this.childCourseId = childCourseId;
    this.dataSourceId = dataSourceId;
    this.courseRoleId = courseRoleId;
  }

  public String getId()
  {
    return id;
  }

  public String getUserId()
  {
    return userId;
  }

  public String getCourseId()
  {
    return courseId;
  }

  public String getChildCourseId()
  {
    return childCourseId;
  }

  public String getDataSourceId()
  {
    return dataSourceId;
  }

  public String getCourseRoleId()
  {
    return courseRoleId;
  }
  
}
