Settings on the TV:
-------------------
Go to Settings > All Settings
- Go to Network > LG Connect App --> activate!
- Go to General > TV Mobile --> activate!


Retrieve MAC and IP adress:
---------------------------
Go to Settings > All Settings
- Go to Network > Ethernet or WIFI (depends of your connection)

Click on it, and then the MAC & IP adress are displayed.



Define Static ip adress:
------------------------

Your router may always give the same IP adress to your TV, but it's recommanded to define a static one).
From here (networks ethernet or wifi menu) you can define a static IP adress (just uncheck dynamic and let the config already put).
But you can also let the "dynamic" part here, and define a static ip from your router interface.



Client Key:
-----------

Need to retrieve a client key from your TV.

For that, the first time, you must instantiate without client key parameter
public LGWebOS(String macAddress, String ipAddress);
and in LGWebOSCore enable debug output (first internal variable).

On your TV you will have a popup, which prompt you to accept the connection or not.
Once accepted, the LGWebOS will ouptut on console the client key (thanks to debug enable).
You can now deactivate debug output from LGWebOSCore.

You must copy it and use it for all next connection when instantiate with:
public LGWebOS(String macAddress, String ipAddress, String clientKey);