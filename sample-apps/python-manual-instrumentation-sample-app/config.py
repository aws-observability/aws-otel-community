import yaml

def create_config(cfg):
    config = {}
    if cfg:
        with open(cfg, 'r') as file:
            config = yaml.safe_load(file)
    config.setdefault('Host','0.0.0.0')
    config.setdefault('Port','8080')
    config.setdefault('TimeInterval',1)
    config.setdefault('RandomTimeAliveIncrementer',1)
    config.setdefault('RandomTotalHeapSizeUpperBound',100)
    config.setdefault('RandomThreadsActiveUpperBound',10)
    config.setdefault('RandomCpuUsageUpperBound',100)
    config.setdefault('SampleAppPorts',[])
    return config

