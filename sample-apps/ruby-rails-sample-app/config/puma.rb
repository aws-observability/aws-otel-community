# Specifices the `bind` address that Puma will listen on to receive requests.
bind "tcp://#{ENV.fetch('LISTEN_ADDRESS').sub('http://', '')}"
