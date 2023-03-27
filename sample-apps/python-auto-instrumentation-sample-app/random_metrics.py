import random
import threading
import time
import os

from config import *
from opentelemetry.metrics import CallbackOptions, Observation
from opentelemetry import metrics

cfg = create_config('config.yaml')

common_attributes = { 'signal': 'metric', 'language': 'python-auto-instrumentation', 'metricType': 'random' }

meter = metrics.get_meter(__name__)

# Starts the callback for cpu usage
def cpu_usage_callback(options: CallbackOptions):
    min = 0
    max = cfg['RandomCpuUsageUpperBound']
    cpu_usage = Observation(value=random.randint(min, max), attributes=common_attributes)
    print('CPU Usage asked by SDK')
    yield cpu_usage

# Starts the callback for heap size
def heap_size_callback(options: CallbackOptions):
    min = 0
    max = cfg['RandomTotalHeapSizeUpperBound']
    total_heap_size = Observation(value=random.randint(min, max), attributes=common_attributes)
    print("Heapsize asked by SDK")
    yield total_heap_size

# This is the random metric collector class
class RandomMetricCollector():
    
    # Init registers 4 different metrics
    def __init__(self):
        testingId = ""
        if (os.environ.get("INSTANCE_ID")):
            testingId = "_" + os.environ["INSTANCE_ID"]

        self.time_alive=meter.create_counter(
            name="time_alive" + testingId,
            description="Total amount of time that the application has been alive",
            unit='ms'
        )
        self.cpu_usage=meter.create_observable_gauge(
            name="cpu_usage" + testingId,
            callbacks=[cpu_usage_callback],
            description="Cpu usage percent",
            unit='1'
        )
        self.threads_active=meter.create_up_down_counter(
            name="threads_active" + testingId,
            description="The total number of threads active",
            unit='1'
        )
        self.total_heap_size=meter.create_observable_gauge(
            name="total_heap_size" + testingId,
            callbacks=[heap_size_callback],
            description="The current total heap size",
            unit='By'
        )
        self.thread_bool = True
        self.thread_count = 0

    # Adds one to the time alive counter
    def update_time_alive(self, cfg=None):
        self.time_alive.add(cfg['RandomTimeAliveIncrementer'], attributes=common_attributes)
    
    # Updates the currently active threads based on its current bounds.
    def update_threads_active(self, cfg=None):
        if self.thread_bool:
            if self.thread_count < cfg['RandomThreadsActiveUpperBound']:
                self.threads_active.add(1, attributes=common_attributes)
                self.thread_count += 1
            else:
                self.thread_bool = False
                self.thread_count -= 1
        
        else:
            if self.thread_count > 0 :
                self.threads_active.add(-1, attributes=common_attributes)
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
                time.sleep(cfg['TimeInterval'])

        update_thread = threading.Thread(target=update, args=(self, cfg,), daemon=True)
        update_thread.start()

