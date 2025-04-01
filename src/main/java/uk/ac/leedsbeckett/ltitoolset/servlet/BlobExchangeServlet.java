/*
 * Copyright 2025 maber01.
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
package uk.ac.leedsbeckett.ltitoolset.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Used for secure upload and download of Blobs. One Blob may be one file or
 * may be a chunk of a file.
 * 
 * @author maber01
 */
public class BlobExchangeServlet extends HttpServlet
{
  static final Logger logger = Logger.getLogger(BlobExchangeServlet.class.getName() );

  @Override
  protected void doPut( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException
  {
    long len = req.getContentLengthLong();
    logger.log(Level.INFO, "ContentLength = {0}", Long.toString( len ));
    if ( len > 10000000L )  // Exceeds 10MB
    {
      resp.sendError( 413, "Upload chunk too big." );
      return;
    }
    
    MessageDigest md;
    try
    {
      md = MessageDigest.getInstance( "SHA-256" );
    }
    catch ( NoSuchAlgorithmException ex )
    {
      Logger.getLogger( BlobExchangeServlet.class.getName() ).log( Level.SEVERE, null, ex );
      resp.sendError( 500, "No message digest functionality available on this server." );
      return;
    }

    ServletInputStream in = req.getInputStream();
    byte[] buffer = new byte[1024];
    long total = 0L;
    int n;
    while ( ((n = in.read( buffer )) > 0) && total <= len )
    {
      md.update( buffer, 0, n );
      total += n;
    }
    
    byte[] bDigest = md.digest();
    String strDigest = new BigInteger( 1, bDigest ).toString( 16 );
    logger.log(Level.INFO, "Digest = {0}", strDigest);
    resp.setStatus( 201 );  // CREATED
  }

  @Override
  protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException
  {
    resp.setContentType( "text/plain" );
    resp.setCharacterEncoding( "utf-8" );
    resp.setStatus( 200 );
    PrintWriter out = resp.getWriter();
    out.print( "Under development." );
    out.close();
  }
  
}
