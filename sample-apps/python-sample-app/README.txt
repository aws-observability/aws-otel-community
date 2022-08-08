Python Opentelemetry Auto-instrumentation Sample App

Installation guide:

python3 -m venv .

source bin/activate

pip install --no-cache-dir -r requirements.txt

OTEL_PROPAGATORS=xray OTEL_PYTHON_ID_GENERATOR=xray opentelemetry-instrument python app.py
