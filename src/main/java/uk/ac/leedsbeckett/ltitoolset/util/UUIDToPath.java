/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.ac.leedsbeckett.ltitoolset.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;

/**
 * Utility to generate tree of paths for storing files
 * named with UUIDs. Intended to keep number of files in
 * a directory reasonably low for sys admin to 
 * browse.
 * 
 * @author jon
 */
public class UUIDToPath
{
  /* E.g. 
4896c91b-9e61-3129-87b6-8aa299028058
*/
  
  private final static int[] begin = { 0,  0,  0,  0,  0,  0,  0 };
  private final static int[] end   = { 4,  8, 13, 18, 23, 28, 32 };
  
  public static Path getRelativePath( String uuidAsString )
  {
    String substr;
    Path path = Paths.get( "" );
    for ( int i=0; i<begin.length; i++ )
    {
      substr = uuidAsString.substring( begin[i], end[i] );
      if ( StringUtils.isEmpty( substr ) )
        substr = "_";
      path = path.resolve( substr );
    }
    return path.resolve( uuidAsString );        
  }  

  public static void main( String[] args )
  {
    Path base = Path.of( "base" );
    for ( int i=0; i<100; i++ )
    {
      Path p = getRelativePath( UUID.randomUUID().toString() );
      System.out.println( p.toString() );
    }
  }
  
}
