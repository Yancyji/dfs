# Getting Started

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.4.1/maven-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/2.4.1/maven-plugin/reference/html/#build-image)
* [Spring Web](https://docs.spring.io/spring-boot/docs/2.4.1/reference/htmlsingle/#boot-features-developing-web-applications)

### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/bookmarks/)

**Spring Boot+FastDFS+Docker+Nginx 文件上传下载 Demo**

使用docker安装FastDFS服务,这里使用的是docker.io/season/fastdfs版本,latest 不包含nginx

***安装tracker服务:tracker.sh文件***
```shell script
# 运行tracker跟踪服务，默认端口为22122，映射`/fastdfs/tracker /data`以保留数据
docker stop tracker
docker rm tracker
docker run --privileged=true -it -d --name tracker \ -v ~/usr/local/docker/tracker_data:/fastdfs/tracker/data \ --net=host season/fastdfs:1.2 tracker
```

***安装storage服务:storage.sh文件***
```shell script
# 运行storage存储服务,绑定tracker跟踪服务的ip与端口
docker stop storage
docker rm storage
docker run -it -d --name storage \ -v ~/usr/local/docker/storage_data:/fastdfs/storage/data \ -v ~/usr/local/docker/store_path:/fastdfs/store_path \ --net=host -e TRACKER_SERVER:你的trackerIP:22122 season/fastdfs:1.2 storage
```

***手动修改storage绑定tracker的IP，这里直接用网友的配置了***
```shell script
# 1.从容器复制配置文件到宿主机 
docker cp storage:/fdfs_conf/storage.conf ~/
# 2.修改配置文件
vim ~/storage.conf  
#找到tracker_server=你的tracker机ip:22122 
# 3.将文件拷贝回docker容器
docker cp ~/storage.conf storage:/fdfs_conf/
# 4.重启存储服务
docker restart storage
# 5.进入storage容器 
docker exec -it storage bash
# 6.查看storage的状态
cd fdfs_conf/  && fdfs_monitor storage.conf
```

***关闭防火墙***
```shell script
#查看端口状态
netstat -tunlp|grep 22122

#查看防火墙状态
firewall-cmd --state

#查看所有打开的端口
netstat -anp

#防火墙开启
systemctl start firewalld

#防火墙关闭
systemctl stop firewalld

#查询指定端口状态
firewall-cmd --query-port=666/tcp

#开放端口
firewall-cmd --zone=public --add-port=80/tcp --permanent

#重新载入
firewall-cmd --reload

#查看
firewall-cmd --zone= public --query-port=80/tcp

#删除
firewall-cmd --zone= public --remove-port=80/tcp --permanent

#关闭防火墙后需要重启docker
systemctl start docker
```

***配置nginx***
```shell script
#nginx同样docker部署,映射配置文件地址和文件下载地址
docker stop fast_nginx
docker rm fast_nginx
docker run --privileged=true -d -p 80:80 --name fast_nginx -v /nginx/nginx.conf:/etc/nginx/nginx.conf -v /etc/localtime:/etc/localtime:ro  -v /nginx:/nginx -v /root/usr/local/docker/store_path/data://root/usr/local/docker/store_path/data nginx
```

```shell script
#适用root，避免权限不够
user root;
events {
    worker_connections  1024;
}

http{
     server{
        listen 80;
        server_name localhost;
     
        location /{
            root html;
            index index.html index.htm;
         }
    
        
        error_page  500 502 503 504 /50x.html;
        location = 50x.html{
            root html;
        }
    
        #访问地址
        location /group1/M00{
            #本地storage
            alias /root/usr/local/docker/store_path/data;
        }
    
     }
} 
```

***新建spring boot项目***
新建一个spring boot项目，导入依赖

```xml
  <dependencies>
     <dependency>
         <groupId>org.springframework.boot</groupId>
         <artifactId>spring-boot-starter-web</artifactId>
     </dependency>

     <dependency>
         <groupId>org.springframework.boot</groupId>
         <artifactId>spring-boot-starter-test</artifactId>
         <scope>test</scope>
     </dependency>

     <!-- FastDFS依赖 -->
      <dependency>
         <groupId>com.github.tobato</groupId>
             <artifactId>fastdfs-client</artifactId>
        <version>1.26.5</version>
      </dependency>
    <!-- Swagger2 核心依赖 -->
    <dependency>
         <groupId>io.springfox</groupId>
             <artifactId>springfox-swagger2</artifactId>
          <version>2.6.1</version>
     </dependency>
    <dependency>
          <groupId>io.springfox</groupId>
             <artifactId>springfox-swagger-ui</artifactId>
         <version>2.6.1</version>
    </dependency>
 </dependencies>
```
本地调试，使用swagger，所以需要编写swagger配置文件

***swagger配置***
```java
//添加配置注解
@Configuration
public class SwaggerConfig {

    @Bean
    public Docket createRestApi(){
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                 // contriller 扫描路径
                .apis(RequestHandlerSelectors.basePackage("com.dfs.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
         return new ApiInfoBuilder()
                 .title("SpringBoot利用Swagger构建API文档")
                 .description("创建人：yancy")
                 .termsOfServiceUrl("")
                 .version("version 1.0")
                 .build();
    }
}
```

***FastDFS配置***
```java
@Configuration
//导入配置
@Import( FdfsClientConfig.class)
//Jmx重复注册bean的问题
@EnableMBeanExport(registration = RegistrationPolicy.IGNORE_EXISTING)
public class DfsConfig {


}
```
```yaml
server:
#访问端口
  port: 3000
spring:
  servlet:
    multipart:
      max-file-size: 100MB # 最大支持文件大小
      max-request-size: 100MB # 最大支持请求大小
   # 分布式文件系统FDFS配置
fdfs:
     # 链接超时
  connect-timeout: 600
     # 读取时间
  so-timeout: 600
     # 生成缩略图参数
  thumb-image:
    width: 150
    height: 150
#tracker跟踪服务的ip与端口
  tracker-list: 192.168.217.128:22122
lee:
#文件下载地址
  path: group1/M00/00/00/
#下载保存地址
  save: D:\img\
```

之后编写上传下载的工具类，具体看com.dfs.util.FileDfsUtil
编写controller,com.dfs.controller.FileController

```java
//添加swagger注解
@SpringBootApplication
@EnableSwagger2
public class DfsApplication {

    public static void main(String[] args) {
        SpringApplication.run(DfsApplication.class, args);
    }

}
```

启动服务，访问http://localhost:3000/swagger-ui.html，就可以愉快的上传下载文件了。

后续增加文件信息写入数据库，数据库信息回显，完善nginx配置等。

各种组件都只用到了一点简单知识，学习任重而道远！








