package com.amazon.sampleapp;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.Map;
import java.util.ArrayList;
import org.yaml.snakeyaml.Yaml;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Config {
    private static final Logger logger = LogManager.getLogger();
    private static String configHost;
    private static String configPort;
    private static int configInterval;
    private static int configTime;
    private static int configHeap;
    private static int configThreads;
    private static int configCpu;
    private static ArrayList<String> configSamplePorts;
    public Config() {
        //reading in values from config yaml file
        Yaml yaml = new Yaml();
        InputStream inputStream = getClass().getResourceAsStream("/config.yaml");
        Map<String, Object> variables = yaml.load(inputStream);
        configHost = (String) variables.get("Host");
        configPort = (String) variables.get("port");
        configInterval = (int) variables.get("TimeInterval");
        configTime = (int) variables.get("RandomTimeAliveIncrementer");
        configHeap = (int) variables.get("RandomTotalHeapSizeUpperBound");
        configThreads = (int) variables.get("RandomThreadsActiveUpperBound");
        configCpu = (int) variables.get("RandomCpuUsageUpperBound");
        configSamplePorts = (ArrayList<String>) variables.get("SampleAppPorts");
    }

    //getter functions to return config values

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

public class Config {
    private static Config INSTANCE = new Config();
    public static Config getConfig() {
        return INSTANCE;
    }

    private Config() {
        Yaml yaml = new Yaml();
        InputStream inputStream = getClass().getResourceAsStream("/config.yaml");
        Map<String, Object>  = yaml.load(inputStream);
    }
}
