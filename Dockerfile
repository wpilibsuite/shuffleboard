FROM ubuntu:16.04

RUN \
  apt-get update && \
  apt-get install -y software-properties-common xvfb  libswt-gtk-3-java curl && \
  echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | debconf-set-selections && \
  add-apt-repository -y ppa:webupd8team/java && \
  apt-get update && \
  apt-get install -y oracle-java8-installer && \
  rm -rf /var/lib/apt/lists/* && \
  rm -rf /var/cache/oracle-jdk8-installer

ENV JAVA_HOME /usr/lib/jvm/java-8-oracle

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

COPY . /usr/src/app
