/*
 * Copyright The OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package software.amazon.adot.sampleapp;

import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

public class Config {
    private static String configHost;
    private static String configPort;
    private static int configInterval;
    private static int configTime;
    private static int configHeap;
    private static int configThreads;
    private static int configCpu;
    private static ArrayList<String> configSamplePorts;

    private Config(InputStream input) {
        // reading in values from config yaml file
        Yaml yaml = new Yaml();
        Map<String, Object> variables = (Map<String,Object>) yaml.load(input);
        configHost = (String) variables.get("Host");
        configPort = (String) variables.get("port");
        configInterval = (int) variables.get("TimeInterval");
        configTime = (int) variables.get("RandomTimeAliveIncrementer");
        configHeap = (int) variables.get("RandomTotalHeapSizeUpperBound");
        configThreads = (int) variables.get("RandomThreadsActiveUpperBound");
        configCpu = (int) variables.get("RandomCpuUsageUpperBound");
        configSamplePorts = (ArrayList<String>) variables.get("SampleAppPorts");
    }

    public static Config fromEnvVarOrResource() {
        InputStream stream;

        String file = System.getenv("ADOT_JAVA_SAMPLE_APP_CONFIG");

        try {
            if (file != null) {
                stream = new FileInputStream(file);
            } else {
                stream = Config.class.getResourceAsStream("/config.yaml");
            }
        } catch (FileNotFoundException ex) {
            throw new RuntimeException("File not found: ", ex);
        }

        return new Config(stream);
    }

    // getter functions to return config values

    public String getHost() {
        return configHost;
    }

    public String getPort() {
        return configPort;
    }

    public int getInterval() {
        return configInterval;
    }

    public int getTimeAdd() {
        return configTime;
    }

    public int getHeap() {
        return configHeap;
    }

    public int getThreads() {
        return configThreads;
    }

    public int getCpu() {
        return configCpu;
    }

    public ArrayList<String> getSamplePorts() {
        return configSamplePorts;
    }
}
