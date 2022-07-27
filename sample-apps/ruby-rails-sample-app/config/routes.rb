Rails.application.routes.draw do
  root 'application#root'

  get '/aws-sdk-call', to: 'application#aws_sdk_call'
  get '/outgoing-http-call', to: 'application#outgoing_http_call'
  get '/outgoing-sampleapp', to: 'application#outgoing_sampleapp'

end
