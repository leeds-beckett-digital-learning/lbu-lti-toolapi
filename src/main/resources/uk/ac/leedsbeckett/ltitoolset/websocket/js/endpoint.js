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

    analyse()
    {
      var analysis = {};
      analysis.clashMap = new Map();
      analysis.binCount = 0;
      analysis.binData = [];
      analysis.json = null;

      function analyticalReplacer( key, value )
      {
        console.log( "analysing" );
        console.log( key );
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

      analysis.json = JSON.stringify( this.payload, analyticalReplacer );
      return analysis;
    }
    
    split( analysis )
    {
      function replacer( key, value )
      {
        console.log( "replacing" );
        console.log( this );
        console.log( key );
        console.log( value );
        if ( value instanceof Uint8Array )
        {
          let b = {};
          b.id = getBlobId( analysis.clashMap );
          b.len = value.length;
          b.data = value;
          analysis.binData.push( b );
          return b.id;
        }
        return value;
      }

      analysis.json = JSON.stringify( this.payload, replacer );
      return analysis;
    }
    
    async send( socket, clientConfig )
    {
      var payloadText;
      var analysis = null;
      
      if ( this.payloadType && this.payload )
      {
        analysis = this.analyse();
        if ( analysis.binCount === 0  )
        {
          payloadText = analysis.json;
        }
        else
        {
          this.split( analysis );
          payloadText = analysis.json;
        }
      }      
      
      // Upload binary parts first
      if ( analysis && analysis.binCount > 0 )
      {
        for ( var i=0; i<analysis.binData.length; i++ )
        {
          console.log( "Uploading binary data " + analysis.binData[i].id );
          const b = analysis.binData[i];
          const blob = new Blob([b.data]);
          const requestOptions = {
              method: 'PUT',
              headers: { 
                'Content-Type': 'application/octet-stream',
                'Content-Length':  b.len,
                'X-LBU-SessionID': clientConfig.sessionId,
                'X-LBU-Nonce':     clientConfig.blobNonce,
                'X-LBU-BlobId':    b.id
              },
              body: blob
          };
          const response = await fetch( clientConfig.blobUri, requestOptions);
          console.log( "status = " + response.status );      
        }
      }
      
      var str = "toolmessageversion1.0\n";
      str += "id:" + this.id + "\n";
      if ( this.replyToId )
        str += "replytoid:" + this.replyToId + "\n";
      if ( this.messageType )
        str += "messagetype:" + this.messageType + "\n";
      if ( this.payloadType && this.payload )
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
        str += "payloadtype:" + this.payloadType + "\npayload:\n" ;
        str += payloadText;
      }
      console.log( "Sending message on web socket." );
      console.log( str );
      socket.send( str );
      return str;
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
    
    async onMessage( event )
    {
        console.log( 'Message from server: ', event.data);
        if ( typeof event.data === "string" )
        {
          //var partial = new lib.IncomingParts();
          let message = await this.decodeMessage( event.data );
          console.log( message );
          if ( message.control )
            this.processControlMessage( message );
          else
            this.dispatchMessage( message );
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
    
    sendMessage( message )
    {
      message.send( this.socket, this.clientConfig );
    };

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
    
    // Remember that because this is async the value in the return
    // statement will be wrapped in a promise.
    async decodeMessage( str )
    {
      let sig = "toolmessageversion1.0";
      let header, linesplit, name, value;
      let message = new Object();
      let started = false;
      let strPayload = null;
      const regex = RegExp('(.*)[\n\r]+', 'gm');

      message.binaryParts = new Map();
      message.valid = false;
      message.control = false;
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
            strPayload = str.substring( regex.lastIndex );
            break;
          }
        }
      }

      // fetch the binary parts now - before parsing any JSON
      for ( const [key,value] of message.binaryParts )
      {
        value.data = null;

        const requestOptions = {
            method: 'GET',
            headers: { 
              'X-LBU-SessionID': this.clientConfig.sessionId,
              'X-LBU-Nonce':     this.clientConfig.blobNonce,
              'X-LBU-BlobId':    value.id
            }
        };      
        // https://developer.mozilla.org/en-US/docs/Web/API/ReadableStream#async_iteration_of_a_stream_using_for_await...of
        const response = await fetch( this.clientConfig.blobUri, requestOptions);
        console.log( "status = " + response.status );      
        if ( Math.floor( response.status / 100 ) !== 2 )
          return; // should be some exception handling here
        // should check for excessive size...
        if ( !response.body )
          return;
        console.log( response.body );
        console.log( typeof response.body );
        const chunks = [];
        var byteCount=0;
        for await (const chunk of response.body)
        {
          // chunk should be Uint8Array
          byteCount += chunk.byteLength;
          console.log( chunk );
          chunks.push(chunk);
        }
        // If there is only one chunk set it
        if ( chunks.length === 1 )
        {
          console.log( "one chunk only" );
          console.log( chunks[0] );
          value.data = chunks[0];
        }
        else
        {
          // Multiple chunks so concatenate
          // big enough array for all
          console.log( "multiple chunks" );
          value.data = new Uint8Array( new ArrayBuffer( byteCount ) );
          var offset = 0;
          // copy data one by one
          for ( var i=0; i<chunks.length; i++ )
          {
            value.data.set( chunks[i], offset );
            offset += chunks[i].byteLength;
          }
          console.log( value.data );
        }
      }

      if ( message.id && message.messageType )
        message.valid = true;

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

      if ( strPayload )
      {
        console.log( "Parsing JSON payload" );
        message.payload = JSON.parse( strPayload, reviver );
      }
      
      return message;
    };
  
    
  
  };


  return lib;
})();


export default lbultitoolapi;
