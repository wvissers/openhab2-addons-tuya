# Tuya binding

This is a binding for devices that are controlled using the TCP/UDP Tuya protocol over the local WiFi network. Devices using this protocol are available in a wide variety of brands. If you have devices you can control with apps like TuyaSmart, Smart Life or LSCSmartControl are likely to be compatible.
In The Netherlands, a series of compatible product are available from the Action stores, labeled with the brand LSC Smart Connect. 

This binding is based on the reverse engineering done for the tuyapi project. Please make sure to check out [https://github.com/codetheweb/tuyapi](https://github.com/codetheweb/tuyapi) if you want to know more about Tuya devices. This is also a great resource for other Tuya based software project.

Please note that this binding is under construction. Let me know what you think about this, but don't expect everything to work perfectly yet. The binding is offered 'as is' with no warranty of any kind. Use it at your own risk.

## Setup

Since the local device communication is encrypted, you will need to obtain the encryption keys and the device ids. Every Tuya device has its own key, so this is needed for every new device you want to add. These keys can be found by analyzing the traffic between the Tuya app and the Tuya cloud service. Since this communication is also encrypted using https, a little effort is needed here. The 'trick' is to use a computer as proxy server for your mobile device. There is good free software available that allows you to view the requests passing the proxy server in readable text.

Look at these [setup instructions using Windows](http://www.htgsd.com/information-technology/apple/homekit/how-to-capture-tuya-lan-homebridge-device-devid-and-key-on-windows-10/) or these [setup instructions](https://github.com/codetheweb/tuyapi/blob/master/docs/SETUP.md) to get the encryption keys and device ids. 

Please note, that usually only a single connection can be made with every device. It might not work properly if you use multiple local connections.

## Supported Things

Currently it supports the LSC Smart Connect Power Plug,  Smart LED white and color ambiance lamps, and Smart Filament LED lamps. Other devices that use the same communication scheme might also work.

## Quick start

For a quick start to review this binding, proceed as follows:

1. Download the file [org.openhab.binding.tuya-2.4.0-SNAPSHOT.jar](https://github.com/wvissers/openhab2-addons-tuya/raw/master/target/org.openhab.binding.tuya-2.4.0-SNAPSHOT.jar) in the target folder of this repository.
2. Copy this file to the addons folder of the operational openHAB system.
3. Use the Paper UI to create a new thing, using the "Tuya" binding.
4. With the Paper UI, set the devId, localKey and version. 
5. Add an item as desired.
6. Try out the item in e.g. the Basic UI.

## Discovery

Auto-discovery is not applicable to this binding. However, there is a listener to discover the IP addresses of registered things. These are visible in the Paper UI as properties of the things.

## Binding Configuration

There is no binding configuration necessary. Place the tuya jar file into the addons directory as described above and the binding will be supported.

## Thing Configuration

Configuring the tuya things with the Paper UI is probably the best way to do it. For each device you will need at least the devId or gwId, localKey and version.

## Channels

The channels can be retrieved from the Paper UI after configuring. They should be reasonably self-explaining.


## Full Example

Todo..
