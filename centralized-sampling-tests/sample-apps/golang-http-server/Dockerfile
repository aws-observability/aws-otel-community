# Stage 0 - Build Golang sample app
FROM golang:1.22 as builder

WORKDIR /app
COPY . .
ENV GOPROXY direct

RUN go mod download
RUN go install .
RUN CGO_ENABLED=0 go build -o golang-sample-app .

# Stage 1 - Run Golang Sample App
FROM scratch
WORKDIR /
COPY --from=builder /app/golang-sample-app .

EXPOSE 8080

# Entrypoint
CMD ["/golang-sample-app"]
