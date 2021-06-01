#!/bin/sh
java -cp lib/*:gateway/target/classes -Dkooder.properties=/root/kooder.properties com.gitee.kooder.server.Gateway %1 %2 %3 %4 %5 %6
