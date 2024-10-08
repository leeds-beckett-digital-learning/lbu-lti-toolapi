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
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a response from Blackboard Learn API with results of course
 * search or list.
 * 
 * @author maber01
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetUsersV1Results implements Serializable
{
  private final ArrayList<UserV1> results;
  private final PagingInfo pagingInfo;

  @JsonCreator
  public GetUsersV1Results( 
          @JsonProperty(value = "results",    required = true ) ArrayList<UserV1> results, 
          @JsonProperty(value = "pagingInfo", required = false) PagingInfo pagingInfo )
  {
    this.results = results;
    this.pagingInfo = pagingInfo;
  }
  
  @JsonProperty
  public List<UserV1> getResults()
  {
    return results;
  }

  @JsonProperty
  public PagingInfo getPagingInfo()
  {
    return pagingInfo;
  }
}
