FROM python:3.8.9

WORKDIR /app

COPY . .

RUN pip install --upgrade pip

RUN pip install --no-cache-dir -r requirements.txt

# Uncomment the following line if OTEL_RESOURCE_ATTRIBUTES is not specified in the environment elsewhere
# ENV OTEL_RESOURCE_ATTRIBUTES='service.name=python-auto-instrumentation-sample-app'

ENTRYPOINT OTEL_PROPAGATORS=xray OTEL_PYTHON_ID_GENERATOR=xray opentelemetry-instrument python app.py
