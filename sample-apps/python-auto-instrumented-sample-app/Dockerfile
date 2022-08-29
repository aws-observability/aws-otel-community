FROM python:3.8.9

WORKDIR /app

COPY . .

RUN pip install --no-cache-dir -r requirements.txt

ENV OTEL_RESOURCE_ATTRIBUTES='service.name=python-autoinstrumented-sampleapp'

ENTRYPOINT OTEL_PROPAGATORS=xray OTEL_PYTHON_ID_GENERATOR=xray opentelemetry-instrument python app.py
