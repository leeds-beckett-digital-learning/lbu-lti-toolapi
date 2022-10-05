/*
 * Copyright 2022 maber01.
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
package uk.ac.leedsbeckett.lti.toolset.websocket;

import java.util.UUID;

/**
 *
 * @author maber01
 */
public class ToolMessage
{
  public static final String HEADER = "toolmessageversion1.0";
  
  boolean valid=true;
  
  String id;
  String replyToId;
  String messageType;
  String payloadType;
  Object payload;
  
  String raw;

  public ToolMessage( String replytoid, String messageType, Object payload )
  {
    this.id = UUID.randomUUID().toString();
    this.replyToId = replytoid;
    this.messageType = messageType;
    if ( payload != null )
      this.payloadType = payload.getClass().getName();
    this.payload = payload;
    this.raw = null;
  }
  
  public ToolMessage( String raw )
  {
    this.raw = raw;
  }

  public boolean isValid()
  {
    return valid;
  }

  public void setValid( boolean valid )
  {
    this.valid = valid;
  }

  
  
  public String getId()
  {
    return id;
  }

  public void setId( String id )
  {
    this.id = id;
  }

  public String getReplyToId()
  {
    return replyToId;
  }

  public void setReplyToId( String replyToId )
  {
    this.replyToId = replyToId;
  }

  public String getMessageType()
  {
    return messageType;
  }

  public void setMessageType( String messageType )
  {
    this.messageType = messageType;
  }

  public String getPayloadType()
  {
    return payloadType;
  }

  public void setPayloadType( String payloadType )
  {
    this.payloadType = payloadType;
  }

  
  public Object getPayload()
  {
    return payload;
  }

  public void setPayload( Object payload )
  {
    this.payload = payload;
  }

  public String getRaw()
  {
    return raw;
  }

  public void setRaw( String raw )
  {
    this.raw = raw;
  }

  
}
