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

/**
 *
 * @author maber01
 */
public class HandlerAlertException extends Exception
{
  final private String messageId;
  
  /**
   * Constructs an instance of <code>HandlerAlertException</code> with the
   * specified detail message.
   *
   * @param msg the detail message.
   * @param messageId Id of the incoming message that caused the issue.
   */
  public HandlerAlertException( String msg, String messageId )
  {
    super( msg );
    this.messageId = messageId;
  }

  public String getMessageId()
  {
    return messageId;
  }
}
