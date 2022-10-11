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
package uk.ac.leedsbeckett.ltitoolset.websocket.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used on ToolEndpoint implementations to set
 * properties for the generation of Javascript for use on the client side
 * to access the endpoint.
 * 
 * @author maber01
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.TYPE )
public @interface EndpointJavascriptProperties
{
  /**
   * The name of the javascript module AND the name of the value that
   * is exported from the module.
   * 
   * @return The name of the module.
   */
  String module();
  
  /**
   * A prefix to use in the generation of variables. (Currently ignored.)
   * 
   * @return A string property.
   */
  String prefix();
  
  /**
   * The name of an enum class that has constants representing each of the
   * server messages that might be sent to a client. The class must implement
   * ToolMessageName.
   * 
   * @return The enum name including package.
   */
  String messageEnum();
}
