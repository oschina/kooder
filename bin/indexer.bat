@echo off
java -Xmx1024m -cp lib\*;indexer/target/classes com.gitee.kooder.indexer.ServerDaemon %1 %2 %3 %4 %5 %6