# Tuya binding

This is a binding for devices that are controlled using the TCP/UDP Tuya protocol over the local WiFi network. Devices using this protocol are available in a wide variety of brands. If you have devices you can control with apps like TuyaSmart, Smart Life or LSCSmartControl are likely to be compatible.
A series of compatible product are available from the Action stores throughout Europe, labeled with the brand name LSC Smart Connect. 

This binding is based on the tuyapi project. Please make sure to check out [https://github.com/codetheweb/tuyapi](https://github.com/codetheweb/tuyapi) if you want to know more about Tuya devices, or are interested in other implementations to control these devices. They did a great job creating this! 

This binding implementation is still experimental. Let me know what you think about this, but don't expect everything to work perfectly yet. The binding is offered 'as is' with no warranty of any kind. Use it at your own risk. 

The way the Tuya devices are designed makes it very difficult to create a 100% stable binding using the local API. Some guidelines to increase the stability:

- After configuring the things with PaperUI, leave the binding some time (about 5 minutes minimum) to stabilize.
- Leave the devices powered on. Switching lights on and off with a regular light switch is discouraged.
- When updating the binding by copying a new jar file into the addons folder, restarting openHAB is encouraged.

Due to the fact that it is apparently not going to be stable enough I will discontinue development. Feel free to
use the code for your own purposes. 
 

## Setup

Since the local device communication is encrypted, you will need to obtain the encryption keys and the device ids. Every Tuya device has its own key, so this is needed for every new device you want to add. These keys can be found by analyzing the traffic between the Tuya app and the Tuya cloud service. Since this communication is also encrypted using https, a little effort is needed here. The 'trick' is to use a computer as proxy server for your mobile device. There is good free software available that allows you to view the requests passing the proxy server in readable text.

Look at these [setup instructions using Windows](http://www.htgsd.com/information-technology/apple/homekit/how-to-capture-tuya-lan-homebridge-device-devid-and-key-on-windows-10/) or these [setup instructions](https://github.com/codetheweb/tuyapi/blob/master/docs/SETUP.md) to get the encryption keys and device ids. 

Please note, that usually only a single connection can be made with every device. It might not work properly if you use multiple local connections.

## Supported Things

Currently it supports the LSC Smart Connect Power Plug,  Smart LED white and color ambiance lamps, Smart Filament LED lamps and Smart Sirens. Other devices that use the same communication scheme might also work. Please let me know when it works with other Tuya based devices as well.

## Quick start

For a quick start to review this binding, proceed as follows:

1. Download the file [org.openhab.binding.tuya-2.4.0-SNAPSHOT.jar](https://github.com/wvissers/openhab2-addons-tuya/raw/master/target/org.openhab.binding.tuya-2.4.0-SNAPSHOT.jar) in the target folder of this repository.
2. Copy this file to the addons directory of openhab2.
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

After adding the binding add the thing using the Paper UI in the Configuration -> Things section with the + sign. Select the Tuya binding and the right thing, and enter the devId and localKey. You may also want to choose a more convenient thing ID than the default one. The device should report ONLINE in the Paper UI after adding. 

Next, create items to link the thing to. Create a file (e.g. tuya.items) in the conf/items directory of openhab2. Add something like the next example. This assumes you have a power plug with thing id "plug", a color LED lamp with thing id "led" and a siren with thing id "siren".

```
Switch PowerPlug            "Power Plug [%s]"   { channel="tuya:powerplug:plug:power" }

Switch ColorLampPower       "Color lamp [%s]"   { channel="tuya:colorled:led:power" }
Switch ColorLampMode        "Color mode [%s]"   { channel="tuya:colorled:led:colorMode" }
Dimmer ColorLampBrightness  "Brightness"        { channel="tuya:colorled:led:brightness" }
Dimmer ColorLampTemp        "Color temperature" { channel="tuya:colorled:led:colorTemperature" }
Color  ColorLampColor       "Color"             { channel="tuya:colorled:led:color" }

Switch SirenAlarm           "Siren alarm [%s]"  { channel="tuya:siren:siren:alarm" }
Dimmer SirenVolume          "Volume [%d%%]"     { channel="tuya:siren:siren:volume" }
Number SirenDuration        "Duration [%d sec]" { channel="tuya:siren:siren:duration" }

```

Next, to show it in the basic UI, create a sitemap file in the conf/sitemaps directory. You may also add the Frames to an already existing sitemap.

```
sitemap tuya label="Tuya demo"
{
    Frame label="Power plug" {
        Switch item=PowerPlug
    }
    Frame label="Color LED lamp" {
        Switch item=ColorLampPower
        Switch item=ColorLampMode
        Slider item=ColorLampBrightness
        Slider item=ColorLampTemp
        Colorpicker item=ColorLampColor
    }
    Frame label="Siren" {
        Switch item=SirenAlarm
        Slider item=SirenVolume
        Setpoint item=SirenDuration minValue=1 maxValue=30 step=1
    }
}
```

After this, you should be able to view the sitemap with the Basic UI (/basicui/app?sitemap=tuya) and control the devices. 
