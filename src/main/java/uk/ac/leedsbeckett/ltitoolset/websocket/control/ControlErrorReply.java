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
package uk.ac.leedsbeckett.ltitoolset.websocket.control;

/**
 *
 * @author maber01
 */
public class ControlErrorReply
{
  String userMessage;
  String logMessage;
  String extraDataClassName;
  Object extraData;

  public String getUserMessage()
  {
    return userMessage;
  }

  public void setUserMessage( String userMessage )
  {
    this.userMessage = userMessage;
  }

  public String getLogMessage()
  {
    return logMessage;
  }

  public void setLogMessage( String logMessage )
  {
    this.logMessage = logMessage;
  }

  public String getExtraDataClassName()
  {
    return extraDataClassName;
  }

  public void setExtraDataClassName( String extraDataClassName )
  {
    this.extraDataClassName = extraDataClassName;
  }

  public Object getExtraData()
  {
    return extraData;
  }

  public void setExtraData( Object extraData )
  {
    this.extraData = extraData;
  }
  
}
