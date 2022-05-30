package com.dannyhurley.parametermapper;

import com.bitwig.extension.controller.api.ControllerHost;
import com.bitwig.extension.controller.api.CursorTrack;
import com.dannyhurley.devices.MappableDevice;
import com.dannyhurley.devices.MappableParameter;
import com.dannyhurley.json.JSONParser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParameterMapper {

    public static List<MappableDevice> readAndMapParams(ControllerHost host, CursorTrack cursorTrack) {

        try {
            String jsonString = "{\"hi\": \"hello\"}";

            MappableDevice device = new MappableDevice(
                    "4172747541564953436F446950726F63",
                    0,
                    2,
                    host,
                    cursorTrack,
                    new HashMap<String, MappableParameter>(),
                    "Comp DIODE-609",
                    false,
                    0
            );

            device.writeParametersToJSONFile();

            Map<String, String> map = JSONParser.parse(jsonString);

            ArrayList<MappableDevice> mappedParameters = new ArrayList<MappableDevice>() {{
                add(device);
            }};
            host.println(String.valueOf(map));

            return mappedParameters;
        } catch (IOException e) {
            host.println("::: error mapping parameters");
            return new ArrayList<>();
        }
    }
}
