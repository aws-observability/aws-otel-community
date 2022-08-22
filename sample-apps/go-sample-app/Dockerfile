FROM golang:1.17-alpine AS mod
RUN apk update && apk add ca-certificates
WORKDIR /app
COPY go.mod .
COPY go.sum .
RUN GO111MODULE=on go mod download CGO_ENABLED=0 GOOS=linux go build -o=/bin/main .

FROM scratch
COPY --from=mod /bin/main /bin/main
COPY --from=mod /etc/ssl/certs/ca-certificates.crt /etc/ssl/certs/
COPY ./config.yaml /
CMD ["/bin/main"]
