![](./gateway/src/main/webapp/img/kooder_logo.png)

### 背景

一个企业里往往有大量的项目，每个项目都包含很多的代码，这些代码都是企业的核心资产。
经过日积月累，不同的开发人员不断的修改完善，企业中很难有人能掌握所有的代码。
于是企业全库的代码搜索就变得非常重要。

例如我们可以搜索公司代码是否包含某类敏感信息，是否使用了某些不安全的方法等等。

### Kooder 是什么

Kooder 是一个开源的代码搜索工具，目标是为包括 Gitee/GitLab/Gitea 在内的代码托管系统提供
自动的源码、仓库和 Issue 的搜索服务。

**搜索界面效果**

![Kooder ScreenShot](docs/img/screenshot.png)

### Kooder 架构

Kooder 服务包含两个模块，分别是 gateway 和 indexer（默认配置下 indexer 被集成到 gateway 中）。
其中 gateway 用来接受来自 HTTP 的索引任务， 对任务进行检查后存放到队列中；
同时 gateway 还接受搜索的请求，并返回搜索结果给客户端。而 indexer 进程负责监控队列中的索引任务，
并将这些要新增、删除和修改索引的任务更新到索引库中。

### 模块说明

* `core`    核心对象和公共类
* `gateway` 用来接收来自 HTTP 的索引和搜索的请求
* `indexer` 构建、更新和删除索引的服务

### 数据流图

![Kooder Flow](docs/img/gsearch-flow.png)

### 源码安装

1.依赖

* openjdk >= 8
* maven > 3

2.下载代码

```
$ git clone https://gitee.com/koode/kooder.git
$ cd kooder
```

### 运行前准备工作

配置文件: `core/src/main/resources/kooder.properties`

1. 配置 HTTP 服务

`http.url` Kooder 的网址，该地址用于向 Git 服务注入 Webhook 的链接地址，
必须是 Git 服务可访问的地址，例如：`http.url = http://<kooder-host>:8080`

`http.port`  Kooder 运行的 HTTP 端口

2. 配置 GitLab 服务地址

目前 Kooder 支持 Gitee、GitLab 和 Gitea ，其他服务正在开发中。

`gitlab.url`  访问 GitLab 的首页  
`gitlab.personal_access_token`  Gitlab 管理员账号 root 的 Personal Access Token

更多配置项请看 [configuration.md](docs/configuration.md)

3. 构建并运行

```
$ cd Kooder
$ mvn install
### 给执行脚本添加权限
$ chmod +x bin/*.sh
### 启动 gateway
$ bin/gateway.sh
### 浏览器访问 http://localhost:8080
```

### Kooder Search API

@see [API.md](docs/API.md)

### Docker 安装

#### docker-compose
依赖
* docker-ce环境
* docker-compose

开发代码优化后，部署只需将代码 clone 下来，然后在服务器上部署容器平台，在平台上执行如下命令：
```
### 开箱即用
docker-compose up -d 

### 关闭容器
docker-compose down
```



#### docker-compose ha 版
依赖
* docker-ce环境
* docker-compose

开发代码优化后，部署只需将代码clone下来，然后在服务器上部署容器平台，在平台上执行如下命令：
```
### 开箱即用
docker-compose -f docker-compose-ha.yaml up -d

### 关闭容器
docker-compose -f docker-compose-ha.yaml down
```


实现的效果如下：

![Kooder docker-ha](docs/img/docker-ha-kooder.png)


配置文件: `core/src/main/resources/kooder.properties`，修改配置文件之后，执行如下命令:

```
docker-compose down
docker-compose up -d 
```

查看服务启动状态
``` 
docker logs -f CONTAINER_ID
......
2021-04-07 13:28:49 INFO [gateway] - Tasks [indexer,gitee] started.
2021-04-07 13:28:49 INFO [gateway] - READY (*:8080)!
.......
```
以上信息显示的日志表明启动成功

**注意**

每次启动都会执行`mvn install`以确保配置文件生效

### 对接不同平台

#### 对接 GitLab

需配置如下几项：

```
http.startup.tasks = indexer,gitlab
gitlab.url = http://gitlab-host:gitlab-port/  
gitlab.personal_access_token = <root user personal access token>  
git.username = root  
git.password =  
```

如果不填写密码，则 Kooder 会自动使用 access token 作为密码。

#### 对接 Gitee 私有化

需配置如下几项：

```
http.startup.tasks = indexer,gitee
gitee.url = https://<gitee-host>/  
gitee.personal_access_token = <root user personal access token>  
git.username = root  
git.password =  
```

#### 对接 Gitea

1.进入 Gitea 管理后台；

![](./docs/img/gitea_webhook.png)

2.添加 Gitea Web 钩子；

![](./docs/img/gitea_webhook_select.png)

3.设置 Web 钩子。

Url 填写 `http://kooder-ip:kooder-port/gitea`，请求方式为`POST + application/json`，触发条件可选`所有事件`或者`自定义事件`。

如选择自定义事件，则需要勾选仓库事件的`推送`、`仓库`选项，和工单事件的`工单`选项。

![](./docs/img/gitea_webhook_setting.png)


2.配置如下几项

```
http.startup.tasks = indexer,gitea
gitea.secret_token = <webhook secret token>
gitea.url = http://gitea-ip:prot/
gitea.personal_access_token = <admin personal access token>
git.username = <admin username>
git.password = <admin password>
```

### 从文件索引仓库

Kooder 目前支持同一托管平台的仓库索引，如需索引多个托管平台，进行分次导入即可。

在 `kooder.properties` 配置文件中进行如下设置：
```java
//开启 Kooder 从文件索引仓库的特性

https.startup.tasks = indexer,file //增加 file 字段

file.index.path = C:/Documents/Kooder/file.txt //配置 file.index.path 文件路径（file.txt 为本地文件，路径可自行配置）

file.index.vender = gitee //指定对应的代码托管平台（必须）

```
file.txt 内容
```
//添加配置中指定的托管平台仓库地址
https://gitee.com/koode/kooder.git
https://gitee.com/ld/J2Cache.git
...
```


### 构建并运行

```
$ cd Kooder
$ mvn install
### 启动 gateway
$ bin/gateway.sh
### 浏览器访问 http://localhost:8080
```


### 搜索界面效果
![kooder-index](docs/img/kooder-index.png)

![Kooder ScreenShot](docs/img/screenshot.png)

