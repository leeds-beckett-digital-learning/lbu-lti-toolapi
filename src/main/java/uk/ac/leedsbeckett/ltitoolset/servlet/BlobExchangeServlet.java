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
import java.io.OutputStream;
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
import org.apache.commons.io.IOUtils;
import uk.ac.leedsbeckett.ltitoolset.ToolCoordinator;
import uk.ac.leedsbeckett.ltitoolset.blobex.BlobExException;
import uk.ac.leedsbeckett.ltitoolset.blobex.BlobExchanger;

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
    logger.log( Level.WARNING, "Someone is uploading a blob." );
    ToolCoordinator toolCoord  = ToolCoordinator.get( req.getServletContext() );
    if ( toolCoord  == null ) { resp.sendError( 500, "Cannot find tool manager." ); return; }
    BlobExchanger blobEx = toolCoord.getBlobExchanger();
    if ( blobEx  == null ) { resp.sendError( 500, "Cannot find blob exchanger." ); return; }
    
    long len = req.getContentLengthLong();
    logger.log( Level.INFO, "ContentLength = {0}", Long.toString( len ) );
    if ( len > 10000000L )  // Exceeds 10MB
    {
      resp.sendError( 413, "Upload too big." );
      return;
    }

    String blobid     = req.getHeader( BlobExchanger.HEADER_BLOBID );
    String nonce      = req.getHeader( BlobExchanger.HEADER_NONCE );
    String sessid     = req.getHeader( BlobExchanger.HEADER_SESSIONID );
    
    if ( !blobEx.isCorrectNonce( sessid, nonce ) )
    {
      resp.sendError( 500, "Unknown blob exchange session or incorrect nonce." );
      return;
    }    
    
    // For now just read everything into byte array
    byte[] buffer = IOUtils.toByteArray( req.getInputStream() );
    if ( buffer.length != len )
    {
      resp.sendError( 500, "Binary data wrong length. Content length = " + len + " actual length " + buffer.length );
      return;
    }    

    try
    {
      blobEx.putBlob( sessid, blobid, buffer );
      logger.log(Level.WARNING, "Uploaded sessid {0} blobid {1}", new Object[ ]{sessid, blobid});    
    }
    catch ( BlobExException ex )
    {
      resp.sendError( 500, "Unable to store blob." );
      return;
    }
    
    resp.setStatus( 201 );  // CREATED
  }

  @Override
  protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException
  {
    ToolCoordinator toolCoord  = ToolCoordinator.get( req.getServletContext() );
    if ( toolCoord  == null ) { resp.sendError( 500, "Cannot find tool manager." ); return; }
    BlobExchanger blobEx = toolCoord.getBlobExchanger();
    if ( blobEx  == null ) { resp.sendError( 500, "Cannot find blob exchanger." ); return; }
    
    String blobid     = req.getHeader( BlobExchanger.HEADER_BLOBID );
    String nonce      = req.getHeader( BlobExchanger.HEADER_NONCE );
    String sessid     = req.getHeader( BlobExchanger.HEADER_SESSIONID );

    if ( !blobEx.isCorrectNonce( sessid, nonce ) )
       { resp.sendError( 500, "Unkown session or incorrect nonce." ); return; }
    
    byte[] data = blobEx.getBlob( sessid, blobid );
    if ( data == null )
       { resp.sendError( 500, "Unkown blob." ); return; }
    
    resp.setContentType( "application/octet-stream" );
    resp.setStatus( 200 );
    OutputStream out = resp.getOutputStream();
    out.write( data );
    out.close();
  }
  
}
