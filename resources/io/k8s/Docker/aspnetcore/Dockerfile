FROM microsoft/aspnetcore-build:${DockerBuildVersion} as build-env

WORKDIR /app

COPY . ./
RUN dotnet restore
RUN dotnet publish -c Release -r linux-x64 -o out

FROM microsoft/aspnetcore:${DockerRunVersion}
LABEL maintainer="Robert Kozak <rkozak@gmail.com>"

WORKDIR /app

COPY --from=build-env . ./

EXPOSE ${Port}
ENV ASPNETCORE_URLS http://*:${Port}

ENTRYPOINT ["dotnet", "${Name}.dll"]
