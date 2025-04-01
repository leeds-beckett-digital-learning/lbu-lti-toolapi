/*
 * Copyright 2022 Leeds Beckett University.
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

package uk.ac.leedsbeckett.ltitoolset;

import uk.ac.leedsbeckett.ltitoolset.resources.PlatformResourceKey;
import java.io.Serializable;

/**
 * This contains data that a user of the platform-wide tool might need.
 * Implementations for different tools will vary. Objects of this type are
 * set in the LTI Launch State.
 * 
 * @author jon
 */
public class ToolLaunchState implements Serializable
{
  /**
   * Many users (many states) may reference the same platform resource. It is 
   * important that it doesn't hold a reference to the resource object. So, it 
   * holds a unique key to the resource. The resources themselves are
   * put in a different cache.
   */
  private PlatformResourceKey platformResourceKey;
  private String toolResourceId;
  
  private String personId;
  private String personName;
  private String courseId;
  private String courseTitle;
  private String relativeWebSocketUri;
  protected boolean allowedToConfigure = false;

  public PlatformResourceKey getPlatformResourceKey()
  {
    return platformResourceKey;
  }

  public void setPlatformResourceKey( PlatformResourceKey platformResourceKey )
  {
    this.platformResourceKey = platformResourceKey;
  }

  public String getToolResourceId()
  {
    return toolResourceId;
  }

  public void setToolResourceId( String toolResourceId )
  {
    this.toolResourceId = toolResourceId;
  }

  
  
  public String getPersonId()
  {
    return personId;
  }

  public void setPersonId( String personId )
  {
    this.personId = personId;
  }

  public String getPersonName()
  {
    return personName;
  }

  public void setPersonName( String personName )
  {
    this.personName = personName;
  }

  public String getCourseId()
  {
    return courseId;
  }

  public void setCourseId( String courseId )
  {
    this.courseId = courseId;
  }

  public String getCourseTitle()
  {
    return courseTitle;
  }

  public void setCourseTitle( String courseTitle )
  {
    this.courseTitle = courseTitle;
  }

  public String getRelativeWebSocketUri()
  {
    return relativeWebSocketUri;
  }

  public void setRelativeWebSocketUri( String relativeWebSocketUri )
  {
    this.relativeWebSocketUri = relativeWebSocketUri;
  }

  /**
   * Is the user connected to this state object allowed to configure the tool?
   *
   * @return Is allowed?
   */
  public boolean isAllowedToConfigure()
  {
    return allowedToConfigure;
  }

  /**
   * Simple setter.
   *
   * @param allowedToConfigure Is the user allowed to configure the tool.
   */
  public void setAllowedToConfigure( boolean allowedToConfigure )
  {
    this.allowedToConfigure = allowedToConfigure;
  }
}
