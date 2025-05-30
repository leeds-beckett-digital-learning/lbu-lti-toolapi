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

import java.lang.reflect.Method;

/**
 * A class that holds information about a handler method from an endpoint.
 * 
 * @author maber01
 */
public class HandlerMethodRecord
{
    final String name;
    final Method method;
    final Class<?> parameterClass;
    final boolean replyPromised;
   
    /**
     * Instantiate the class providing values for the final fields.
     * 
     * @param name Name of the message.
     * @param method The reflected method.
     * @param parameterClass The class of the message that is to be handled.
     * @param replyPromised Whether the handler must reply to the incoming message promptly.
     */
    public HandlerMethodRecord( String name, Method method, Class<?> parameterClass, boolean replyPromised )
    {
      this.name = name;
      this.method = method;
      this.parameterClass = parameterClass;
      this.replyPromised = replyPromised;
    }

    /**
     * Get the name of the message.
     * 
     * @return The name.
     */
    public String getName()
    {
      return name;
    }

    /**
     * Get the reflected method.
     * 
     * @return The method.
     */
    public Method getMethod()
    {
      return method;
    }

    /**
     * Get the parameter class.
     * 
     * @return The class.
     */
    public Class<?> getParameterClass()
    {
      return parameterClass;
    }  

  public boolean isReplyPromised()
  {
    return replyPromised;
  }
}
