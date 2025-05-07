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

  function getBlobId( clashMap )
  {
    var n;
    do
    {
      nextBlobId++;
      if ( nextBlobId === 0x7fffffff )
        nextBlobId = 0;
    } while ( clashMap.has( nextBlobId ) );

    return nextBlobId;
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
            let num = Number( r.substring( 7 ) );
            analysis.clashMap.set( num, num );
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
          b.placeholder = "binary_" + b.id;

          // a new buffer to receive metadata and data
          let buffer = new ArrayBuffer( value.byteLength + 8 );
          // Need a dataview to specify endianness of 32 bit ints
          let metadataview = new DataView( buffer, 0, 8 );
          metadataview.setUint32( 0, b.id, true ); 
          metadataview.setUint32( 4, b.len, true ); 
          // offset array for the actual data
          let dataarray = new Uint8Array( buffer, 8, value.byteLength );
          // copy the data over
          dataarray.set( value );
          // make a byte array against the whole lot
          b.taggedData = new Uint8Array( buffer );
          analysis.binData.push( b );
          return b.placeholder;
        }
        return value;
      }

      analysis.json = JSON.stringify( this.payload, replacer );
      return analysis;
    }
    
    send( socket )
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
      socket.send( str );
      if ( analysis && analysis.binCount > 0 )
      {
        for ( var i=0; i<analysis.binData.length; i++ )
          socket.send( analysis.binData[i].taggedData );
      }
      return str;
    }
  };

  lib.ToolSocket = class
  {
    wsuri;
    openfunc;
    handler;
    socket;
    pendingIncoming = new Array();
    
    constructor( websserviceuri, handler )
    {
      this.wsuri = websserviceuri;
      this.handler = handler;
      this.validateHandler( handler );
      this.socket = new WebSocket( this.wsuri );
      this.socket.binaryType = "arraybuffer";
      this.pendingIncoming = new Array();
      
      
      this.socket.addEventListener( 'open',    (event) => 
      {
        if ( handler.open )
          handler.open();
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
        console.log( 'Message from server: ', event.data);
        if ( typeof event.data === "string" )
        {
          //var partial = new lib.IncomingParts();
          let message = this.decodeMessage( event.data );
          console.log( message );
          if ( message.binaryParts.size === 0 )
            this.dispatchMessage( message );
          else
            this.pendingIncoming.push( message );
        }
        if ( event.data instanceof ArrayBuffer )
        {
          console.log( 'This is binary data.' );
          // parse two 32 bit numbers
          if ( event.data.byteLength < 8 )
          {
            alert( "Invalid binary insert." );
            return;
          }
          var metadataview = new DataView( event.data, 0, 8 );
          var bid = metadataview.getUint32( 0, true );
          var placeholder = "binary_" + bid;
          var len = metadataview.getUint32( 4, true );
          console.log( placeholder + " " + len );
          if ( event.data.byteLength !== (len + 8) )
          {
            alert( "Invalid binary insert = wrong length." );
            return;
          }          
          for ( var i=0; i<this.pendingIncoming.length; i++ )
          {
            console.log( "Checking pending " + i );
            if ( this.pendingIncoming[i].binaryParts.has( placeholder ) )
            {
              this.pendingIncoming[i].binaryParts.delete( placeholder );
              console.log( "Message contains placeholder" );
              var barray = new Uint8Array( event.data.slice( 8 ) );
              console.log( "Adding" );
              console.log( barray );
              if ( this.pendingIncoming[i].binaryParts.size === 0 )
              {
                // find the placeholder and replace it
                
                // then dispatch
                this.dispatchMessage( this.pendingIncoming[i] );            
                // No longer pending so remove from array
                this.pendingIncoming.splice( i, 1 );
              }
              break;
            }
          }
        }
      });
    };
    
    close()
    {
      socket.close();
    }

    validateHandler()
    {
      
    }

    sendMessage( message )
    {
      message.send( this.socket );
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
    
    decodeMessage( str )
    {
      let sig = "toolmessageversion1.0";
      let header, linesplit, name, value;
      let message = new Object();
      let started = false;
      const regex = RegExp('(.*)[\n\r]+', 'gm');

      message.binaryParts = new Map();
      message.valid = false;
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
          else if ( name === "messagetype" )
            message.messageType = value;
          else if ( name === "binaryinsert" )
          {
            var bp = { "id" : Number(value), "placeholder" : "binary_" + value };
            message.binaryParts.set( bp.placeholder, bp );
          }
          else if ( name === "payloadtype" )
            message.payloadType = value;
          else if ( name === "payload" )
          {
            let payload = str.substring( regex.lastIndex );
            message.payload = JSON.parse( payload );
            break;
          }
        }
      }

      if ( message.id && message.messageType )
        message.valid = true;

      return message;
    };
  
  
  };


  return lib;
})();


export default lbultitoolapi;
