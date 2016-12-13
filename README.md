# PennSkype

The MenuView is the initial menu interface for the application. It simply includes a 'join' button that allows the user to connect to a web chat. The DisplayView class is the main user interface for when the webchat is active. On the left side of the frame is the user's local webcam. On the right is the incoming stream of the connected webcam. The interface includes radio buttons that allow the user to choose the level of compression to use when sending video. The user can choose between '1/2', and 'None'. 'None' will send the stream without any compression of images so it is expected to be the slowest of the two options. Conversely, if the user chooses 1/2 compression the compression will be lossy but it should offer the
most responsive video between computers. The interface also allows the user to choose between broadcasting in grayscale or color. Choosing grayscale decreases the amount of data sent so the performance is expected to be better when streaming in black and white mode. The interface also includes an 'End' button which will close the connection between computers
and shut down the application.

As the live feed from the webcam is split into frames, the images are split into the Red, Green and Blue channels and passed through a real time compression algorithm. Prinicipal Componenet  Analysis(PCA) based compression is performed separately on the three channels . PCA involves the projection of the signal onto a basis of eigen vectors V such that V diagonalizes its covariance matrix. The 2D matrix of colors was first converted into a 2 x n row 2D array and the JAMA library's SVD implementation was used to find the covariance matrix. From here the the rotation matrix was calculated and a simple Matrix Multiplication gave the Principal Components. This translated the amount of data being sent to half of the original along with parameters like the rotation factor, height , width , compression ratio and color. For an RGB image the compression for each of the color channels were done on separate threads to utilize concurrancy. Gray scale images required only a single channel compression and lesser data to be transmmitted. It was found that further compression from 1/2 drastically deteriorated the quality of the original image. For reconstruction, the reverse mathematical process was performed again of separate threads for the Red , green and blue channels and the original image was reconstructed using the parameters sent with the image. 

The application is designed with MVC in mind. We have several views representing the different pages the user interacts with. The WebcamController class handles interactions between the user and the interface and alerts the model of any actions the user has taken such as switching color mode or compression. The model then handles the main functions of the application and returns the processed images to the be displayed by the view. The WebcamStream class contains the main method and serves as the driver for the application. It initializes the views, the controller and the model.

The Connection class manages everything from the discovery connection between two computers to the sending/receiving of stream data to the closing of the connection.  The connection uses auto-discovery to establish the connections between the two computers, meaning they can connect without entering IP addresses.  This initiates by each computer listening for packets on a specific port (packet port), followed by both computers sending out a discovery packet to each IP address on the network (including non-local subnets). When a computer receives a discovery packet, it immediately stops listening for discovery packets, sends a discovery_response packet, and converts itself into the “server”.  Once the other computer receives the discovery_response packet, it also stops listening for discovery packets, converts itself into the “client”, and now both computers know the IP address of the other. Next, the client and server initiate the opening of the streaming connection, by communicating via status packets. During this process, two threads are created (send and receive) which begin a loop of of checking the incoming and outgoing queues and sending/receiving objects over the ObjectStream. At this point, the connection process is complete and the method returns, but not before creating one more thread called end (which specifically listens for end packets).  When either user ends the connection, the computer will send an end packet to the other computer, and both will begin the process of closing the connection, and ending the program.

There is one race case with this setup, where both computers receive the discovery packet almost simultaneously.  When this happens, both computers will stop listening for discovery packets (and thus discovery_response packets), so when the servers send their discovery_response packets (which is normal behavior after receiving a discovery packet) the packets won’t be received, and they will not continue connecting.  To fix this issue, whenever a computer receives a discovery packet, in addition to sending out a discovery_response packet, it also sends out a fix packet which contains the timestamp at which the computer received the discovery packet. Fix packets are normally ignored, except in the case that the computer became the server, this tells the system that this race condition has been met. If this happens, both computers compare the time they received the discovery packet to the time in the fix packet.  If the computer’s timestamp is larger, it doesn’t do anything, but if it is smaller, the computer converts itself into the client, and the connection process continues normally.

In addition to the sending and receiving of data, the connection also manages the differencing library.  The differencing library was writted specifically with byte arrays of images in mind.  The process begins by calculating the numerical difference between each pixel in the array, and populates a third byte array.  This third byte array is then compressed (since it contains a 0 for every pixel that has not changed) and this difference is then sent across the connection.  The first frame is sent in full, and then both computers keep a record of the previous frame, so only differences are send from there on.  In addition, in the case that frames are dropped, a full frame is sent every 200 frames.

Both the normal and the race condition described here are outlined in sequence level diagrams.

——
Execution steps: Execute PennSkype.jar on two different computers with a webcam. Click the PennSkype icon on both computers (This does not have to be simultaneously clicked). You will see the screen change to connecting as it looks for the other computer. Once found your web conference should begin on both computers. Clicking on the icons will modify the functionality. Click end to stop the conference.

In addition to including the code, an executable jar is included, allowing the program to be run outside of eclipse and without importing additional libraries.  In order to create this, the program was exported in eclipse into an “executable jar” with the external libraries included.

All external libraries required to run this program are located in the jars folder.  To add them to the eclipse project, edit the properties of the project, edit the build path, add external jars, and select every external library in the jars folder.

The project outline document is also included.

Github Repo: https://github.com/chrisschlitt/WebcamCompression

——

Completed Requirements:
Group 1:
 -Formal Design (Sequence Diagram)
 -Interfaces (Model, Good coupling between classes)
 -Design Pattern (MVC)
Group 2:
 -Data Structures (BlockingQueue)
 -Java Graphics (UI)
 -Multithreading (Connection, Taking Pictures, Compression/Reconstruction, etc.)
 -Advanced Topics (External Libraries, Networking, Computer Vision)
 
——
