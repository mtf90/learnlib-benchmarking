FROM maven:3.9.16-eclipse-temurin-25-alpine AS builder

WORKDIR /build

# download all dependencies first to enable caching
ADD pom.xml .
RUN mvn -B dependency:go-offline

ADD src src
RUN mvn -B package -Pjlink

####################

FROM alpine:3.23.5

WORKDIR /app
COPY --from=builder /build/target/maven-jlink/default .
CMD ./bin/app

## build via
# docker build -t learnlib-benchmarking .

## save via
# docker image save <IMG:TAG> | gzip > learnlib-benchmarking.tar.gz

## load via
# docker image load < learnlib-benchmarking.tar.gz

## run (initially) via
# docker run -p 8080:8080 --name learnlib-benchmarking [-v /path/to/storage/:/app/data] learnlib-benchmarking

## manage via
# docker stop learnlib-benchmarking
# docker start learnlib-benchmarking
