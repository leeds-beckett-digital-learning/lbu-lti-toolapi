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
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestExceptionMessage
{
  private final String status;
  private final String code;
  private final String message;
  private final String developerMessage;
  private final String extraInfo;

  public RestExceptionMessage( 
          @JsonProperty( value="status",           required=true  ) String status, 
          @JsonProperty( value="code",             required=false ) String code, 
          @JsonProperty( value="message",          required=true  ) String message, 
          @JsonProperty( value="developerMessage", required=false ) String developerMessage, 
          @JsonProperty( value="extraInfo",        required=false ) String extraInfo )
  {
    this.status = status;
    this.code = code;
    this.message = message;
    this.developerMessage = developerMessage;
    this.extraInfo = extraInfo;
  }

  @JsonProperty
  public String getStatus()
  {
    return status;
  }

  @JsonProperty
  public String getCode()
  {
    return code;
  }

  @JsonProperty
  public String getMessage()
  {
    return message;
  }

  @JsonProperty
  public String getDeveloperMessage()
  {
    return developerMessage;
  }

  @JsonProperty
  public String getExtraInfo()
  {
    return extraInfo;
  }
}
