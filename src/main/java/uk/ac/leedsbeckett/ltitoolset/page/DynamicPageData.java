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
package uk.ac.leedsbeckett.ltitoolset.page;

import java.io.Serializable;

/**
 * A page support object can send an "initial state" to the web page in the
 * form of a JSON object. This is intended to be a POJO that will be
 * serialized as JSON and sent to the browser. Implementations of page support
 * will subclass this.
 * 
 * @author maber01
 */
public class DynamicPageData implements Serializable
{
  private boolean debugging=false;
  private String myId;
  private String myName;
  private String webSocketUri;

  public boolean isDebugging()
  {
    return debugging;
  }

  public void setDebugging( boolean debugging )
  {
    this.debugging = debugging;
  }

  public String getMyId()
  {
    return myId;
  }

  public void setMyId( String myId )
  {
    this.myId = myId;
  }

  public String getMyName()
  {
    return myName;
  }

  public void setMyName( String myName )
  {
    this.myName = myName;
  }

  public String getWebSocketUri()
  {
    return webSocketUri;
  }

  public void setWebSocketUri( String webSocketUri )
  {
    this.webSocketUri = webSocketUri;
  }
  
}
