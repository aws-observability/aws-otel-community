# Specifices the `bind` address that Puma will listen on to receive requests.
bind "tcp://#{$host}" +":" + "#{$port}"