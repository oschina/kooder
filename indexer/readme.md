### Gitee Search Indexer

indexer 用来接收来自包括数据库、消息队列、Gateway 请求等渠道的索引构建、更新、删除的命令，
并将更新到索引库中。

indexer 自身维护一个持久化队列，可根据实际索引任务量来配置并发处理线程。
由于索引库不支持多线程同时写入，为了确保最大的吞吐量，对此块的设计需要认真考虑。

#### indexer 多线程模型

![Gitee Search Indexer Flow](../docs/img/gsearch-indexer.png)