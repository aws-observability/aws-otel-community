FROM golang:1.17-alpine AS mod
RUN apk update && apk add ca-certificates
WORKDIR /app
COPY go.mod .
COPY go.sum .
RUN GO111MODULE=on go mod download

FROM golang:1.17-alpine as build
COPY --from=mod $GOCACHE $GOCACHE
COPY --from=mod $GOPATH/pkg/mod $GOPATH/pkg/mod
WORKDIR /app
COPY . .
RUN GO111MODULE=on CGO_ENABLED=0 GOOS=linux go build -o=/bin/main .

FROM scratch
COPY --from=build /bin/main /bin/main
COPY --from=build /etc/ssl/certs/ca-certificates.crt /etc/ssl/certs/
COPY ./config.yaml /
CMD ["/bin/main"]
