/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/AnnotationType.java to edit this template
 */
package uk.ac.leedsbeckett.ltitoolset.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation used to describe a tool's functionality.
 * @author jon
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.TYPE )
public @interface ToolFunctionality
{
  public ToolInstantiationType instantiationType();
  public boolean instantiateOnDeepLinking() default false;
  public boolean instantiateOnLaunching() default false;
}
