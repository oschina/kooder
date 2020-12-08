### Gitee Search Gateway

`Gateway` 是 Gitee Search 对外提供服务的接口(HTTP)，通过该接口可以对索引进行管理，以及执行常规的搜索操作。

#### 接口说明

**索引管理接口列表**

* `/task/add`  添加索引
* `/task/update`   更新索引
* `/task/delete`   删除索引

需要参数: type=repo|issue|code  
请求体格式请看 json/*.json

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

#### HTTP 状态码说明

* `200`  执行成功，响应内容直接存放在 HTTP BODY
* `400`  HTTP 请求参数错误，例如 type 参数值错误
* `403`  action 方法受保护，不允许调用
* `404`  请求的地址不存在对应的 action 方法
* `406`  HTTP 请求内容格式错误，一般是 json 格式有误
* `500`  action 执行异常

#### 源码包说明

`com.gitee.search.action` 处理接口业务逻辑 
`com.gitee.http.server` HTTP服务  
