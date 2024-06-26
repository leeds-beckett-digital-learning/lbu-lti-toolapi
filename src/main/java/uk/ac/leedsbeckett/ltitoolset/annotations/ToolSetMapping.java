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
package uk.ac.leedsbeckett.ltitoolset.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is applied to a class to indicates mappings for
 * the tool set.
 * 
 * @author maber01
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.TYPE )
public @interface ToolSetMapping
{
  /**
   * Name of the tool set.
   * 
   * @return The name
   */
  public String name();
  
  /**
   * Where in URL space, relative to the servlet context, where the standard 
   * login servlet will be placed.
   * 
   * @return The URL.
   */
  public String loginUrl();
  
  /**
   * Where in URL space, relative to the servlet context, where the standard 
   * launch servlet will be placed.
   * 
   * @return The URL.
   */
  public String launchUrl();   

  /**
   * In URL space, relative to the servlet context, where the standard 
   * Jwks servlet (which supplies a list of current public keys) will be placed.
   * 
   * @return The URL.
   */
  public String jwksUrl();   
  
  /**
   * Where the auto-reg-init is mapped in URL space relative to servlet context.
   * 
   * @return The URL.
   */
  public String autoRegUrl();

  /**
   * The URL of the deep linking JSP page.
   * 
   * @return The URL.
   */
  public String deepLinkingUrl();
}
