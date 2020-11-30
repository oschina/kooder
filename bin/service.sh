#!/bin/sh
# description: jsw-test
# processname: jsw-test
# chkconfig: 234 20 80

# 程序名称
SERVICE_NAME=jsw-test
#程序路径获取相对路径,可填写绝对路径，如APP_HOME=/usr/local/bingo_cdp/deploy/bingo-cdp-timerjob-linux
APP_HOME=$(dirname $(pwd))
EXEC=/opt/jsvc/commons-daemon-1.0.15-src/src/native/unix/jsvc
JAVA_HOME=/usr/java/jdk1.8.0_51

#依赖路径
cd ${APP_HOME}
CLASS_PATH="$PWD/classes":"$PWD/lib/*"

#程序入口类
CLASS=main.DaemonMainClassForLinux

#程序ID文件
PID=${APP_HOME}/${SERVICE_NAME}.pid
#日志输出路径
LOG_OUT=${APP_HOME}/logs/${SERVICE_NAME}.out
LOG_ERR=${APP_HOME}/logs/${SERVICE_NAME}.err

#输出
echo "service name: $SERVICE_NAME"
echo "app home: $APP_HOME"
echo "jsvc: $EXEC"
echo "java home: $JAVA_HOME"
echo "class path: $CLASS_PATH"
echo "main class: $CLASS"

#执行
do_exec()
{
    $EXEC -home "$JAVA_HOME" -Dsun.jnu.encoding=UTF-8 -Dfile.encoding=UTF-8 -Djcifs.smb.client.dfs.disabled=false -Djcifs.resolveOrder=DNS -Xms512M -Xmx1024M -cp $CLASS_PATH -outfile $LOG_OUT -errfile $LOG_ERR -pidfile $PID $1 $CLASS
}

#根据参数执行
case "$1" in
    start)
        do_exec
        echo "${SERVICE_NAME} started"
            ;;
    stop)
        do_exec "-stop"
        echo "${SERVICE_NAME} stopped"
            ;;
    restart)
        if [ -f "$PID" ]; then
            do_exec "-stop"
            do_exec
            echo "${SERVICE_NAME} restarted"
        else
            echo "service not running, will do nothing"
            exit 1
        fi
            ;;
    status)
        ps -ef | grep jsvc
        ;;
    *)
        echo "usage: service ${SERVICE_NAME} {start|stop|restart|status}" >&2
        exit 3
        ;;
esac