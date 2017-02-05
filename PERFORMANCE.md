# Compared with quic

+ Go through https://github.com/google/proto-quic to setup quic.
+ Go through README.md for mocket sample client and server.

Comparision is made between quic http (1645 Bytes) vs mocket native(2KB) one way

To run echo Server: ./gradlew echoServer
To run file Client: ./gradlew fileClient -PfileName=data/2KB

####Test Case 1:

    Conditions: localhost, 0ms additional delay, 0 % packet-loss
    Quic time taken(s): 0.163, 0.147, 0.133, 0.140, 0.144
    Mocket time taken(s): 0.081, 0.048, 0.080, 0.049, 0.055

####Test Case 2:

    Conditions: localhost,  100 additional delay, 10% packet-loss
    Quic time taken(s): 1.345, 0.982, 0.960, 1.132, 0.957
    Mocket time taken(s): .079, 5.127, .057, 0.052, 0.042

####Test Case 2:

    Conditions: localhost,  300 additional delay, 15% packet-loss
    Quic time taken(s): 2.673, 1.366, 5.614, 3.910, 2.676
    Mocket time taken(s): 5.576, 11.552, .043, .056, 5.576
