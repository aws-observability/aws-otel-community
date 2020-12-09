FROM golang:1.15 AS mod
WORKDIR $GOPATH/main
COPY go.mod .
COPY go.sum .
RUN GO111MODULE=on go mod download

FROM golang:1.15 as build
COPY --from=mod $GOCACHE $GOCACHE
COPY --from=mod $GOPATH/pkg/mod $GOPATH/pkg/mod
WORKDIR $GOPATH/main
COPY . .
RUN GO111MODULE=on CGO_ENABLED=0 GOOS=linux go build -o=/bin/main .

FROM scratch
COPY --from=build /bin/main /bin/main
CMD ["/bin/main"]
