FROM registry.cn-hangzhou.aliyuncs.com/devops_hu/maven:3.5.2

ARG USER_HOME_DIR="/root"

WORKDIR ${USER_HOME_DIR}

ENV M3_HOME=/usr/local/maven
ENV PATH=$PATH:$M3_HOME/bin

COPY . ${USER_HOME_DIR}

RUN chmod 755 ${USER_HOME_DIR}/bin/*.sh  && mkdir -p /var/log/supervisor && \
    cp /etc/supervisord.conf /etc/supervisord.conf_back && \
    cp -rf ${USER_HOME_DIR}/supervisord.conf /etc/ && \
    cd ${USER_HOME_DIR} && mvn install && \
    rm -rf /tmp/* /var/cache/apk/*

EXPOSE 8080
CMD ["/usr/bin/supervisord"]
