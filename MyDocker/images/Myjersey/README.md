# demos/Myjersey的镜像

***
	docker build -t cxt/myjeresy:v1.0 ./
	docker run -d -p 8088:8088 --name myjeresy cxt/myjeresy:v1.0
***
## 启动可选启动参数
* -e JAVA_OPTS="-Xms128m -Xmx512m" 
* -v /home/cxt/docker/images/Myjersey/logs:opt/cxt/Myjersey/logs