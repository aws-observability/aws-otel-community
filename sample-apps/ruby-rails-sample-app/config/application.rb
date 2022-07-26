require_relative "boot"

require "yaml"
require "rails"
require "action_controller/railtie"
require "action_view/railtie"

# Require the gems listed in Gemfile, including any gems
# you've limited to :test, :development, or :production.
Bundler.require(*Rails.groups)

module RubyRailsSampleApp
  class Application < Rails::Application
    # Initialize configuration defaults for originally generated Rails version.
    config.load_defaults 7.0

    
    configurations_from_yaml_file = YAML.load(File.read("config.yaml"))
    $host = configurations_from_yaml_file["Host"]
    $port = configurations_from_yaml_file["Port"]
    $time_interval = configurations_from_yaml_file["TimeInterval"]
    $random_time_alive_incrementer = configurations_from_yaml_file["RandomTimeAliveIncrementer"]
    $random_total_heap_size_upper_bound = configurations_from_yaml_file["RandomTotalHeapSizeUpperBound"]
    $random_threads_active_upper_bound = configurations_from_yaml_file["RandomThreadsActiveUpperBound"]
    $random_cpu_usage_upper_bound = configurations_from_yaml_file["RandomCpuUsageUpperBound"]
    $sample_app_ports = configurations_from_yaml_file["SampleAppPorts"]

    # Don't generate system test files.
    config.generators.system_tests = nil
  end
end
