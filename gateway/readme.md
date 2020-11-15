### Gitee Search Gateway

`Gateway` 是 Gitee Search 对外提供服务的接口(HTTP)，通过该接口可以对索引进行管理，以及执行常规的搜索操作。

#### 接口说明

**索引管理接口列表**

* `/index/add`  添加索引
* `/index/update`   更新索引
* `/index/delete`   删除索引

**搜索接口列表**

* `/search/repositories`   仓库搜索
* `/search/codes` 代码搜索
* `/search/issues`  Issue搜索
* `/search/pullrequests`    PR搜索
* `/search/wiki` 文档搜索
* `/search/commits` commit 搜索
* `/search/users` 用户搜索

**系统管理接口列表**

* `/metrics/keywords`  获取热门关键字
* `/metrics/storage` Gitee Search 的存储信息


#### 源码包说明

`com.gitee.search.action` 处理接口业务逻辑 
`com.gitee.http.server` HTTP服务  
