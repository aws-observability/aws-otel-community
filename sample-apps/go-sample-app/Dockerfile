FROM golang:1.17-alpine AS build
RUN apk update && apk add ca-certificates
WORKDIR /app
COPY go.mod .
COPY go.sum .
RUN GO111MODULE=on go mod download

FROM scratch
COPY --from=build /bin/main /bin/main
COPY --from=build /etc/ssl/certs/ca-certificates.crt /etc/ssl/certs/
COPY ./config.yaml /
CMD ["/bin/main"]
