# midjourney-proxy

代理 MidJourney 的discord频道，实现api形式调用AI绘图

此项目Fork自 [midjourney-proxy](https://github.com/novicezk/midjourney-proxy "midjourney-proxy") 项目，对其进行了增强，可见文档最后的更新日志。

## 使用前提
1. 科学上网
2. docker环境
3. 注册 MidJourney，创建自己的频道，参考 https://docs.midjourney.com/docs/quick-start
4. 添加自己的机器人: [流程说明](docs/机器人生成/discord-bot.md)

## 启动

1. 构建镜像
```shell
./build-image.sh
```

2. 启动容器，并设置参数

```shell
# 或者直接在启动命令中设置参数
docker run -d --name midjourney-proxy \
 -p 8686:8080 \
 --restart=always \
 novicezk/midjourney-proxy:1.2
```

3. 检查discord频道中新创建的机器人是否在线
4. 调用api接口的根路径为 `http://ip:port/mj`，具体API接口见下文

## 注意事项
1. 启动失败请检查全局代理或HTTP代理，排查 [JDA](https://github.com/DV8FromTheWorld/JDA) 连接问题
2. 若回调通知接口失败，请检查网络设置，容器中的宿主机IP通常为172.17.0.1


## 配置项

| 变量名 | 非空 | 描述 |
| :-----| :----: | :---- |
| mj.discord.guild-id | 是 | discord服务器ID |
| mj.discord.channel-id | 是 | discord频道ID |
| mj.discord.user-token | 是 | discord用户Token |
| mj.discord.bot-token | 是 | 自定义机器人Token |
| mj.discord.mj-bot-name | 否 | mj机器人名称，默认 "Midjourney Bot" |
| mj.notify-hook | 否 | 任务状态变更回调地址 |                            |
| mj.task-store.timeout | 否 | 任务过期时间，过期后删除，默认30天 |
| mj.translate-way | 否 | 中文prompt翻译方式，可选null(默认)、baidu、gpt |
| mj.baidu-translate.appid | 否 | 百度翻译的appid |
| mj.baidu-translate.app-secret | 否 | 百度翻译的app-secret |
| mj.openai.gpt-api-key | 否 | gpt的api-key |
| mj.openai.timeout | 否 | openai调用的超时时间，默认30秒 |
| mj.openai.model | 否 | openai的模型，默认gpt-3.5-turbo |
| mj.openai.max-tokens | 否 | 返回结果的最大分词数，默认2048 |
| mj.openai.temperature | 否 | 相似度(0-2.0)，默认0 |

## API接口说明
前往接口文档：[开发文档](https://console-docs.apipost.cn/preview/1b7bf7ee2dda00fe/8051d5904667d25a "开发文档")

## 更新版本

### 2023-05-20
- 增加用户等登录注册，部分接口需登录使用
- 增加用户VIP的功能，部分接口只有在VIP时间内才能使用
- 增加多用户生成图片排队功能
- 修复跨域问题

### 2023-05-15
- 增加生成的图片上传到腾讯云的COS存储
- 增加图片按天分区存储

### 2023-05-13
- 将原本最低需要JDK17版本降低到JDK1.8
- 增加对MySQL的支持，用于存储生成的图像，删除Redis
- 增加接口API请求日志打印
