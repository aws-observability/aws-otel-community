FROM mcr.microsoft.com/dotnet/sdk:6.0 AS build-env
WORKDIR /app
COPY . ./
RUN dotnet publish donet-sample-app/*.csproj -c Release -o out

FROM mcr.microsoft.com/dotnet/aspnet:6.0
WORKDIR /app
EXPOSE 8080
COPY --from=build-env /app/out .
ENTRYPOINT ["dotnet", "donet-sample-app.dll"]

[
    {
        "commitID":"cfsgdtwuei",
        "go_layer": {
                "arm64": {
                        "ap-northeast-1" = "arn:aws:lambda:ap-northeast-1:901920570463:layer:aws-otel-collector-amd64-ver-0-61-0:1",
                        "ap-northeast-2" = "arn:aws:lambda:ap-northeast-2:901920570463:layer:aws-otel-collector-amd64-ver-0-61-0:1",
                        "ap-south-1"     = "arn:aws:lambda:ap-south-1:901920570463:layer:aws-otel-collector-amd64-ver-0-61-0:1",
                        "ap-southeast-1" = "arn:aws:lambda:ap-southeast-1:901920570463:layer:aws-otel-collector-amd64-ver-0-61-0:1",
                        "ap-southeast-2" = "arn:aws:lambda:ap-southeast-2:901920570463:layer:aws-otel-collector-amd64-ver-0-61-0:1",
                        "ca-central-1"   = "arn:aws:lambda:ca-central-1:901920570463:layer:aws-otel-collector-amd64-ver-0-61-0:1",
                        "eu-central-1"   = "arn:aws:lambda:eu-central-1:901920570463:layer:aws-otel-collector-amd64-ver-0-61-0:1",
                        "eu-north-1"     = "arn:aws:lambda:eu-north-1:901920570463:layer:aws-otel-collector-amd64-ver-0-61-0:1",
                        "eu-west-1"      = "arn:aws:lambda:eu-west-1:901920570463:layer:aws-otel-collector-amd64-ver-0-61-0:1",
                        "eu-west-2"      = "arn:aws:lambda:eu-west-2:901920570463:layer:aws-otel-collector-amd64-ver-0-61-0:1",
                        "eu-west-3"      = "arn:aws:lambda:eu-west-3:901920570463:layer:aws-otel-collector-amd64-ver-0-61-0:1",
                        "sa-east-1"      = "arn:aws:lambda:sa-east-1:901920570463:layer:aws-otel-collector-amd64-ver-0-61-0:1",
                        "us-east-1"      = "arn:aws:lambda:us-east-1:901920570463:layer:aws-otel-collector-amd64-ver-0-61-0:1",
                        "us-east-2"      = "arn:aws:lambda:us-east-2:901920570463:layer:aws-otel-collector-amd64-ver-0-61-0:1",
                        "us-west-1"      = "arn:aws:lambda:us-west-1:901920570463:layer:aws-otel-collector-amd64-ver-0-61-0:1",
                        "us-west-2"      = "arn:aws:lambda:us-west-2:901920570463:layer:aws-otel-collector-amd64-ver-0-61-0:1",
                },
                "amd64": {
                        "ap-northeast-1" = "arn:aws:lambda:ap-northeast-1:901920570463:layer:aws-otel-collector-arm64-ver-0-62-1:1",
                        "ap-northeast-2" = "arn:aws:lambda:ap-northeast-2:901920570463:layer:aws-otel-collector-arm64-ver-0-62-1:1",
                        "ap-south-1"     = "arn:aws:lambda:ap-south-1:901920570463:layer:aws-otel-collector-arm64-ver-0-62-1:1",
                        "ap-southeast-1" = "arn:aws:lambda:ap-southeast-1:901920570463:layer:aws-otel-collector-arm64-ver-0-62-1:1",
                        "ap-southeast-2" = "arn:aws:lambda:ap-southeast-2:901920570463:layer:aws-otel-collector-arm64-ver-0-62-1:1",
                        "ca-central-1"   = "arn:aws:lambda:ca-central-1:901920570463:layer:aws-otel-collector-arm64-ver-0-62-1:1",
                        "eu-central-1"   = "arn:aws:lambda:eu-central-1:901920570463:layer:aws-otel-collector-arm64-ver-0-62-1:1",
                        "eu-north-1"     = "arn:aws:lambda:eu-north-1:901920570463:layer:aws-otel-collector-arm64-ver-0-62-1:1",
                        "eu-west-1"      = "arn:aws:lambda:eu-west-1:901920570463:layer:aws-otel-collector-arm64-ver-0-62-1:1",
                        "eu-west-2"      = "arn:aws:lambda:eu-west-2:901920570463:layer:aws-otel-collector-arm64-ver-0-62-1:1",
                        "eu-west-3"      = "arn:aws:lambda:eu-west-3:901920570463:layer:aws-otel-collector-arm64-ver-0-62-1:1",
                        "sa-east-1"      = "arn:aws:lambda:sa-east-1:901920570463:layer:aws-otel-collector-arm64-ver-0-62-1:1",
                        "us-east-1"      = "arn:aws:lambda:us-east-1:901920570463:layer:aws-otel-collector-arm64-ver-0-62-1:1",
                        "us-east-2"      = "arn:aws:lambda:us-east-2:901920570463:layer:aws-otel-collector-arm64-ver-0-62-1:1",
                        "us-west-1"      = "arn:aws:lambda:us-west-1:901920570463:layer:aws-otel-collector-arm64-ver-0-62-1:1",
                        "us-west-2"      = "arn:aws:lambda:us-west-2:901920570463:layer:aws-otel-collector-arm64-ver-0-62-1:1",
                      },
              }
        "python_layer":  {
                        ....
                        ....
        }
        }
]
