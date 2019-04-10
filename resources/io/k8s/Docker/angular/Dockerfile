FROM node:${DockerBuildVersion} as build-env

WORKDIR ${WorkDir}
COPY . ${WorkDir}

RUN apk update && apk add git && npm install \
    && npm run build 

FROM nginx:${DockerRunVersion}
LABEL maintainer="Robert Kozak <rkozak@gmail.com>"

WORKDIR "/var/www/${Name}"

COPY --from=build-env ./nginx.conf /etc/nginx/nginx.conf
COPY --from=build-env ./dist "/var/www/${Name}"
