package com.dannyhurley;

import com.bitwig.extension.api.util.midi.ShortMidiMessage;
import com.bitwig.extension.callback.ShortMidiMessageReceivedCallback;
import com.bitwig.extension.controller.api.*;
import com.bitwig.extension.controller.ControllerExtension;
import com.dannyhurley.devices.MappableDevice;
import com.dannyhurley.parametermapper.ParameterMapper;

import java.util.Arrays;
import java.util.List;

public class BitwigParameterMonitorExtension extends ControllerExtension
{
   public List<MappableDevice> mappedDevices;
   public CursorTrack cursorTrack;

   protected BitwigParameterMonitorExtension(final BitwigParameterMonitorExtensionDefinition definition, final ControllerHost host)
   {
      super(definition, host);
   }

   private void mapDevices() {
      // parse JSON into ArrayList of MappableDevices
      this.mappedDevices = ParameterMapper.readAndMapParams(getHost(), this.cursorTrack);
   }

   @Override
   public void init()
   {
      final ControllerHost host = getHost();      

      mTransport = host.createTransport();
      host.getMidiInPort(0).setMidiCallback((ShortMidiMessageReceivedCallback)msg -> onMidi0(msg));
      host.getMidiInPort(0).setSysexCallback((String data) -> onSysex0(data));

      // TODO: Perform your driver initialization here.
      // For now just show a popup notification for verification that it is running.
      host.showPopupNotification("Bitwig Parameter Monitor Initialized");

      TrackBank mainTrackBank = host.createMainTrackBank(1, 0, 0);
      CursorTrack cursorTrack = host.createCursorTrack("cursor track", "cursor track", 0, 0, true);
      mainTrackBank.followCursorTrack(cursorTrack);

      this.cursorTrack = cursorTrack;

      // TODO move this into its own config reader / writer

      int numberOfTracks = 5;
      TrackBank allTracksBank = host.createTrackBank(numberOfTracks, 2, 2);

      // DeviceMatcher deviceIdMatcher = host.createVST3DeviceMatcher("41727475415649534B61743150726F63");

      // for each track

      // for each device

      // look up device info in DB

      // create parameter

      host.println("::::::::::::::::::::::::::::::::::::::");
      for(int trackIndex = 0; trackIndex < allTracksBank.getSizeOfBank(); trackIndex++) {
//      for(int trackIndex = 0; trackIndex < 1; trackIndex++) {
         Track track = allTracksBank.getItemAt(trackIndex);
         int trackDisplayNumber = trackIndex + 1;
         track.name().markInterested();
         track.name().addValueObserver((name) -> {
            if (!name.equals("")) {
               host.println("Track " + trackDisplayNumber + " Name:");
               host.println(name.toLowerCase());
            }
         });

         int numberOfDevicesPerTrack = 100;
         DeviceBank trackDeviceBank = track.createDeviceBank(numberOfDevicesPerTrack);
         for (int deviceIndex = 0; deviceIndex < trackDeviceBank.getSizeOfBank(); deviceIndex++) {
//         for(int deviceIndex = 1; deviceIndex < 2; deviceIndex++) {
//            Device device = trackDeviceBank.getDevice(deviceIndex);

//            int deviceDisplayNumber = deviceIndex + 1;
//            device.name().markInterested();
//            device.name().addValueObserver((name) -> {
//               host.println(name.toLowerCase());
//            });
//
//            device.hasLayers().addValueObserver((hasLayers) -> {
//               host.println("\nhas layers?: " + hasLayers);
//            });

//
//            device.addDirectParameterIdObserver((name) -> {
//               for(String n : name) {
//                  host.println(n);
//               }
//            });

//            device.addDirectParameterNameObserver(1000, (id, name) -> {
//               // make a matcher for each mappable device
//
//               // create a device bank with a single entry and filter out all other devices
//               // on the selected track using the matcher
//               DeviceBank singleMatchedDeviceBank = track.createDeviceBank(1);
//               singleMatchedDeviceBank.setDeviceMatcher(deviceIdMatcher);
//               Device matchedDeviceFromBank = singleMatchedDeviceBank.getDevice(0);
//               SpecificPluginDevice specificDevice = matchedDeviceFromBank.createSpecificVst3Device("41727475415649534B61743150726F63");
//
//               Parameter p = specificDevice.createParameter(Integer.parseInt(id));
//               p.markInterested();
//
//               host.println(id);
//            });
         }

         host.println("::::::::::::::::::::::::::::::::::::::");
      }

      mapDevices();

      // TODO this seems to have a performance issue with VSTs, will be nice to have in the future.
//      final HardwareSurface midiTouchbarHardwareSurface = host.createHardwareSurface();
//      final AbsoluteHardwareKnob firstSlider = midiTouchbarHardwareSurface.createAbsoluteHardwareKnob("SLIDER_1");
//
//      int midiChannel = 0; // MIDI channel 1
//      firstSlider.setAdjustValueMatcher(midiInPort.createAbsoluteCCValueMatcher(midiChannel, 0));
//      Parameter valhallaDelayMix = valhallaDelay.mappedParameters.get(ValhallaDelay.ParameterName.MIX);
//      firstSlider.setBinding(valhallaDelayMix);

      getHost().showPopupNotification("INITIALIZED");
   }

   @Override
   public void exit()
   {
      // TODO: Perform any cleanup once the driver exits
      // For now just show a popup notification for verification that it is no longer running.
      getHost().showPopupNotification("Bitwig Parameter Monitor Exited");
   }

   @Override
   public void flush()
   {
      // TODO Send any updates you need here.
   }

   /** Called when we receive short MIDI message on port 0. */
   private void onMidi0(ShortMidiMessage msg) 
   {
      // TODO: Implement your MIDI input handling code here.
   }

   /** Called when we receive sysex MIDI message on port 0. */
   private void onSysex0(final String data) 
   {
      // MMC Transport Controls:
      if (data.equals("f07f7f0605f7"))
            mTransport.rewind();
      else if (data.equals("f07f7f0604f7"))
            mTransport.fastForward();
      else if (data.equals("f07f7f0601f7"))
            mTransport.stop();
      else if (data.equals("f07f7f0602f7"))
            mTransport.play();
      else if (data.equals("f07f7f0606f7"))
            mTransport.record();
   }

   private Transport mTransport;
}
