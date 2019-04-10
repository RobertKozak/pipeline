FROM microsoft/dotnet:${DockerBuildVersion} as build-env

WORKDIR /app

COPY . ./
RUN dotnet publish -c Release -o out

FROM microsoft/dotnet:${DockerRunVersion}
LABEL maintainer="Robert Kozak <rkozak@gmail.com>"

WORKDIR /app
COPY --from=build-env /app .

ENTRYPOINT ["dotnet", "${Name}.dll"]
