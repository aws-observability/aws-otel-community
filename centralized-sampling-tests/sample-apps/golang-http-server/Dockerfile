FROM golang:1.20

WORKDIR /app
COPY . .
ENV GOPROXY direct

RUN go mod download
RUN go install .

# Build Golang sample app
RUN go build -o application .

EXPOSE 8080

# Entrypoint
CMD ["./application"]
