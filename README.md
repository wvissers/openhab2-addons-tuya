# Tuya binding

This is a binding for devices that are controlled using the TCP/UDP Tuya protocol over the local WiFi network. Devices using this protocol are available in a wide variety of brands. If you have devices you can control with apps like TuyaSmart, Smart Life or LSCSmartControl are likely to be compatible.
In The Netherlands, a series of compatible product are available from the Action stores, labeled with the brand LSC Smart Connect. 

This binding is based on the reverse engineering done for the tuyapi project. Please make sure to check out [https://github.com/codetheweb/tuyapi](https://github.com/codetheweb/tuyapi) if you want to know more about Tuya devices.

Please note that this binding is under construction. Let me know what you think about this, but don't expect everything to work perfectly. The binding is offered 'as is' with no warranty of any kind. Use it at your own risk.

## Setup

Unfortunately, the setup may be somewhat complicated, since the protocol is encrypted with AES, and the keys for this communication must be obtained with a little effort to decode the traffic of the app to the cloud. 

Look at these [setup instructions](https://github.com/codetheweb/tuyapi/blob/master/docs/SETUP.md) to get the encryption keys and device ids.

## Supported Things

Currently it supports the LSC Smart Connect Power Plug only. More devices will hopefully soon be supported.

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
