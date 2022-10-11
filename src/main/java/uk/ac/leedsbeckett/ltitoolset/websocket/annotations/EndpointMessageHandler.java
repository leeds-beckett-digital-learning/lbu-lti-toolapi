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
 * This annotates methods in a websocket endpoint. If no name is specified
 * the prefix 'handle' will be taken off the method name and the remainder
 * will be used as the message name. A javascript class will be created
 * for use by client software to construct a message and the class will be
 * named using this property. Client messages will be automatically dispatched
 * to the right handler based on the message name.
 * 
 * @author maber01
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.METHOD)
public @interface EndpointMessageHandler
{
  /**
   * The name of the message that will be handled by the annotated method.
   * If blank the name will be based on the method name.
   * 
   * @return The name which is blank by default.
   */
  String name() default "";
}
