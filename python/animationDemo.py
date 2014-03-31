import WifiScreen
import sys
import random
import time

if sys.version_info[:3] != (2,7,3):
   print("Needs Python version 2.7.3 for Galileo compatibility.")
   sys.exit()

screen = WifiScreen.WifiScreen(sys.argv[1], int(sys.argv[2]))

width = screen.width()
height = screen.height()

# defaults:
# stroke = (0,0,0)
# fill = (255,255,255)
# text size = 1 (16px high)

screen.background(0, 59, 111) # TARDIS blue background

width = screen.width()
delta = 5
sign = 1
x=0

screen.stroke(255,255,255)

screen.image("bunneyGreen.png",x,400)
image1 = screen.bufferSize()

screen.rect(0,200,20,20)
rect1 = screen.bufferSize()

screen.circle(0,100,20)
circle1 = screen.bufferSize()

screen.point(0, 50)
point1 = screen.bufferSize()

screen.line(x, height, width, 0)
line1 = screen.bufferSize()

text = ''
left = "going left"
right = "going right"

text = right

screen.text(text, 0, 300)
text1 = screen.bufferSize()

while(1):
   screen.image("bunneyGreen.png",x,400,image1)
   screen.rect(x,200,20,20,rect1)
   screen.circle(x,100,20,circle1)  
   screen.point(x, 50, point1)
   screen.line(x, height, width, 0, line1)
   screen.text(text, x, 300, text1)

   x += sign*delta
   if(x == 955):
      sign = -1
      text = left
   elif(x == 5):
      sign = 1
      text = right
   time.sleep(1/60.0)
#   screen.background(0, 59, 111)

print("done")
