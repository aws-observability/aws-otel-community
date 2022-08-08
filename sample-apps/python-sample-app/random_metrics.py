import random
import threading
import time

from config import *
from opentelemetry.metrics import CallbackOptions, Observation
from opentelemetry import metrics

cfg = create_config('config.yaml')

meter = metrics.get_meter(__name__)

# Starts the callback for cpu usage
def cpu_usage_callback(options: CallbackOptions):
    min = 0
    max = cfg['random_cpu_usage_upper_bound']
    cpu_usage = Observation(value=random.randint(min, max))
    print('CPU Usage asked by SDK')
    yield cpu_usage

# Starts the callback for heap size
def heap_size_callback(options: CallbackOptions):
    min = 0
    max = cfg['random_total_heap_size_upper_bound']
    total_heap_size = Observation(value=random.randint(min, max))
    print("Heapsize asked by SDK")
    yield total_heap_size

# This is the random metric collector class
class random_metric_collector():
    
    # Init registers 4 different metrics
    def __init__(self):
        self.time_alive=meter.create_counter(
            name="time_alive",
            description="Increments by 1s every second the app is alive",
            unit='s'
        )
        self.cpu_usage=meter.create_observable_gauge(
            name="cpu_usage",
            callbacks=[cpu_usage_callback],
            description="The percentage of CPU usage in use",
            unit='1'
        )
        self.threads_active=meter.create_up_down_counter(
            name="threads_active",
            description="the total number of threads active",
            unit='1'
        )
        self.total_heap_size=meter.create_observable_gauge(
            name="total_heap_size",
            callbacks=[heap_size_callback],
            description="the current total heap size",
            unit='1'
        )
        self.thread_bool = True
        self.thread_count = 0

    # Adds one to the time alive counter
    def update_time_alive(self, cfg=None):
        self.time_alive.add(cfg['random_time_alive_incrementer'])
    
    # Updates the currently active threads based on its current bounds.
    def update_threads_active(self, cfg=None):
        if self.thread_bool:
            if self.thread_count < cfg['random_threads_active_upper_bound']:
                self.threads_active.add(1)
                self.thread_count += 1
            else:
                self.thread_bool = False
                self.thread_count -= 1
        
        else:
            if self.thread_count > 0 :
                self.threads_active.add(-1)
                self.thread_count -= 1
            else:
                self.thread_bool = True
                self.thread_count += 1

    # This function registers the metrics client 
    def register_metrics_client(self,cfg=None):
        
        def update(self, cfg):
            while True:
                self.update_time_alive(cfg)
                self.update_threads_active(cfg)
                print("updating time alive & active threads...")
                time.sleep(cfg['time_interval'])

        cpu_usage_callback_thread = threading.Thread(target=cpu_usage_callback, args=(cfg,) ,daemon=True)
        heap_size_callback_thread = threading.Thread(target=heap_size_callback, args=(cfg,), daemon=True)
        update_thread = threading.Thread(target=update, args=(self, cfg,), daemon=True)
        cpu_usage_callback_thread.start()
        heap_size_callback_thread.start()
        update_thread.start()

# rmc = random_metric_collector()
# rmc.register_metrics_client(cfg)