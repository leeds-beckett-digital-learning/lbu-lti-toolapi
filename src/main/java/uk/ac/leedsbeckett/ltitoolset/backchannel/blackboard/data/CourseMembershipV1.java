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
import java.io.Serializable;

/**
 *
 * @author maber01
 */
@JsonIgnoreProperties( ignoreUnknown = true )
public class CourseMembershipV1 implements Serializable
{
  @JsonProperty( "id" )            private final String id = null;
  @JsonProperty( "userId" )        private final String userId = null;
  @JsonProperty( "courseId" )      private final String courseId = null;
  @JsonProperty( "childCourseId" ) private final String childCourseId = null;
  @JsonProperty( "dataSourceId" )  private final String dataSourceId = null;
  @JsonProperty( "courseRoleId" )  private final String courseRoleId = null;

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
