@echo off
java -Xmx1024m -cp lib\*;gateway/target/classes com.gitee.search.http.Gateway %1 %2 %3 %4 %5 %6