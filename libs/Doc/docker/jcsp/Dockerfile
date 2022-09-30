FROM ubuntu:latest
LABEL Description="This image is used to install & check Java CSP"

# Дистрибутив версии JAVA_CSP_VERSION должен находиться в одной папке с Dockerfile.
# Запуск производится с помощью команды, например, для Java CSP версии 5.0.40714 для
# пользователя с логином LOGIN и паролем PASSWORD, зарегистрированными на сайте cryptopro.ru:
# docker build --no-cache -t java-csp-inst --build-arg JAVA_CSP_VERSION=5.0.40714 --build-arg LOGIN=<your_login> --build-arg PASSWORD=<your_password> .

RUN locale -a
RUN locale

# Переданная извне версия Java CSP.
ARG JAVA_CSP_VERSION

# Переданный извне логин.
ARG LOGIN

# Переданный извне пароль.
ARG PASSWORD

ENV LANG     C.UTF-8   
ENV LC_ALL   C.UTF-8
ENV LC_CTYPE C.UTF-8

RUN ls -las

# Копирование дистрибутива Java CSP из контекста в папку distrib.

RUN mkdir -p /distrib
COPY /java-csp-$JAVA_CSP_VERSION.zip /distrib/

WORKDIR /distrib
RUN ls -las

# Установка дополнительного ПО: wget и gnupg2.

RUN locale
RUN apt-get update

RUN apt-get --assume-yes install wget
RUN apt-get --assume-yes install gnupg2

# Установка CSP 5.0 KC1 из deb-репозитория: 
# https://www.cryptopro.ru/forum2/default.aspx?g=posts&m=90351#post90351

RUN sh -c 'echo "deb https://$LOGIN:$PASSWORD@cryptopro.ru/repo/deb 5.0-unstable main" >/etc/apt/sources.list.d/cprocsp.list'
RUN wget --quiet https://www.cryptopro.ru/sites/default/files/products/csp/cryptopro_key.pub
RUN apt-key add cryptopro_key.pub
RUN apt-get update

RUN apt-get --assume-yes install lsb-cprocsp-base lsb-cprocsp-kc1-64 lsb-cprocsp-ca-certs cprocsp-curl-64 lsb-cprocsp-rdr-64 lsb-cprocsp-capilite-64 

# Проверка установки.

RUN /opt/cprocsp/sbin/amd64/cpconfig -license -view

# Установка дополнительного ПО: zip и java.

RUN apt-get --assume-yes install zip unzip
RUN apt-get --assume-yes install openjdk-8-jdk

RUN java -version

# Распаковка дистрибутива Java CSP.

RUN unzip -q java-csp-$JAVA_CSP_VERSION.zip
WORKDIR java-csp-$JAVA_CSP_VERSION

# Установка JCP и Java CSP из дистрибутива.
# Java CSP задан провайдером по умолчанию
# (-default_provider 1).

RUN java -cp .:*: ru.CryptoPro.Installer.InstallerConsole -force -install -jcp -jcsp -default_provider 1

# Проверка установки.

RUN echo "#!/bin/sh" > ./test.sh && echo "" >> ./test.sh && echo "java ru.CryptoPro.JCSP.JCSPLicense -required" >> ./test.sh && echo "java ru.CryptoPro.JCP.tools.Check -all" >> ./test.sh && chmod +x ./test.sh

CMD ["./test.sh"]