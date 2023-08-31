using Microsoft.AspNetCore.Hosting;
using Microsoft.Extensions.Hosting;
using dotnet_sample_app.Controllers;
using System;

namespace dotnet_sample_app
{
    public class Program
    {
        public static Config cfg = Config.ReadInFile("config.yaml");

        public static void Main(string[] args)
        {
            CreateHostBuilder(args).Build().Run();
        }

        public static IHostBuilder CreateHostBuilder(string[] args) =>
            Host.CreateDefaultBuilder(args)
                .ConfigureWebHostDefaults(webBuilder =>
                {
                    webBuilder.UseStartup<Startup>();
                    string listenAddress = "http://"+cfg.Host+":"+cfg.Port;
                    webBuilder.UseUrls(listenAddress);
                });
    }
}
