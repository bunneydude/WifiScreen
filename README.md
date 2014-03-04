WifiScreen
==========

Usage - Server side
-------------------

The python class, WifiScreen, attempts to faithfully implement the Arduino TFTLibrary. Most function calls are identical (e.g. rect(xStart, yStart, width, height)). However, one noticeable difference is how images are handled.

Three image types are supported (PNG, BMP, and JPG). To draw an image, you call image("imageName.png", xPosition, yPosition). If the image has never been used before, the server will send the image to the client. Subsequent calls to draw image "imageName.png" will not incur another image transfer. Instead, the client uses the version of "imageName.png" it has locally stored.

If you made a change to "imageName.png" and want to update the client version, you would call image("imageName.png", xPosition, yPosition, 1). This will set the 'overwrite' flag and trigger a new version of "imageName.png" to be sent and replace the client's old version.


Usage - Client side
-------------------

When the app launches, enter the IP address and port number for the server you want to connect to. To avoid timeouts, it is advisable to start the server before trying to connect the client.


Known Issues
------------

1) After connecting to a server, the app has to be closed to return to the main screen and choose a new connection. This isn't a bug - it just isn't done currently.

2) Trying to do animation (e.g. a box bouncing around) flickers. I determined it isn't the wireless connection so I'll have to tweak the Java some to handle the notion of moving an object to a new location instead of just clearing the screen and creating a new object every time.

3) The libGDX Java app internally maintains a linked list to draw elements to the screen. If you were to try and do an animation like the pong example in the TFTLibrary (repeatedly drawing a shape, drawing a shape the color of the background over it, then drawing a new shape elsewhere) the app could exhaust its heap. The linked list is cleared when a call to 'background(r, g, b)' is made, however. The takeaway is that the library currently is not optimized for animation. 

Planned Features
----------------

1) Optimization for animation

2) Adding functions to WifiScreen.py to include more of libGDX's shapes (cone, cube, ellipse, polygon, triangle)

3) Create an Arduino library so this class can be accessed from the sketch in a more platform-independent manner.
