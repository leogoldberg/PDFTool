FROM rust:1

WORKDIR /app
COPY . .
WORKDIR /app/hello-world

CMD cargo run --bin hello
