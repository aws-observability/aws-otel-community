FROM mcr.microsoft.com/dotnet/sdk:7.0 AS build-env
WORKDIR /app
COPY . ./
RUN dotnet publish *.csproj -c Release -o out

FROM mcr.microsoft.com/dotnet/aspnet:7.0
WORKDIR /app
ENV AWS_REGION=us-west-2
ENV LISTEN_ADDRESS=0.0.0.0:8080
ENV ASPNETCORE_URLS=http://+:8080
ENV OTEL_EXPORTER_OTLP_ENDPOINT=http://otel:4317
EXPOSE 8080
COPY --from=build-env /app/out .
ENTRYPOINT ["dotnet", "donet-sample-app.dll"]
  
