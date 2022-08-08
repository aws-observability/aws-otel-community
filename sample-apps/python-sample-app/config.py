import yaml

def create_config(cfg):
    config = {}
    if cfg:
        with open(cfg, 'r') as file:
            config = yaml.safe_load(file)
    config.setdefault('host','0.0.0.0')
    config.setdefault('port','8080')
    config.setdefault('time_interval',1)
    config.setdefault('random_time_alive_incrementer',1)
    config.setdefault('random_total_heap_size_upper_bound',100)
    config.setdefault('random_threads_active_upper_bound',10)
    config.setdefault('random_cpu_usage_upper_bound',100)
    config.setdefault('sample_app_ports',['4567'])
    return config

create_config('config.yaml')