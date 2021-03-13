## Kooder 配置

Kooder 配置文件位于源码中的 `core/src/main/resource/kooder.properties` 文件中。
每次修改完该文件需要重新 `maven install` 重新打包，并重启服务。

### [ kooder.properties ]

Web Service configurations

`http.bind = 127.0.0.1`  HTTP 绑定的 IP 地址，默认是 127.0.0.1  
`http.port = 8080`  HTTP 服务端口  
`http.log.pattern = /,/index/*,/search/*,/api/*`    记录访问日志的请求前缀  
`http.webroot = gateway/src/main/webapp`    Web 静态文件和模板文件的存放目录  
`http.startup.tasks = indexer`  将 `indexer` 依附到 `gateway` 进程中运行

[Gitlab configurations]

`gitlab.url = `  Gitlab 服务地址
`gitlab.personal_access_token = xxx`  Gitlab 管理员账号的 access token  
`gitlab.secret_token = gsearch`  Webhook 回调的密钥  
`gitlab.connect_timeout = 2000`  连接超时设置，单位毫秒  
`gitlab.read_timeout = 10000`  数据读取超时设置，单位毫秒

Git access configrations  

`git.username = xxx`    访问 git 仓库的用户名  
`git.password = xxx`    访问 Git 仓库的密码  
`# git.ssh.key = ./data/ssh_key`    使用 SSH 方式访问 Git 仓库的密钥  
`# git.ssh.keypass = xxx`   SSH 密钥对应的密码  

持久化任务队列配置，支持两种队列 redis 和 embed 。
其中 embed 队列是内建队列，使用磁盘存储队列中信息。 。

`queue.provider = embed`    using embed queue  
`queue.types = repo,issue,code`   
`queue.redis.host = 127.0.0.1`  Redis service host  
`queue.redis.port = 6379`       Redis service port  
`queue.redis.database = 1`      Redis service database  
`queue.redis.key = gsearch-queue`   Redis queue key  

`queue.embed.path = ./data/queue`   embed queue storage path  
`queue.embed.batch_size = 10000`    batch queue size for embed 

Lucene storage configurations  

`storage.type = disk`  
`storage.disk.path = ./data/lucene`  
`storage.disk.use_compound_file = false`  
`storage.disk.max_buffered_docs = -1`  
`storage.disk.ram_buffer_size_mb = 16`  

git repository storage configurations

`storage.repositories.path = ./data/repositories`  
`storage.repositories.max_size_in_gigabyte = 200`  
  
Task thread configurations

`indexer.no_task_interval = 1000`  
`indexer.batch_fetch_count = 10`  
`indexer.tasks_per_thread = 2`  