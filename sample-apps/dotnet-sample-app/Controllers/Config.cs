using System.IO;
using YamlDotNet.Serialization;
using YamlDotNet.Serialization.NamingConventions;

namespace dotnet_sample_app.Controllers
{
    public class Config
    {
        public string Host;
        public string Port;
        public int TimeInterval;
        public int RandomTimeAliveIncrementer;        
        public int RandomTotalHeapSizeUpperBound;
        public int RandomThreadsActiveUpperBound;    
        public int RandomCpuUsageUpperBound;                                   
        public string[] SampleAppPorts;

        public Config() {
            this.Host = "0.0.0.0";
            this.Port = "8080";
            this.TimeInterval = 1;
            this.RandomTimeAliveIncrementer = 1;
            this.RandomTotalHeapSizeUpperBound = 100;
            this.RandomThreadsActiveUpperBound = 10;
            this.RandomCpuUsageUpperBound = 100;
            this.SampleAppPorts = new string[0];
        }

        public static Config ReadInFile(string file) {
            var deserializer = new DeserializerBuilder()
                .WithNamingConvention(PascalCaseNamingConvention.Instance)
                .Build();

            Config returnConfig = null;
            try {
                returnConfig = deserializer.Deserialize<Config>(File.ReadAllText(file));
            }
            catch {
                returnConfig = new Config();
            }
            return returnConfig;

        }
    }
}
