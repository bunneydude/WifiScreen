import socket
import imghdr
import struct

#command codes
BACKGROUND = 20 
STROKE = 21 
NOSTROKE = 22  
FILL = 23   
NOFILL = 24 
TEXT = 25   
SETTEXTSIZE = 26 
POINT = 27        
LINE = 28        
RECT = 29       
WIDTH = 30     
HEIGHT = 31   
CIRCLE = 32  
IMAGE = 33      
NEWIMAGE = 34
GETIMAGE = 35
ACK = 36
NACK = 37
BUFFERSIZE = 38
IMGTYPES = {'png':0, 'jpeg':1, 'bmp':2}


class WifiScreen():

   def __init__(self, host=0, port=2000):
      self.validImages={}
      self.imgIndex = 0

      if(host == 0):
         self.host = socket.gethostbyname(socket.gethostname()) # not super platform independent
      else:
         self.host = host

      self.port = port

      self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
      self.sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1) # this is needed so the connection can be reused
      self.sock.bind((host,port))
      self.sock.listen(1) #for now, just one connection
      print("Waiting for connection on IP " + str(self.host) + ", port " + str(self.port) + "\n")
      self.conn,self.data = self.sock.accept()
      print("Connection: " + str(self.conn) + "\n")
      print("Info: " + str(self.data) + "\n")

      temp = ord(self.receive()) # wait for client to signal that it's ready
      if(temp != ACK):
         print("Expected " + str(ACK) + ", got " + str(temp))

   def __del__(self):
      self.conn.close()
      self.sock.close()

   def send(self, cmd, payload=[], text="", name=0):
      toSend = '' # format is <cmd - 1 byte>, <payload length - 2 bytes>, <text length - 2 bytes> <payload>, <text>
      #each entry in the payload is 2 bytes
      #each entry in text is 1 bytes
      toSend += chr(cmd)
      toSend += struct.pack('>h', len(payload) )
      toSend += struct.pack('>h', len(text) )
      toSend += struct.pack('>h', name)

      if( ( (type(payload) is list) or (type(payload) is str) ) and (type(text) is str) ):
         if(type(payload) is list):
            payload = struct.pack('>'+str(len(payload))+'h', *payload)

         toSend += payload
         toSend += text
         self.conn.send(toSend)
      else:
         print("Bad types. Payload was: " + str(type(payload)) +", Text was: " + str(type(text)) )

   def receive(self): 
      data = self.conn.recv(1024)
      return data

   def background(self, red, green, blue):
      self.send(BACKGROUND, [red, green, blue])
   
   def stroke(self, red, green, blue):
      self.send(STROKE, [red, green, blue])      

   def noStroke(self):
      self.send(NOSTROKE)

   def fill(self, red, green, blue):
      self.send(FILL, [red, green, blue])

   def noFill(self):
      self.send(NOFILL)

   def text(self, text, xPos, yPos, objectName=0):
      self.send(TEXT, [xPos, yPos], text, name = objectName)

   def setTextSize(self, size):
      if(size in range(1,6)): # valid 'size' values are [1,5]
         self.send(SETTEXTSIZE, [size-1])
      else:
         raise NameError('Text size must be in range [1,5]. It was ' + str(size) )

   def point(self, xPos, yPos, objectName=0):
      self.send(POINT, [xPos, yPos], name = objectName)

   def line(self, xStart, yStart, xEnd, yEnd, objectName=0):
      self.send(LINE, [xStart, yStart, xEnd, yEnd], name = objectName)

   def rect(self, xPos, yPos, width, height, objectName=0):
      self.send(RECT, [xPos, yPos, width, height], name = objectName)      

   def width(self):
      self.send(WIDTH)
      temp = map(ord,self.receive())
      return ((temp[1]<<24) + (temp[2]<<16) + (temp[3]<<8) + (temp[4]))

   def height(self):
      self.send(HEIGHT)
      temp = map(ord,self.receive())
      return ((temp[1]<<24) + (temp[2]<<16) + (temp[3]<<8) + (temp[4]))

   def bufferSize(self):
      self.send(BUFFERSIZE)
      temp = map(ord,self.receive())
      return ((temp[1]<<24) + (temp[2]<<16) + (temp[3]<<8) + (temp[4]))

   def circle(self, xPos, yPos, radius, objectName=0):
      self.send(CIRCLE, [xPos, yPos, radius], name = objectName)

   def image(self, imgName, xPos, yPos, objectName=0, overwrite=0,):
      if( (imgName not in self.validImages) or (overwrite == 1) ): # if new image or want to overwrite, send image file
         self.sendImage(imgName, overwrite)

      #either way, specify location and index for the image to be drawn
      self.send(IMAGE, [xPos, yPos, self.validImages[imgName] ], name = objectName)

   def sendImage(self, imgName, overwrite=0):
      # you shouldn't need to call 'sendImage' directly. Just use 'image'
      imgType = imghdr.what(imgName) # get file type
      if(imgType in IMGTYPES): # make sure is one of bmp, jpeg, png
         f=open(imgName,'rb')

         index = self.imgIndex
         if(overwrite == 1):
            if(imgName in self.validImages):
               index = self.validImages[imgName] # use index of the image we want to overwrite
            else:
               raise NameError('File does not exist: ' + imgName)
         else:
            self.imgIndex += 1 #if not overwriting, use the next available index
               
         self.send(NEWIMAGE,[IMGTYPES[imgType], overwrite, index]) # specify control info for image transfer

         line = f.read(2**13-1)
         while(len(line) != 0):
            self.send(GETIMAGE, payload=[], text=line) #sent bytes of image in (2^13)-1 byte chunks
            line = f.read(2**13-1)

         self.send(GETIMAGE) #marks completion of file transfer
         f.close()

         self.validImages[imgName] = index # add image and its index to list of cached images on client
         print("Added image " + imgName + " as index " + str(index))
      else:
         print("Unknown file type: " + str(imgType))
         
