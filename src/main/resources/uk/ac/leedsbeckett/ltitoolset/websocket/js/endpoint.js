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


/*

===================================
  Generated script, do not edit. 
===================================

*/

const lbultitoolapi = (function () {

  let lib = new Object();
  let nextid = Math.floor( Math.random()*10000 );
  
  let nextBlobId = 0;
  let strBlobId = "binary_0";
  
  function getBlobId( clashMap )
  {
    var n;
    do
    {
      nextBlobId++;
      if ( nextBlobId === 0x7fffffff )
        nextBlobId = 0;
      strBlobId = "binary_" + nextBlobId;
    } while ( clashMap.has( strBlobId ) );

    return strBlobId;
  }
  
  
  
  lib.ClientMessage = class 
  {
    constructor( messageType, payloadType )
    {
      this.id = nextid++;
      this.messageType = messageType?messageType:null;
      this.payloadType = payloadType?payloadType:null;
      this.replyToId   = null;
      this.payload     = null;
    }    
  };



  lib.ToolSocket = class
  {
    wsuri;
    openfunc;
    handler;
    socket;
    clientConfig;
    
    constructor( websserviceuri, handler )
    {
      this.wsuri = websserviceuri;
      this.handler = handler;
      this.validateHandler( handler );
      this.socket = new WebSocket( this.wsuri );
      this.socket.binaryType = "arraybuffer";
      
      
      this.socket.addEventListener( 'open',    (event) => 
      {
        // don't call open on handler yet - wait for client config
      });

      this.socket.addEventListener( 'close', (event) => 
      {
        if ( event.wasClean )
          console.log( `Connection to service was closed cleanly with code = ${event.code} reason = ${event.reason}` );
        else
          alert( `Connection to service was closed abruptly with code = ${event.code} reason = ${event.reason}` );
        this.socket = null;
      });

      this.socket.addEventListener( 'error', (event) => 
      {
        alert( `Web Socket error. ${event.message}` );
      });

      this.socket.addEventListener( 'message', (event) => 
      {
        this.onMessage( event );
      });
      
    };
    
    onMessage( event )
    {
      if ( typeof event.data === "string" )
      {
        console.log( 'Message from server: ', event.data);
        
        //var partial = new lib.IncomingParts();
        let message = this.decodeMessageHeaders( event.data );
        this.getBinaryParts( message )
          .then( () =>
            {
              console.debug( "onMessage then handler" );
              this.decodePayload( message );
              console.log( message );
              if ( message.control )
                this.processControlMessage( message );
              else
                this.dispatchMessage( message );
            } );
      }
    }
    
    close()
    {
      socket.close();
    }

    validateHandler()
    {
      
    }

    processControlMessage( message )
    {
      if ( message.messageType === "ControlClientConfiguration" )
      {
        this.clientConfig = message.payload;
        if ( this.handler.open )
          this.handler.open();        
      }
    }
    
    dispatchMessage( message )
    {
      if ( !message.valid )
      {
        alert( "Invalid message from server." );
        return;
      }

      if ( this.handler['handle'+message.messageType] )
        this.handler['handle'+message.messageType]( message );
      else
        console.log( "No handler for messages of type " + message.messageType );
    }
    
    decodeMessageHeaders( str )
    {
      let sig = "toolmessageversion1.0";
      let header, linesplit, name, value;
      let message = new Object();
      let started = false;
      const regex = RegExp('(.*)[\n\r]+', 'gm');

      message.binaryParts = new Map();
      message.valid = false;
      message.control = false;
      message.strPayload = null;
      
      console.log( message );
      while ( true )
      {
        linesplit = regex.exec( str );
        if ( linesplit )
          header = linesplit[1];
        else
          break;          
        if ( !started )
        {
          started = true;
          if ( sig === header )
            continue;
          else
            return message;
        }
        let n = header.indexOf( ":" );
        if ( n > 0 )
        {
          name = header.substring( 0, n );
          value = header.substring( n+1 );
          if ( name === "id" )
            message.id = value;
          else if ( name === "replytoid" )
            message.replyToId = value;
          else if ( name === "control" && value === "true" )
            message.control = true;
          else if ( name === "messagetype" )
            message.messageType = value;
          else if ( name === "binaryinsert" )
          {
            var bp = { "id" : value, "data" : null };
            message.binaryParts.set( bp.id, bp );
          }
          else if ( name === "payloadtype" )
            message.payloadType = value;
          else if ( name === "payload" )
          {
            message.strPayload = str.substring( regex.lastIndex );
            break;
          }
        }
      }
      if ( message.id && message.messageType )
        message.valid = true;
      return message;
    }

    // Returns a promise that is fulfilled when all data is loaded into
    // byte arrays.
    getBinaryParts( message )
    {
      // If no binary parts return a promise that instantly resolves.
      if ( !message.strPayload || !message.binaryParts || message.binaryParts.size === 0 )
        return new Promise( (resolve, reject) => { resolve("No binary parts to fetch."); } );
      
      var getBinaryPromises = [];
      // fetch the binary parts now - before parsing any JSON
      for ( const [key,value] of message.binaryParts )
        getBinaryPromises.push( this.getOneBinaryPart( value ) );
      // Return a promise that succeeds if all parts succeed
      return Promise.all( getBinaryPromises )
              .then( (value) => console.debug( "All binary parts succeeded." ), 
                     (error) => console.debug( "One or more binary parts failed" ) );
    }
    
    
    // Return a 'Promise' that the fetch is complete and the data read
    getOneBinaryPart( binarypart )
    {
      return new Promise( (resolve, reject ) =>
      {
        binarypart.data = null;

        const requestOptions = 
        {
            method: 'GET',
            headers:
            { 
              'X-LBU-SessionID': this.clientConfig.sessionId,
              'X-LBU-Nonce':     this.clientConfig.blobNonce,
              'X-LBU-BlobId':    binarypart.id
            }
        };
        
        function processBytesSuccess( data )
        {
          binarypart.data = data;
          console.log( binarypart.data );
          resolve( "Success" );
        }
        
        function procesBytesFail( error )
        {
          // Fail the main getOneBinaryPart promise
          reject( error );
        }
        
        function processFetchSuccess( response )
        {
          console.log( "status = " + response.status );      
          if ( Math.floor( response.status / 100 ) !== 2 )
            reject( response.statusText ); // should be some exception handling here
          // should check for excessive size...
          response.bytes().then( (data) => processBytesSuccess( data ), (error) => processBytesFail( error) );
        }
        
        function processFetchFail( error )
        {
          // reject the promise for getOneBinaryPart()
          reject( error );
        }
        
        fetch( this.clientConfig.blobUri, requestOptions)
          .then( (response) => processFetchSuccess( response ), (error) => processFetchFail() );

      } );
    }
    
    decodePayload( message )
    {
      function reviver( key, value )
      {
        if ( !message.binaryParts ) return value;
        if ( message.binaryParts.size === 0 ) return value;
        if ( typeof value === 'string' || value instanceof String )
        {
          if ( value.indexOf( "binary_" ) === 0 )
          {
            if ( message.binaryParts.has( value ) )
            {
              let bp = message.binaryParts.get( value );
              if ( bp.data )
                return bp.data;
            }
          }
        }        
        return value;
      }

      if ( message.strPayload )
      {
        console.log( "Parsing JSON payload" );
        message.payload = JSON.parse( message.strPayload, reviver );
      }
      
      return message;
    };

    
    sendMessage( message )
    {
      var analysis;

      if ( message.payloadType && message.payload )
        analysis = this.analyseAndSplit( message );
      
      // Start uploading binary parts first compiling list of promises
      // so they can all be monitored. (Monitoring required because all
      // the binaries need to be successfully uploaded with PUT method
      // before we attempt to send the main payload via websocket
      const putPromises = [];
      if ( analysis && analysis.binCount > 0 )
        for ( var i=0; i<analysis.binData.length; i++ )
          putPromises.push( this.putBinary( analysis.binData[i], this.clientConfig ) );

      // While uploading is proceeding compose the text message
      // by combining headers with payload, if there is any.
      var str = this.toText( message, analysis );
      
      // can start sending text now if there were no binary inserts
      if ( !analysis || analysis.binCount === 0 )
      {
        console.log( "No binary inserts so want to send text right away", str );
        this.socket.send( str );
        return;
      }

      console.log( "Will send this text when binary uploading done", str );
      // Schedule sending the text when all binary uploads worked
      const thisToolSocket = this;
      function sendTextIfBinarySucceeded( values )
      {
        // what have we got here?
        console.log( "sendTextIfBinarySucceeded()" );
      
        // fetch promises might succeed but unacceptable status
        // code was returned. So, we still need to check for success.
        for ( var i=0; i<analysis.binData.length; i++ )
          if ( analysis.binData[i].failed )
          {
            console.error( "Not sending message because one or more binary uploads failed." );
            return;
          }
        // All good so now send the string
        console.log( "Sending message. socket.send( str )" );
        thisToolSocket.socket.send( str );      
      }
      function binaryFailed( reason )
      {
        console.error( "one or more binary uploads failed", reason );
      }      
      
      Promise.all( putPromises )
          .then(
                (value)  => { sendTextIfBinarySucceeded( value ); },
                (reason) => { binaryFailed( reason );             }
              );
      
      console.log( "Text message will be sent when all binary uploads complete." );
      return;
    }
  
    analyse( message )
    {
      var analysis = {};
      analysis.clashMap = new Map();
      analysis.binCount = 0;
      analysis.binData = [];
      analysis.json = null;

      function analyticalReplacer( key, value )
      {
        if ( value instanceof Uint8Array )
        {
          analysis.binCount++;
          return undefined;
        }
        if ( typeof value === 'string' || value instanceof String )
        {
          if ( value.indexOf( "binary_" ) === 0 )
          {
            for ( var i=7; i<value.length; i++ )
            {
              let n = r.charCodeAt( i );
              if ( n < 0x30 || n > 0x39 )
                return value;
            }
            analysis.clashMap.set( value, value );
          }
          return value;
        }
        return value;
      }

      analysis.json = JSON.stringify( message.payload, analyticalReplacer );
      return analysis;
    }
    
    split( message, analysis )
    {
      function replacer( key, value )
      {
        if ( value instanceof Uint8Array )
        {
          let b = {};
          b.id = getBlobId( analysis.clashMap );
          b.len = value.length;
          b.data = value;
          b.sent = false;
          b.failed = false;
          analysis.binData.push( b );
          return b.id;
        }
        return value;
      }

      analysis.json = JSON.stringify( message.payload, replacer );
      return analysis;
    }

    analyseAndSplit( message )
    {
      // Count Uint8Arrays in the payload
      // and find any string values that could be mistaken for
      // binary placeholder strings. At same time serialize payload
      // to JSON.
      var analysis = this.analyse( message );
      // If there is one or more UintArray values in the payload 
      // make them into a list and 
      // make a new JSON with placeholder strings to represent them.
      if ( analysis.binCount !== 0  )
        this.split( message, analysis );
      
      return analysis;
    }
    
    // Will return a promise so multiple binary puts can be
    // done in parallel.
    putBinary( b )
    {
      console.log( "Starting binary data upload " + b.id );
      const blob = new Blob([b.data]);
      const requestOptions = {
          method: 'PUT',
          headers: { 
            'Content-Type': 'application/octet-stream',
            'Content-Length':  b.len,
            'X-LBU-SessionID': this.clientConfig.sessionId,
            'X-LBU-Nonce':     this.clientConfig.blobNonce,
            'X-LBU-BlobId':    b.id
          },
          body: blob
      };
      function onFulfilled( response )
      {
        console.log( "For binary " + b.id + " status = " + response.status );
        // Still counts as fail if status indicates problem.
        if ( Math.floor( response.status/100 ) !== 2 )
        {
          console.log( "Failed upload " + response.statusText );
          b.failed = true;
        }
      }
      function onRejected( reason )
      {
        b.failed = true;
        console.log( "blob " + b.id + " failed", reason );
      }
      return fetch( this.clientConfig.blobUri, requestOptions)
              .then( onFulfilled, onRejected );
    }    
    
    toText( message, analysis )
    {
      var str = "toolmessageversion1.0\n";
      str += "id:" + message.id + "\n";
      if ( message.replyToId )
        str += "replytoid:" + message.replyToId + "\n";
      if ( message.messageType )
        str += "messagetype:" + message.messageType + "\n";
      if ( message.payloadType && message.payload )
      {
        if ( analysis && analysis.binCount > 0 )
        {
          for ( var i=0; i<analysis.binData.length; i++ )
          {
            str+= "binaryinsert:";
            str+= analysis.binData[i].id;
            str+= "\n";
          }
        }
        str += "payloadtype:" + message.payloadType + "\npayload:\n" ;
        str += analysis.json;
      }
      return str;
    }
  
    
  
  };


  return lib;
})();


export default lbultitoolapi;
