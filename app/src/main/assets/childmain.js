document.addEventListener('DOMContentLoaded', function() {

    // DOM elements manipulated as user interacts with the app
    var messageBox = document.querySelector('#messages');
    var pconnectBtn = document.querySelector('#pconnect');
    var closeBtn = document.querySelector('#close');
    var remoteVideo = document.querySelector('#remoteVideo');
    var localVideo = document.querySelector('#localVideo');
    localVideo.volume = 0;
    
    //var call_data = JSON.parse(Android.getCallData());
    var call_data = 'i2u2bot1';// Android.getCallData();

      // DOM utilities
      var makePara = function (text) {
        var p = document.createElement('p');
        p.innerText = text;
        return p;
      };

      var addMessage = function (para) {
        if (messageBox.firstChild) {
          messageBox.insertBefore(para, messageBox.firstChild);
        }
        else {
          messageBox.appendChild(para);
        }
      };

      var logError = function (text) {
        var p = makePara('ERROR: ' + text);
        p.style.color = 'red';
        addMessage(p);
      };

      var logMessage = function (text) {
        // if( typeof(text)=="object"){
        //   var StringText = JSON.Stringify(text); 
        //   text = StringText;
        // }
        addMessage(makePara(text));
      };

    
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~WEBRTC CORE SWAG~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    var room = call_data;
    var isChannelReady;
    var isInitiator = false;
    var isStarted = false;
    var localStream;
    var pc;
    var dataChannel;
    var remoteStream;
    var turnReady;
    var blockMessage = false;
    var pc_config = {
        'iceServers': [{
            'url': 'stun:stun1.l.google.com:19302'
        }, {
            'url': 'turn:numb.viagenie.ca',
            'credential': '2201234321k',
            'username': 'kevz93g@gmail.com'
        }]
    };
    var dataOptions = null;
    var pc_constraints = {
        'optional': [{
            'DtlsSrtpKeyAgreement': true
        }, {
            'RtpDataChannels': false
        }]
    };

    // Set up audio and video regardless of what devices are present.
    var sdpConstraints = {
        'mandatory': {
            'OfferToReceiveAudio': true,
            'OfferToReceiveVideo': true
        }
    };

    /////////////////////////////////////////////
    var localVideo = document.querySelector('#localVideo');
    var remoteVideo = document.querySelector('#remoteVideo');
    // var localVideo = document.getElementById('#localVideo');
    // var remoteVideo = document.getElementById('#remoteVideo'); 

    //var room = location.pathname.substring(1);
    //var room = 'foo';

    var socket = io.connect('https://signalling.i2u2robot.in:7080'); // test port 7070

    console.log('Creating or joining room ', room);
    socket.emit('create or join', room);


    socket.on('created', function(data) {
        console.log(data.SocketID + ' created room ' + data.room);
        console.log('This peer is the initiator of room ' + room + '!');
        isInitiator = true;
    });

    socket.on('full', function(room) {
        console.log('Room ' + room + ' is full');
        blockMessage = true;
    });

    socket.on('join', function(room) {
        console.log('Another peer made a request to join room ' + room);
        if(!blockMessage)
        handleRemoteHangup();
        isChannelReady = true;
        console.log(' isChannelReady updated -->', isChannelReady);

    });

    socket.on('joined', function(data) {
        console.log('The peer ' + data.SocketID + 'has joined room ' + data.room);
        isChannelReady = true;
        console.log('joined isChannelReady', isChannelReady);
    });

    socket.on('log', function(array) {
        console.log.apply(console, array);
    });

    socket.on('joinUpdate', function(data) {
        console.log('NEW :: ' + data)
    });
    ///////////////////////////////////////////////////////////////////

    function sendMessage(message) {
        if (!blockMessage) {
            //console.log('Client sending message: ',message );
            //  if (typeof message === 'object') {
            //    message = JSON.stringify(message);
            //  }

            socket.emit('message', {
                'message': message,
                'room': room
            });
        }
    }
    //////////////////////////////////////////////////////////////////

    socket.on('message', function(message) {
        //console.log('Client received message:', message);
        if (message === 'got user media') {
            console.log(' Got user media message');
            maybeStart();
        } else if (message.type === 'offer') {
            console.log('got message.type OFFER');
            if (!isInitiator && !isStarted) {
                maybeStart();
            }
            pc.setRemoteDescription(new RTCSessionDescription(message));
            doAnswer();
        } else if (message.type === 'answer' && isStarted) {
            pc.setRemoteDescription(new RTCSessionDescription(message));
        } else if (message.type === 'candidate' && isStarted) {
            var candidate = new RTCIceCandidate({
                sdpMLineIndex: message.label,
                candidate: message.candidate
            });
            pc.addIceCandidate(candidate);
        } else if (message === 'bye' && isStarted) {
            handleRemoteHangup();
        }
    });

    ////////////////////////////////////////////////////


    function handleUserMedia(stream) {
        console.log('Getting user media with constraints', constraints);
        console.log('Adding local stream.');
        localVideo.src = window.URL.createObjectURL(stream);
        console.log('localvideo width :'+ localVideo.width+'localVideo height '+localVideo.height);
        localStream = stream;
        console.log('Sending Got user media !');
        sendMessage('got user media');
        if (isInitiator) {
            maybeStart();
        }
    }

    function handleUserMediaError(error) {
        console.log('getUserMedia error: ', error);
    }

    var constraints = {
        video: {
        mandatory: {
            maxWidth: 1280,
            maxHeight: 720
        }
    },
        audio: true
    };

    getUserMedia(constraints, handleUserMedia, handleUserMediaError);


    if (location.hostname != "localhost") {
        requestTurn('https://computeengineondemand.appspot.com/turn?username=41784574&key=4080218913');
    }

    function maybeStart() {
        console.log('In maybestart() isStarted :' + isStarted + ' local stream : ' + localStream + 'isChannelReady' + isChannelReady);

        if (!isStarted && typeof localStream != 'undefined' && isChannelReady) {
            createPeerConnection();
            pc.addStream(localStream);
            console.log('pc.addstream done!');
            isStarted = true;
            console.log('isStarted : ', isStarted);
            console.log('isInitiator : ', isInitiator);
            if (isInitiator) {
                console.log('Calling');
                doCall();

            }
        }
    }

    window.onbeforeunload = function() {
        sendMessage('bye');
        return null;
    }

    /////////////////////////////////////////////////////////

    function createPeerConnection() {
        try {
            pc = new RTCPeerConnection(pc_config, pc_constraints);
            pc.onicecandidate = handleIceCandidate;
            console.log('Created RTCPeerConnnection with:\n' +
                '  config: \'' + JSON.stringify(pc_config) + '\';\n' +
                '  constraints: \'' + JSON.stringify(pc_constraints) + '\'.');
        } catch (e) {
            console.log('Failed to create PeerConnection, exception: ' + e.message);
            alert('Cannot create RTCPeerConnection object.');
            return;
        }
        pc.onaddstream = handleRemoteStreamAdded;
        pc.onremovestream = handleRemoteStreamRemoved;

        if (isInitiator) {
            try {
                // Reliable Data Channels not yet supported in Chrome
                dataChannel = pc.createDataChannel("sendDataChannel", dataOptions);

                dataChannel.onmessage = handleMessage;
                trace('Created send data channel');
            } catch (e) {
                alert('Failed to create data channel. ' +
                    'You need Chrome M25 or later with RtpDataChannel enabled');
                trace('createDataChannel() failed with exception: ' + e.message);
            }
            dataChannel.onopen = handleSendChannelStateChange;
            dataChannel.onclose = handleSendChannelStateChange;
        } else {
            pc.ondatachannel = gotReceiveChannel;
        }
    }

    function gotReceiveChannel(event) {
        trace('Receive Channel Callback');
        dataChannel = event.channel;
        dataChannel.onmessage = handleMessage;
        dataChannel.onopen = handleReceiveChannelStateChange;
        dataChannel.onclose = handleReceiveChannelStateChange;
    }

    function handleMessage(event) {
        trace(event.data);
        if (event.data == 'snap') {
            trace('In snap');
            snapshot();
        } else{
            Android.Arduino(event.data);
        
        var AndroidPacket = Android.getArduinoPacket(); // Retrieve packet from Arduino
        //console.log(AndroidPacket);
        send(AndroidPacket);
        }
    }

    function handleSendChannelStateChange() {
        var readyState = dataChannel.readyState;
       // send('Hello world!');
        trace('Send channel state is: ' + readyState);
    }

    function handleReceiveChannelStateChange() {
        var readyState = dataChannel.readyState;
        //send('Hello back');
        trace('Receive channel state is: ' + readyState);
    }

    function handleIceCandidate(event) {
       // console.log('handleIceCandidate event: ', event);
        if (event.candidate) {
            sendMessage({
                type: 'candidate',
                label: event.candidate.sdpMLineIndex,
                id: event.candidate.sdpMid,
                candidate: event.candidate.candidate
            });
        } else {
            console.log('End of candidates.');
        }
    }


    function handleCreateOfferError(event) {
        console.log('createOffer() error: ', e);
    }

    function handleCreateAnswerError(error) {
        console.log('createAnswer() error: ', error);
    }

    function doCall() {
        console.log('Sending offer to peer');
        pc.createOffer(setLocalAndSendMessage, handleCreateOfferError);
    }

    function doAnswer() {
        console.log('Sending answer to peer.');
        pc.createAnswer(setLocalAndSendMessage, handleCreateAnswerError, sdpConstraints);
    }

    function setLocalAndSendMessage(sessionDescription) {
        // Set Opus as the preferred codec in SDP if Opus is present.
        sessionDescription.sdp = preferOpus(sessionDescription.sdp);
        pc.setLocalDescription(sessionDescription);
        console.log('setLocalAndSendMessage sending message', sessionDescription);
        sendMessage(sessionDescription);
    }

    function requestTurn(turn_url) {
        var turnExists = false;
        for (var i in pc_config.iceServers) {
            if (pc_config.iceServers[i].url.substr(0, 5) === 'turn:') {
                turnExists = true;
                turnReady = true;
                break;
            }
        }
        if (!turnExists) {
            console.log('Getting TURN server from ', turn_url);
            // No TURN server. Get one from computeengineondemand.appspot.com:
            var xhr = new XMLHttpRequest();
            xhr.onreadystatechange = function() {
                if (xhr.readyState === 4 && xhr.status === 200) {
                    var turnServer = JSON.parse(xhr.responseText);
                    console.log('Got TURN server: ', turnServer);
                    pc_config.iceServers.push({
                        'url': 'turn:' + turnServer.username + '@' + turnServer.turn,
                        'credential': turnServer.password
                    });
                    turnReady = true;
                }
            };
            xhr.open('GET', turn_url, true);
            xhr.send();
        }
    }

    function handleRemoteStreamAdded(event) {
        console.log('Remote stream added.');
        remoteVideo.src = window.URL.createObjectURL(event.stream);
        remoteStream = event.stream;
        remoteVideo.style.opacity = 1;
    }

    function handleRemoteStreamRemoved(event) {
        console.log('Remote stream removed. Event: ', event);
        remoteVideo.style.opacity = 0;
    }

    function hangup() {
        console.log('Hanging up.');
        stop();
        sendMessage('bye');

    }

    function handleRemoteHangup() {
        isInitiator = true;
        console.log('Session terminated.');
        stop();
    }

    function stop() {
        dataChannel.close();
        pc.close();
        isStarted = false;
        remoteVideo.style.opacity = 0;
        // isAudioMuted = false;
        // isVideoMuted = false;
        pc = null;
    }

    ///////////////////////////////////////////

    // Set Opus as the default audio codec if it's present.
    function preferOpus(sdp) {
        var sdpLines = sdp.split('\r\n');
        var mLineIndex;
        // Search for m line.
        for (var i = 0; i < sdpLines.length; i++) {
            if (sdpLines[i].search('m=audio') !== -1) {
                mLineIndex = i;
                break;
            }
        }
        if (mLineIndex === null) {
            return sdp;
        }

        // If Opus is available, set it as the default in m line.
        for (i = 0; i < sdpLines.length; i++) {
            if (sdpLines[i].search('opus/48000') !== -1) {
                var opusPayload = extractSdp(sdpLines[i], /:(\d+) opus\/48000/i);
                if (opusPayload) {
                    sdpLines[mLineIndex] = setDefaultCodec(sdpLines[mLineIndex], opusPayload);
                }
                break;
            }
        }

        // Remove CN in m line and sdp.
        sdpLines = removeCN(sdpLines, mLineIndex);

        sdp = sdpLines.join('\r\n');
        return sdp;
    }

    function extractSdp(sdpLine, pattern) {
        var result = sdpLine.match(pattern);
        return result && result.length === 2 ? result[1] : null;
    }

    // Set the selected codec to the first in m line.
    function setDefaultCodec(mLine, payload) {
        var elements = mLine.split(' ');
        var newLine = [];
        var index = 0;
        for (var i = 0; i < elements.length; i++) {
            if (index === 3) { // Format of media starts from the fourth.
                newLine[index++] = payload; // Put target payload to the first.
            }
            if (elements[i] !== payload) {
                newLine[index++] = elements[i];
            }
        }
        return newLine.join(' ');
    }

    // Strip CN from sdp before CN constraints is ready.
    function removeCN(sdpLines, mLineIndex) {
        var mLineElements = sdpLines[mLineIndex].split(' ');
        // Scan from end for the convenience of removing an item.
        for (var i = sdpLines.length - 1; i >= 0; i--) {
            var payload = extractSdp(sdpLines[i], /a=rtpmap:(\d+) CN\/\d+/i);
            if (payload) {
                var cnPos = mLineElements.indexOf(payload);
                if (cnPos !== -1) {
                    // Remove CN payload from m line.
                    mLineElements.splice(cnPos, 1);
                }
                // Remove CN line in sdp
                sdpLines.splice(i, 1);
            }
        }

        sdpLines[mLineIndex] = mLineElements.join(' ');
        return sdpLines;
    }

     function send(sendData) {
        trace('checking ready state before sending');
        if (dataChannel.readyState == 'open'){
            trace('Going to send something');
            if(typeof sendData =='object'){
                console.log('Sending Object.')
                dataChannel.send(JSON.stringify(sendData));
         }
            else{       
            trace('Sending no object ');                     
            dataChannel.send(sendData);
        }
        }
    }

    var canvas = document.createElement('canvas');
    var ctx = canvas.getContext('2d');
    canvas.setAttribute('width', 1280);
    canvas.setAttribute('height', 720);
    var sendingsnap = false;

    var chunkLength = 10000;
    function snapshot() {
        sendingsnap = true;
        trace('Snapshot();');
        if (localStream != 'undefined') {
            trace('localstream not undefined');
            ctx.drawImage(localVideo, 0, 0,1280,720);
            // "image/webp" works in Chrome.
            // Other browsers will fall back to image/png.
            //document.querySelector('img').src = canvas.toDataURL('image/webp');
            var imageData = canvas.toDataURL('image/png'); 
            SliceAndSend(imageData);  
    }
}
    function SliceAndSend(chunk) {
    var idata = {}; // data object to transmit over data channel
    //if (event) text = event.target.result; // on first invocation
     trace('chunk lenght :'+ chunk.length);
    if (chunk.length > chunkLength) {
        idata.message = chunk.slice(0, chunkLength); // getting chunk using predefined chunk length
    } else {
        trace('idata lenght is NOT greater than chunk lenght');
        idata.message = chunk;
        idata.last = true;
    }
    idata.snap= true;
    send(idata); // use JSON.stringify for chrome!
    var remainingData = chunk.slice(idata.message.length);
    if (remainingData.length) setTimeout(function () {
        SliceAndSend(remainingData); // continue transmitting
    }, 10)
        if(idata.snap==true)
        sendingsnap = false;
}
    //------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //--------------------------------------------------------------Viewport settings
    var viewportwidth, viewportheight;
    if (typeof window.innerWidth != 'undefined') {
        viewportwidth = window.innerWidth,
            viewportheight = window.innerHeight
    }
    console.log('viewport height: ' + viewportheight +'::viewportwidth: '+ viewportwidth);
    // document.getElementById("videoContainer").style.width = viewportwidth + 'px';
    // document.getElementById("videoContainer").style.height = viewportheight + "px";
    setInterval(function () { 
        if(isStarted&&!sendingsnap){
        var AndroidPacket = Android.getArduinoPacket(); // Retrieve packet from Arduino
        send(AndroidPacket);  
    }
    }, 1000);

});