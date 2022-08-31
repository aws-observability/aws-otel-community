## Python Opentelemetry Auto-instrumentation Sample App

### Getting Started:

#### Local

```
# create venv
python3 -m venv .

source bin/activate

# install requirements
pip install --no-cache-dir -r requirements.txt

# run app with environment variables set.
OTEL_RESOURCE_ATTRIBUTES='service.name=python-auto-instrumentation-sampleapp' OTEL_PROPAGATORS=xray OTEL_PYTHON_ID_GENERATOR=xray opentelemetry-instrument python app.py
```

#### Docker
Build the image using the dockerfile and run the image in a container. 

