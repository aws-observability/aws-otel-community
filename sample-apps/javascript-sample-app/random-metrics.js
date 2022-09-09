create_cfg = require('./config');

const { CollectorMetricExporter } = require('@opentelemetry/exporter-collector-grpc');
const { MeterProvider } = require('@opentelemetry/metrics');
const { Resource } = require('@opentelemetry/resources');
const { SemanticResourceAttributes } = require('@opentelemetry/semantic-conventions');

/** The OTLP Metrics Provider with OTLP gRPC Metric Exporter and Metrics collection Interval  */
const meter = new MeterProvider({
    resource: Resource.default().merge(new Resource({
      [SemanticResourceAttributes.SERVICE_NAME]: "js-sampleapp"
    })),
    // Expects Collector at env variable `OTEL_EXPORTER_OTLP_ENDPOINT`, otherwise, http://localhost:4317
    exporter: new CollectorMetricExporter(),
    interval: 1000,
  }).getMeter('js-sampleapp');

const cfg = create_cfg.create_config('./config.yaml');
const TIME_ALIVE_METRIC = 'timeAlive';
const CPU_USAGE_METRIC = 'cpuUsage';
const THREADS_ACTIVE_METRIC = 'threadsActive';
const HEAP_SIZE_METRIC = 'totalHeapSize';

let threadBool = true;
let threadCount = 0;

const timeAliveMetric = meter.createCounter(TIME_ALIVE_METRIC, {
    description: 'Total amount of time that the application has been alive',
    unit: 's'
});

// Value observer is the same as an observable gauge
const cpuUsageMetric = meter.createValueObserver(CPU_USAGE_METRIC, {
    description: 'Cpu usage percent',
    unit: '1'
},  async (observerResult) => {
    const value = await getCpuUsage();
    observerResult.observe(value, { label: '1' });
});

const threadsActiveMetric = meter.createUpDownCounter(THREADS_ACTIVE_METRIC, {
    description: 'The total number of threads active',
    unit:'1'
});

// UpDown Sum Observer is the same as ObservableUpDownCounter
const totalHeapSizeMetric = meter.createUpDownSumObserver(HEAP_SIZE_METRIC, {
    description: 'The current total heap size',
    unit:'By'
}, async (observerResult) => {
    const value = await getTotalHeapSize();
    observerResult.observe(value, { label: 'By' });
});

/** Define Metrics Dimensions */
const labels = { pid: process.pid, env: 'beta' };

function getCpuUsage() {
    console.log("getting cpu usage...")
    return new Promise((resolve) => {
        setTimeout(() => {
            resolve(Math.random() * (cfg.RandomCpuUsageUpperBound - 0) + 0);
        }, 100);
    });
}

function getTotalHeapSize() {
    console.log("getting total heap size...")
    return new Promise((resolve) => {
        setTimeout(() => {
            resolve(Math.random() * (cfg.RandomTotalHeapSizeUpperBound - 0 ) + 0);
        }, 100);
    });
}

function updateTimeAlive() {
    timeAliveMetric.bind(labels).add(cfg.RandomTimeAliveIncrementer);
}

function updateThreadsActive() {
    if (threadBool) {
        if (threadCount < cfg.RandomThreadsActiveUpperBound) {
            threadsActiveMetric.bind(labels).add(1);
            threadCount++;
        }
        else {
            threadBool = false;
            threadCount--;
        }
    }
    else {
        if (threadCount > 0) {
            threadsActiveMetric.bind(labels).add(-1);
            threadCount--;
        }
        else {
            threadBool = true;
            threadCount++;
        }
    }

}
setInterval(() =>{
    updateTimeAlive();
    updateThreadsActive();
}, cfg.TimeInterval * 1000);