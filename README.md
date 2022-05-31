## Nefis
Nefis is an adaptive video transmission system based on network coding designed on the Android platform, which realizes the functions of video frame slicing operation, network coding and decoding, and video playback.The system takes video transmission in the D2D communication environment as the application background. By combining SVC with network coding technology, the system can significantly and adaptively adjust the network coding redundancy and video resolution according to the current network conditions when users perform video-on-demand.


## Implementation of Network Coding Technology in Nefis
The finite field implementation algorithm used in the core module of network coding scheme in Nefis is Look-up Table Algorithm, and the finite field is GF(2<sup>8</sup>).More implementation details you can find in [Moncode](https://ieeexplore.ieee.org/stamp/stamp.jsp?tp=&arnumber=9613561).Moncode is our open source C library designed for the software implementation of network coding over heterogeneous platforms.Given the different operation and storage requirements of heterogeneous platforms, Moncode provides a variety of GF(2<sup>n</sup>)finite field implementation algorithms with different time and space overheads.Users can determine the order of the finite field and choose the appropriate finite field implementation algorithm based on the application requirements and the performance of the device/platform.


## Building Guides:
### System Development Environment：
>Developed in Java based on the Android Studio platform, Windows 10.

### System operating environment：
>The Nefis system will eventually run on a mobile device. To ensure smooth operation, the Android version of the mobile device should be: Android 6.0.  

### System operation instructions：
#### As a Nefis_server
>The mobile device acting as Nefis_server needs to open its own WiFi hotspot and allow multiple mobile devices around acting as Nefis_client to access it, forming a D2D communication environment to transfer video files.
#### As a Nefis_client
>Nefis_client turns on WiFi and connects to the hotspot provided by the Nefis_server that has the resources the user wants to obtain. This experiment assumes that there is only one Nefis_server.


## System flow
This is a simple flowchart of our program.
![a simple flowchart](https://github.com/NJUPT-JunYin/Nefis/blob/main/a%20simple%20flowchart%20of%20our%20program.png)

## Test results
[Here](https://github.com/NJUPT-JunYin/Nefis/tree/main/Tom%20and%20Jerry) is a set of our test results to show.
