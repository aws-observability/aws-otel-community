FROM alpine:latest AS certs

RUN apk update && apk add ca-certificates

FROM golang:1.19 AS build

WORKDIR /app
COPY go.mod .
COPY go.sum .
ENV GOPROXY=direct
RUN go mod download -x
COPY . .
RUN CGO_ENABLED=0 go build -o=/bin/main .

FROM scratch
COPY --from=build /bin/main /bin/main
COPY --from=certs /etc/ssl/certs/ca-certificates.crt /etc/ssl/certs/
COPY ./config.yaml /
CMD ["/bin/main"]
