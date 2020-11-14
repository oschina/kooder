### Gitee Search
Gitee Search 是 Gitee 的搜索引擎服务模块，为 Gitee 提供仓库、Issue、代码等搜索服务。

#### 模块说明

* `core`    核心对象和公共类
* `gateway` 用来接收来自 HTTP 的索引和搜索的请求
* `indexer` 构建、更新和删除索引的服务

#### 使用方法

1. 构建

```
$ git clone https://gitee.com/oschina/gitee-search.git
$ mvn compile
```