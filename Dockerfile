# FROM maven:3.9.16-eclipse-temurin-25-alpine AS automatalib-build
#
# WORKDIR /build
#
# RUN apk add git
# RUN git clone --depth 1 --branch develop https://github.com/LearnLib/automatalib.git .
# RUN --mount=type=cache,target=/root/.m2 \
# 	mvn -B clean install -DskipTests && \
# 	cp -r /root/.m2/repository /tmp/m2-repo
#
#
# FROM maven:3.9.16-eclipse-temurin-25-alpine AS learnlib-build
#
# WORKDIR /build
#
# COPY --from=automatalib-build /tmp/m2-repo /root/.m2/repository
# RUN apk add git
# RUN git clone --depth 1 --branch develop https://github.com/LearnLib/learnlib.git .
# RUN --mount=type=cache,target=/root/.m2 \
# 	mvn -B clean install -DskipTests && \
# 	cp -r /root/.m2/repository /tmp/m2-repo
#
#
# FROM maven:3.9.16-eclipse-temurin-25-alpine AS benchmarking-build
#
# WORKDIR /build
#
# COPY --from=learnlib-build /tmp/m2-repo /root/.m2/repository
#
# # download all dependencies first to enable caching
# ADD pom.xml .
# RUN mvn -B dependency:go-offline
#
# ADD src src
# RUN mvn -B -e package -Pjlink
#
#
# FROM alpine:3.23.5
#
# WORKDIR /app
# COPY --from=benchmarking-build /build/target/maven-jlink/default .
# COPY systems systems
# RUN mkdir data
#
# CMD ./bin/app

####################

FROM gcr.io/distroless/java-base-debian13:nonroot

WORKDIR /app

COPY target/maven-jlink/default .
COPY systems systems
ENTRYPOINT ["./bin/java", "-m", "de.learnlib.benchmarking/de.learnlib.benchmarking.Main"]

## build (from the project root) via
# docker build -f benchmarking/Dockerfile -t learnlib-benchmarking .
# docker build -t learnlib-benchmarking .

## save via
# docker image save <IMG:TAG> | gzip > learnlib-benchmarking.tar.gz

## load via
# docker image load < learnlib-benchmarking.tar.gz

## run (initially) via
# docker run -p 8080:8080 --name learnlib-benchmarking [-v /path/to/storage/:/app/data] learnlib-benchmarking:latest

## manage via
# docker stop learnlib-benchmarking
# docker start learnlib-benchmarking
