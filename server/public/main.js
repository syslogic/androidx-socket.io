$(function() {

  const FADE_TIME           = 150; // ms
  const TYPING_TIMER_LENGTH = 400; // ms
  const COLORS = [
    '#e21400', '#91580f', '#f8a700', '#f78b00',
    '#58dc00', '#287b00', '#a8f07a', '#4ae8c4',
    '#3b88eb', '#3824aa', '#a700ff', '#d300e7'
  ];

  // Initialize variables
  const $window = $(window);
  const $usernameInput = $('.usernameInput'); // Input for username
  const $sockets = $('.sockets');             // Sockets area
  const $messages = $('.messages');           // Messages area
  const $inputMessage = $('.inputMessage');   // Input message input box
  const $loginPage = $('.login.page');        // The login page
  const $chatPage = $('.chat.page');          // The chatroom page

  // Prompt for setting a username
  let username;
  let connected = false;
  let typing = false;
  let lastTypingTime;

  let $currentInput = $usernameInput.focus();

  // noinspection JSUnresolvedReference
  const socket = io();

  const addParticipantsMessage = (data) => {
    let message = '';
    if (data.data.length === 1) {
      message += "There's 1 participant";
    } else {
      message += "There are " + data.data.length + " participants";
    }
    log(message);
  }

  // Update ul.sockets
  const updateSockets = (data) => {
    $sockets.empty();
    data.forEach((item) => {
      $sockets.append($('<li>')
          .addClass('socket')
          .text(item.username)
      )
    });
  }

  // Sets the client's username
  const setUsername = () => {
    username = cleanInput($usernameInput.val().trim());

    // If the username is valid
    if (username) {
      $loginPage.fadeOut();
      $chatPage.show();
      $loginPage.off('click');
      $currentInput = $inputMessage.focus();
      $inputMessage.focus();

      // Tell the server your username
      socket.emit('user add', username);
    }
  }

  // Sends a chat message
  const sendMessage = () => {
    let message = $inputMessage.val();

    // Prevent markup from being injected into the message
    message = cleanInput(message);

    // if there is a non-empty message and a socket connection
    if (message && connected) {
      $inputMessage.val('');
      addChatMessage({
        username: username,
        message: message
      });

      // tell server to execute 'new message' and send along one parameter
      socket.emit('chat message', message);
    }
  }

  // Log a message
  const log = (message, options) => {
    const $el = $('<li>').addClass('log').text(message);
    addMessageElement($el, options);
  }

  // Whenever the server emits 'private message', update the chat body
  const addPrivateMessage = (data) => {
    addChatMessage(data, { private: true });
  }

  // Adds the visual chat message to the message list
  const addChatMessage = (data, options) => {

    // Don't fade the message in if there is an 'X was typing'
    const $typingMessages = getTypingMessages(data);
    options = options || { private: false };
    if ($typingMessages.length !== 0) {
      options.fade = false;
      $typingMessages.remove();
    }

    const $usernameDiv = $('<span class="username"/>')
        .text(data.username)
        .css('color', getUsernameColor(data.username));
    const $messageBodyDiv = $('<span class="messageBody">')
        .text(data.message);

    const messageClass = options.private ? "message private" : "message";
    const typingClass = data.typing ? 'typing' : '';
    const $messageDiv = $('<li class="' + messageClass +'"/>')
        .data('username', data.username)
        .addClass(typingClass)
        .append($usernameDiv, $messageBodyDiv);

    addMessageElement($messageDiv, options);
  }

  // Adds the visual chat typing message
  const addChatTyping = (data) => {
    data.typing = true;
    data.message = 'is typing';
    addChatMessage(data);
  }

  // Removes the visual chat typing message
  const removeChatTyping = (data) => {
    getTypingMessages(data).fadeOut(function() {
      $(this).remove();
    });
  }

  // Adds a message element to the messages and scrolls to the bottom
  // el - The element to add as a message
  // options.fade - If the element should fade-in (default = true)
  // options.prepend - If the element should prepend all other messages (default = false)
  const addMessageElement = (el, options) => {

    const $el = $(el);

    // Setup default options
    if (!options) {
      options = {};
    }
    if (typeof options.fade === 'undefined') {
      options.fade = true;
    }
    if (typeof options.prepend === 'undefined') {
      options.prepend = false;
    }

    // Apply options
    if (options.fade) {
      $el.hide().fadeIn(FADE_TIME);
    }
    if (options.prepend) {
      $messages.prepend($el);
    } else {
      $messages.append($el);
    }
    $messages[0].scrollTop = $messages[0].scrollHeight;
  }

  // Prevents input from having injected markup
  const cleanInput = (input) => {
    return $('<div/>').text(input).html();
  }

  // Updates the typing event
  const updateTyping = () => {
    if (connected) {
      if (!typing) {
        typing = true;
        socket.emit('typing start');
      }
      lastTypingTime = (new Date()).getTime();

      setTimeout(() => {
        const typingTimer = (new Date()).getTime();
        const timeDiff = typingTimer - lastTypingTime;
        if (timeDiff >= TYPING_TIMER_LENGTH && typing) {
          socket.emit('typing stop');
          typing = false;
        }
      }, TYPING_TIMER_LENGTH);
    }
  }

  // Gets the 'X is typing' messages of a user
  const getTypingMessages = (data) => {
    return $('.typing.message').filter(function(/* i */) {
      return $(this).data('username') === data.username;
    });
  }

  // Gets the color of a username through our hash function
  const getUsernameColor = (username) => {
    // Compute hash code
    let hash = 7;
    for (let i = 0; i < username.length; i++) {
       hash = username.charCodeAt(i) + (hash << 5) - hash;
    }
    // Calculate color
    const index = Math.abs(hash % COLORS.length);
    return COLORS[index];
  }

  // Keyboard events

  $window.keydown(event => {
    // Autofocus the current input when a key is typed
    if (!(event.ctrlKey || event.metaKey || event.altKey)) {
      $currentInput.focus();
    }
    // When the client hits ENTER on their keyboard
    if (event.which === 13) {
      if (username) {
        sendMessage();
        socket.emit('typing stop');
        typing = false;
      } else {
        setUsername();
      }
    }
  });

  $inputMessage.on('input', () => {
    updateTyping();
  });

  // Click events

  // Focus input when clicking anywhere on login page
  $loginPage.click(() => {
    $currentInput.focus();
  });

  // Focus input when clicking on the message input's border
  $inputMessage.click(() => {
    $inputMessage.focus();
  });

  // Socket events

  // Whenever the server emits 'user login', log the login message
  socket.on('user login', (response) => {
    connected = true;
    // Display the welcome message
    // var message = "Welcome to Socket.IO Chat – ";
    // log(message, {prepend: true});
    addParticipantsMessage(response);

    // update the available sockets.
    updateSockets(response.data);
  });

  // Whenever the server emits 'sockets', update the sockets body
  socket.on('sockets', (response) => {
    updateSockets(response.data);
  });

  // Whenever the server emits 'chat message', update the chat body
  socket.on('chat message', (data) => {
    addChatMessage(data);
  });

  // Whenever the server emits 'direct message', update the chat body
  socket.on('direct message', (data) => {
    addPrivateMessage(data);
  });

  // Whenever the server emits 'user joined', log it in the chat body
  socket.on('user joined', (data) => {
    log(data.username + ' joined');
    addParticipantsMessage(data);
  });

  // Whenever the server emits 'user left', log it in the chat body
  socket.on('user left', (data) => {
    log(data.username + ' left');
    addParticipantsMessage(data);
    removeChatTyping(data);
  });

  // Whenever the server emits 'typing start', show the typing message
  socket.on('typing start', (data) => {
    addChatTyping(data);
  });

  // Whenever the server emits 'typing stop', kill the typing message
  socket.on('typing stop', (data) => {
    removeChatTyping(data);
  });

  socket.on('connect', () => {
    log('you have been connected');
  });

  socket.on('disconnect', () => {
    log('you have been disconnected');
  });

  socket.on('reconnect', () => {
    log('you have been reconnected');
    if (username) {
      socket.emit('user add', username);
    }
  });

  socket.on('reconnect_error', () => {
    log('attempt to reconnect has failed');
  });

});
