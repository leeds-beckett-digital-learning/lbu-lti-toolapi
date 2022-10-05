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
package uk.ac.leedsbeckett.ltitoolset.websocket;

import java.util.UUID;

/**
 * A websocket message format for use in this API.
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

  /**
   * Constructor for sender to prepare the message for sending.
   * 
   * @param replytoid ID of the message this is in reply to.
   * @param messageType The type (name) of the message.
   * @param payload The payload as a POJO.
   */
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
  
  /**
   * Constructor for recreating incoming message from raw string.
   * 
   * @param raw The source message string.
   */
  public ToolMessage( String raw )
  {
    this.raw = raw;
  }

  /**
   * Was the message created from a valid raw string.
   * 
   * @return Validity
   */
  public boolean isValid()
  {
    return valid;
  }

  /**
   * Decoder sets this based on the validity of the coded message.
   * 
   * @param valid Validity
   */
  public void setValid( boolean valid )
  {
    this.valid = valid;
  }

  
  /**
   * Get ID of the message.
   * 
   * @return ID string.
   */
  public String getId()
  {
    return id;
  }

  /**
   * Set the ID of the message.
   * 
   * @param id The ID string.
   */
  public void setId( String id )
  {
    this.id = id;
  }

  /**
   * Get the ID of the message that this is a reply to.
   * 
   * @return The reply to ID or null.
   */
  public String getReplyToId()
  {
    return replyToId;
  }

  /**
   * Set the ID of the message that this is a reply to.
   * @param replyToId The reply to ID.
   */
  public void setReplyToId( String replyToId )
  {
    this.replyToId = replyToId;
  }

  /**
   * Get the message type (name).
   * 
   * @return The name.
   */
  public String getMessageType()
  {
    return messageType;
  }

  /**
   * Set the message type (name)
   * @param messageType  The name.
   */
  public void setMessageType( String messageType )
  {
    this.messageType = messageType;
  }

  /**
   * The name of the Java class of the payload.
   * 
   * @return The Java class name.
   */
  public String getPayloadType()
  {
    return payloadType;
  }

  /**
   * The name of the Java class of the payload.
   * 
   * @param payloadType The Java class name.
   */
  public void setPayloadType( String payloadType )
  {
    this.payloadType = payloadType;
  }

  /**
   * Get the original or decoded message payload object.
   * 
   * @return The payload object.
   */
  public Object getPayload()
  {
    return payload;
  }

  /**
   * Set the payload object.
   * 
   * @param payload The java object to use as a payload.
   */
  public void setPayload( Object payload )
  {
    this.payload = payload;
  }

  /**
   * Get the raw string version of the message.
   * 
   * @return The text or null if this is not an incoming message.
   */
  public String getRaw()
  {
    return raw;
  }

  /**
   * Set the raw string version of the incoming message.
   * 
   * @param raw The encoded message.
   */
  public void setRaw( String raw )
  {
    this.raw = raw;
  }
}
