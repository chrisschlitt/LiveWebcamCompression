# PennSkype

The MenuView is the initial menu interface for the application.
It simply includes a 'join' button that allows the user to connect
to a web chat. The DisplayView class is the main user interface for when
the webchat is active. On the left side of the frame is the user's
local webcam. On the right is the incoming stream of the connected
webcam. The interface includes radio buttons that allow the user to 
choose the level of compression to use when sending video. The user
can choose between '1/2', '1/4' and 'None'. 'None' will send the stream
without any compression of images so it is expected to be the slowest
of the three options. Conversely, if the user chooses 1/4 compression
the compression will be the most lossy but it should offer the
most responsive video between computers. The interface also allows the user to 
choose between broadcasting in black and white or color. Choosing black and
white decreases the amount of data sent so the performance is expected
to be better when streaming in black and white mode. The interface also
includes an 'End' button which will close the connection between computers
and shut down the application.

The application is designed with MVC in mind. We have several views representing
the different pages the user interacts with. The WebcamController class
handles interactions between the user and the interface and alerts the model of 
any actions the user has taken such as switching color mode or compression. The model
then handles the main functions of the application and returns the processed images
to the be displayed by the view. The WebcamStream class contains the main method
and serves as the driver for the application. It initializes the views, the controller
and the model.