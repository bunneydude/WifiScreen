import WifiScreen
import sys
import random

screen = WifiScreen.WifiScreen(sys.argv[1], int(sys.argv[2]))

width = screen.width()
height = screen.height()

# defaults:
# stroke = (0,0,0)
# fill = (255,255,255)
# text size = 1 (16px high)

screen.background(0, 59, 111) # TARDIS blue background

screen.rect(10,10,20,40)
screen.circle(100,100,20)

screen.stroke(255,0,0) # red stroke
for x in range(0, width, 10):
   screen.point(x, height/2)

for y in range(0, height, 10):
   screen.point(width/2, y)

screen.text("ABC", 25 + width/2, height - 25)

screen.setTextSize(2)
screen.stroke(255,69,0)
screen.text("ABC", 50 + 8 + width/2, height - 50 - 8)

screen.setTextSize(3)
screen.stroke(255,99,71)
screen.text("ABC", 75 + 16 + width/2, height - 75 - 16)

screen.setTextSize(4)
screen.stroke(240,128,128)
screen.text("ABC", 100 + 24 + width/2, height - 100 - 24)

screen.setTextSize(5)
screen.stroke(255,127,80)
screen.text("ABC", 125 + 30 + width/2, height - 125 - 30)

screen.stroke(127, 255, 0)
for y in range(height/2, height, 10):
   screen.line(0, y, width, 0)

for x in range(0, width/2, 10):
   screen.line(x, height, width, 0)

screen.image("bunneyGreen.png",50,10)


print("done")







