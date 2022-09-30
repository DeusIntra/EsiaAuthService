FROM openjdk:8
LABEL Description="This image is used to install & check JCP"

# Дистрибутив версии JCP_VERSION должен находиться в одной папке с Dockerfile.
# Запуск производится с помощью команды, например, для JCP версии 2.0.41473:
# docker build --no-cache -t jcp-inst --build-arg JCP_VERSION=2.0.41473 .

# Переданная извне версия JCP.
ARG JCP_VERSION

ENV LANG     C.UTF-8   
ENV LC_ALL   C.UTF-8
ENV LC_CTYPE C.UTF-8

# Копирование дистрибутива JCP из контекста в папку distrib.

COPY jcp-$JCP_VERSION.zip /distrib/
WORKDIR /distrib

# Распаковка дистрибутива JCP.

RUN unzip -q jcp-$JCP_VERSION.zip
WORKDIR jcp-$JCP_VERSION

# Установка JCP и JCryptoP из дистрибутива.

RUN java -cp .:*: ru.CryptoPro.Installer.InstallerConsole -force -install -jcp -jcryptop

# Проверка установки.

RUN echo "#!/bin/sh" > ./test.sh && echo "" >> ./test.sh && echo "java ru.CryptoPro.JCP.tools.License -required" >> ./test.sh && echo "java ru.CryptoPro.JCP.tools.Check -all" >> ./test.sh && chmod +x ./test.sh

CMD ["./test.sh"]