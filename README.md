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



Animation
---------

Previously, the only way to make an object move across the screen was to draw, clear the screen (with a background() call), then draw at the new position. While this works fine on a LCD connected physically, this wireless connection has enough latency for a human to detect. To correct this, you can now specify an object's number and the app will update it's value in the linked list instead of creating a new element. The python file 'animationDemo' has an example. The code assumes you aren't going to do something silly like use the object number of a rectangle when trying to draw a circle. Doing so would cause an out of bounds exception and probably crash the app.


Known Issues
------------

1) After connecting to a server, the app has to be closed to return to the main screen and choose a new connection. This isn't a bug - it just isn't done currently.

2) The libGDX Java app internally maintains a linked list to draw elements to the screen. If you were to try and do an animation like the pong example in the TFTLibrary (repeatedly drawing a shape, drawing a shape the color of the background over it, then drawing a new shape elsewhere) the app could maybe exhaust its heap eventually. The linked list is cleared when a call to 'background(r, g, b)' is made, however.

Planned Features
----------------

1) Adding functions to WifiScreen.py to include more of libGDX's shapes (cone, cube, ellipse, polygon, triangle)

2) Create an Arduino library so this class can be accessed from the sketch in a more platform-independent manner.
