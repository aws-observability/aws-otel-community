FROM mcr.microsoft.com/dotnet/sdk:6.0 AS build-env
WORKDIR /app
COPY . ./
RUN dotnet publish *.csproj -c Release -o out

FROM mcr.microsoft.com/dotnet/aspnet:6.0
WORKDIR /app
EXPOSE 8080
COPY --from=build-env /app/out .
ENTRYPOINT ["dotnet", "integration-test-app.dll"]
  
