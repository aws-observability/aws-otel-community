## Python Opentelemetry Manual-Instrumentation Sample App

### Getting Started:

#### Local

```
# create venv
python3 -m venv .

source bin/activate

# install requirements
pip install --no-cache-dir -r requirements.txt

# run app with environment variables set.
python app.py
```

#### Docker
Build the image using the dockerfile and run the image in a container.

docker build -t python-manual-instrumentation-sample-app .

docker run -p 8080:8080 --name python-manual-app python-manual-instrumentation-sample-app 
