package com.dannyhurley.devices;

import com.bitwig.extension.controller.api.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

class JSONParameter {
    public String id;
    public String name;

    public JSONParameter(String id, String name) {
        this.id = id;
        this.name = name;
    }
}

public class MappableDevice extends Mappable {
    public String bitwigDeviceId;
    public Device matchedDeviceFromBank;
    public SpecificPluginDevice specificDevice;
    public Map<String, Parameter> mappedParameters;
    private final int openWindowMidiChannel;
    private final int openWindowCC;
    public String pluginName;
    public ControllerHost host;
    public CursorTrack cursorTrack;

    public void writeParametersToJSONFile() throws IOException {

        HashMap<String, Object> deviceEntry = new HashMap<>();
        HashMap<String, Object[]> parametersForDevice = new HashMap<>();

        ArrayList<JSONParameter> parameters = new ArrayList<JSONParameter>();
        for(int i = 0; i <= 10000; i++) {
            Parameter parameter = this.specificDevice.createParameter(i);
            parameter.name().markInterested();
            int bitwigParameterID = i;

            parameter.name().addValueObserver((name) -> {

                if(name.equals("")) {
                    return;
                }
                parameters.add(new JSONParameter(Integer.toString(bitwigParameterID), name));

                parametersForDevice.put("parameters", parameters.toArray());

                deviceEntry.put(this.pluginName, parametersForDevice);

                ObjectMapper om = new ObjectMapper();
                try {
                    om.writeValue(new FileWriter("/Users/danny/workspace/bitwig-device-parameters/" + this.pluginName + ".json"), deviceEntry);
                } catch (IOException e) {
                    e.printStackTrace();
                }

//                try {
////                    if(name != null && !name.equals("")) {
////                        FileWriter csvWriter = new FileWriter("/Users/danny/workspace/bitwig-device-parameters/" + this.pluginName + ".csv", true);
////                        csvWriter.append(name).append(",").append(Integer.toString(bitwigParameterID)).append("\n");
////                        csvWriter.flush();
////                        csvWriter.close();
////                    }
//
//
//                } catch (IOException e) {
//                    host.println(":::: ERROR WRITING PARAMS");
//                }
            });
        }

    }

    public MappableDevice(
            String bitwigDeviceId,
            int openWindowMidiChannel,
            int openWindowCC,
            ControllerHost host,
            CursorTrack cursorTrack,
            Map<String, MappableParameter> mappableParameters,
            String pluginName,
            boolean inSelector,
            int selectorPosition) {
        host.println("mapping " + pluginName);

        this.bitwigDeviceId = bitwigDeviceId;
        this.openWindowMidiChannel = openWindowMidiChannel;
        this.openWindowCC = openWindowCC;
        this.pluginName = pluginName;
        this.host = host;
        this.cursorTrack = cursorTrack;

        this.matchedDeviceFromBank = findDevice();

        this.specificDevice = matchedDeviceFromBank.createSpecificVst3Device(this.bitwigDeviceId);

        this.mappedParameters = new HashMap<>();
        for(String mappableParameterId : mappableParameters.keySet()) {
            MappableParameter mappableParameter = mappableParameters.get(mappableParameterId);
            Parameter parameter = this.specificDevice.createParameter(mappableParameter.bitwigParameterID);
            parameter.markInterested();
            parameter.setIndication(true);

            this.mappedParameters.put(mappableParameter.id, parameter);

        }
    }

    private Device findDevice() {
        DeviceMatcher deviceIdMatcher = this.host.createVST3DeviceMatcher(this.bitwigDeviceId);
        
        DeviceBank bank = this.cursorTrack.createDeviceBank(1);
        bank.setDeviceMatcher(deviceIdMatcher);

        Device device = bank.getDevice(0);

        device.exists().addValueObserver((exists) -> {
            host.println(exists ? "FOUND " + this.pluginName : this.pluginName + " NOT ON TRACK");
        });

        return device;
    }

    private DeviceBank getInstrumentSelectorDeviceBank(CursorTrack cursorTrack, ControllerHost host, int selectorPosition) {
        int selectorIndex = selectorPosition - 1;

        DeviceBank instrumentSelectorDeviceSearchBank = cursorTrack.createDeviceBank(1);
        UUID instrumentSelectorDeviceId = UUID.fromString("9588fbcf-721a-438b-8555-97e4231f7d2c");
        DeviceMatcher instrumentSelectorDeviceMatcher = host.createBitwigDeviceMatcher(instrumentSelectorDeviceId);

        instrumentSelectorDeviceSearchBank.setDeviceMatcher(instrumentSelectorDeviceMatcher);
        Device instrumentSelectorDevice = instrumentSelectorDeviceSearchBank.getDevice(0);

        instrumentSelectorDevice.exists().addValueObserver((exists) -> {
            host.println(exists ? "FOUND INSTRUMENT SELECTOR" : "INSTRUMENT SELECTOR NOT ON TRACK");
        });

        return instrumentSelectorDevice.createLayerBank(1).getItemAt(selectorIndex).createDeviceBank(1);
    }

    private void toggleWindow() {
        this.matchedDeviceFromBank.isWindowOpen().toggle();
    }

    public boolean setParameter(int midiChannel, int cc, int value) {
        boolean shouldToggleWindow = (
            midiChannel == this.openWindowMidiChannel
            && cc == this.openWindowCC
            && value == 127
        );

        if(shouldToggleWindow) {
            toggleWindow();
            return true;
        }

        Parameter parameterToSet = this.mappedParameters.get(id(midiChannel, cc));
        if(parameterToSet != null) {
            parameterToSet.value().set(value, 128);
            return true;
        }

        return false;
    }
}