### Kooder Search API

**搜索接口**

GET(POST): /search/repositories  #仓库搜索  

|参数名 |参数含义  | 示例|
--- | --- | ---
|q|搜索关键字|q=password|
|lang|指定编程语言(不支持多值)|lang=Java|
|e.id|搜索指定企业的仓库(仅限 gitee)|e.id=1213|
|sort|排序方法(stars,forks,update)|sort=update|
|p|页码(每页20条)|p=3|

GET(POST): /search/codes         #代码搜索  

|参数名 |参数含义  | 示例|
--- | --- | ---
|q|搜索关键字|q=password|
|lang|指定编程语言(不支持多值)|lang=Java|
|e.id|搜索指定企业的仓库(仅限 gitee)|e.id=1213|
|repo.id|搜索指定仓库的代码，支持多值，使用逗号隔开|repo.id=1213,32|
|sort|排序方法(stars,forks,update)|sort=update|
|p|页码(每页20条)|p=3|

GET(POST): /search/issues        #Issue 搜索

|参数名 |参数含义  | 示例|
--- | --- | ---
|q|搜索关键字|q=password|
|e.id|搜索指定企业的仓库(仅限 gitee)|e.id=1213|
|sort|排序方法(create,update)|sort=update|
|p|页码(每页20条)|p=3|


**WebHook 回调接口**

/gitlab/system   # Gitlab 系统回调接口  
/gitlab/project  # Gitlab 仓库回调接口  
/gitee           # Gitee Premium 回调接口
