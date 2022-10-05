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

import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;

/**
 *
 * @author maber01
 */
public class HandlerMethodRecord
{
    final String name;
    final Method method;
    final Class<?> parameterClass;
    
    public HandlerMethodRecord( String name, Method method, Class<?> parameterClass )
    {
      this.name = name;
      this.method = method;
      this.parameterClass = parameterClass;
    }

    public String getName()
    {
      return name;
    }

    public Method getMethod()
    {
      return method;
    }

    public Class<?> getParameterClass()
    {
      return parameterClass;
    }  
}
