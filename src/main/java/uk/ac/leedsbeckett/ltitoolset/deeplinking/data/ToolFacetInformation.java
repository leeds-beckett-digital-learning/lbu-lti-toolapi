/*
 * Copyright 2025 maber01.
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
package uk.ac.leedsbeckett.ltitoolset.deeplinking.data;

import uk.ac.leedsbeckett.ltitoolset.annotations.ToolFacet;
import uk.ac.leedsbeckett.ltitoolset.annotations.ToolInstantiationLevel;

/**
 *
 * @author maber01
 */
public class ToolFacetInformation
{
  private final String id;
  private final String title;
  private final ToolInstantiationLevel instantiationLevel;
  private final String launchURI;

  public ToolFacetInformation( String id, String title, ToolInstantiationLevel instantiationLevel, String launchURI )
  {
    this.id = id;
    this.title = title;
    this.instantiationLevel = instantiationLevel;
    this.launchURI = launchURI;
  }

  public ToolFacetInformation( ToolFacet toolMapping )
  {
    this.id                = toolMapping.id();
    this.title             = toolMapping.title();
    this.instantiationLevel = toolMapping.instantiationLevel();
    this.launchURI         = toolMapping.launchURI();
  }

  public String getId()
  {
    return id;
  }

  public String getTitle()
  {
    return title;
  }

  public ToolInstantiationLevel getInstantiationLevel()
  {
    return instantiationLevel;
  }

  public String getLaunchURI()
  {
    return launchURI;
  }
  
}
