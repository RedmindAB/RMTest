rmtest-selenium-server
======

:exclamation: **IMPORTANT:** :exclamation: <br>
*If you are using Internet Explorer, make sure you download the 32-bit IEServerDriver.exe file.*
*Otherwise "webelement.sendkeys(...)" takes about 4 seconds for each character.*

This module contains a standalone version of the selenium server together with:

+ a custom servlet required by rmtest-selenium in order to query the hub.
+ rmtest-logback for proper logging.

#### the server

the usage is exactly the same than any normal selenium server.

to start a hub:

    java -jar rmtest-selenium-server.jar -role hub

to start a node:

    java -jar rmtest-selenium-server.jar -role node -nodeConfig your-configuration.json

with for example:
```json
{
    "capabilities": [{
	"seleniumProtocol": "WebDriver",
	"browserName": "chrome",
	"osname":"OSX",
	"description": "OSX chrome",
	"maxInstances": 1,
	"deviceName": "AnApple",
	"platform" : "MAC"
    }],
    "configuration": {
        "proxy": "org.openqa.grid.selenium.proxy.DefaultRemoteProxy",
        "maxSession": 5,
        "port": 6650,
        "host": "localhost",
        "register": true,
        "registerCycle": 5000,
        "hubPort": 4444,
        "hubHost": "localhost"
    }
}
```

see http://www.seleniumhq.org/docs/ for more information about the selenium server.

#### logging:

By default, the server is going to look for a logback.xml file in the current folder.

However, you can specify a different file with the 'logback.configurationFile' system property, for example:

    java -Dlogback.configurationFile=hub-logback.xml -jar rmtest-selenium-server.jar -role hub

see http://logback.qos.ch/manual/configuration.html for more information about logback.
