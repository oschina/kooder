FROM registry.cn-hangzhou.aliyuncs.com/devops_hu/maven:3.5.2

ARG USER_HOME_DIR="/root"

WORKDIR ${USER_HOME_DIR}

COPY . .

RUN echo "http://mirrors.ustc.edu.cn/alpine/v3.5/main" >> /etc/apk/repositories && \
    apk add -U tzdata && ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone && \
    chmod 755 ${USER_HOME_DIR}/bin/*.sh && \
    cd ${USER_HOME_DIR} && mvn install && \
    rm -rf /tmp/* /var/cache/apk/*

EXPOSE 8080
ENTRYPOINT ["/bin/sh", "docker-entrypoint.sh"]
