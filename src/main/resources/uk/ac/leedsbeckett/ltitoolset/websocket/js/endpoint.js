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
      
    toString()
    {
      var str = "toolmessageversion1.0\n";
      str += "id:" + this.id + "\n";
      if ( this.replyToId )
        str += "replytoid:" + this.replyToId + "\n";
      if ( this.messageType )
        str += "messagetype:" + this.messageType + "\n";
      if ( this.payloadType && this.payload )
      {
        str += "payloadtype:" + this.payloadType + "\npayload:\n" ;
        str += JSON.stringify( this.payload );
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
    
    constructor( websserviceuri, handler )
    {
      this.wsuri = websserviceuri;
      this.handler = handler;
      this.validateHandler( handler );
      this.socket = new WebSocket( this.wsuri );
      
      
      this.socket.addEventListener( 'open',    (event) => 
      {
        if ( handler.open )
          handler.open();
      });

      this.socket.addEventListener( 'close', (event) => 
      {
        if ( event.wasClean )
          alert( `Connection to service was closed with code = ${event.code} reason = ${event.reason}` );
        else
          alert( "Connection to service was closed abruptly." );
        this.socket = null;
      });

      this.socket.addEventListener( 'error', (event) => 
      {
        alert( `Web Socket error. ${event.message}` );
      });

      this.socket.addEventListener( 'message', (event) => 
      {
        console.log( 'Message from server: ', event.data);
        let message = this.decodeMessage( event.data );
        console.log( message );
        this.dispatchMessage( message );
      });
    };

    validateHandler()
    {
      
    }

    sendMessage( message )
    {
      this.socket.send( message.toString() );
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
