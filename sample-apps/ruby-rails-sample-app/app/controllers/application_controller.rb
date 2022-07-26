class ApplicationController < ActionController::Base
    def aws_sdk_call
        render json: "sdk"
    end
    
    def outgoing_http_call
        render json: "http"
    end

    def outgoing_sampleapp
        render json: "outgoing sample app"
    end
end