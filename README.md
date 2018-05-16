## Rancher-CICD-labb

Rancher är ett container-management system som har som uppdrag att managera och orkestrera docker-baserade applikationer.

Denna labb går ut på att lära sig att sätta upp ett Rancherkluster i virtuella miljöer, samt bygga och deploya en simpel spring-boot applikation.

Komponenterna som kommer kommer att användas i denna labben är:
* **Rancher** det överliggande systemet som kommer att managera våra dockermiljöer.
* **Docker** alla applikationer kommer att köras i docker containers. 
* **Spring Boot app** som exponerar några REST-tjänster för att lista olika ölsorter. 
Applikationen är skriven i **Kotlin** och byggs med **Gradle**.
* **Jenkins** byggserver som kommer att köra **pipelines** för att bygga och deploya artefakter.
* **Nexus** som artifaktrepository. Docker images kommer att deployas till ett privat nexus-docker-repo

### Förberedelser

För att kunna genomföra denna labb, behövs:
* Docker
* VirtualBox
* git
* Github-konto

#### Docker CE

Docker Community Edition är ett av de enklaste sättet att komma igång med docker på sin egen utvecklingsmiljö
och går att hämta från deras officiella sida och har versioner för flera operativsystem: 

Docker store: https://store.docker.com/search?type=edition&offering=community

##### Verifiera att Docker är installerat 
Ett lätt sätt att verifiera att installationen är korrekt,
försök att köra dockers egna hello-world image genom att köra kommandot nedan från terminalen.
```bash
docker run hello-world
```

Detta hämtar en hello-world image från docker-hubs registry och kör den i en container i din Dockermiljö.

Outputen borde se ut något som liknar:
```bash
Hello from Docker!
This message shows that your installation appears to be working correctly.

To generate this message, Docker took the following steps:
 1. The Docker client contacted the Docker daemon.
 2. The Docker daemon pulled the "hello-world" image from the Docker Hub.
    (amd64)
 3. The Docker daemon created a new container from that image which runs the
    executable that produces the output you are currently reading.
 4. The Docker daemon streamed that output to the Docker client, which sent it
    to your terminal.

To try something more ambitious, you can run an Ubuntu container with:
 $ docker run -it ubuntu bash

Share images, automate workflows, and more with a free Docker ID:
 https://cloud.docker.com/

For more examples and ideas, visit:
 https://docs.docker.com/engine/userguide/
 ```
 
Får du en output från containern som liknar det ovan är Docker installerat korrekt!
 
#### VirtualBox
Denna labb kommer att köra i virtuella miljöer via docker-machine och VirtualBox.
Virtualbox finns att hämta här:
https://www.virtualbox.org/wiki/Downloads
 
 
### Skapa upp virtuella miljöer

Rancher sätts upp som ett kluster av miljöer.

* **Rancher-server** : Noden som orkesterar alla andra dockermiljöer. *GUI + API*
* **Rancher-host** : En eller flera noder som kör applikationer i dockermiljö. *Worker*

Denna labben kommer att behöva använda sig av 2 virtuella maskiner.

För att skapa upp dessa miljöer används **docker-machine**. Det medföljer Docker Community Edition. 

**docker-machine** är ett verktyg som låter en snabbt skapa upp flertalet separata docker hostar i virtuella miljöer. 
Vill du läsa mer om **docker-machine** kan du göra det här: https://github.com/docker/machine

Nedan beskrivs hur vi skapar upp två virtuella maskiner med hjälp av docker-machine.

**OBS: nedan avsätts 4096 MB för varje vm. Justera minnesmängden för vad som passar den hårdvaran som du kör ifrån.**

Öppna upp en ny terminalsession och kör kommandona nedan för att skapa upp vm och sedan ansluta sessionen till dockerhosten i vm:
##### Rancher-server
```
docker-machine create -d virtualbox --virtualbox-memory 4096 --virtualbox-boot2docker-url=https://github.com/rancher/os/releases/download/v1.2.0/rancheros.iso rancher-server
eval $(docker-machine env rancher-server)
```


Öppna upp en ny terminalsession och kör kommandona nedan för att skapa upp vm och sedan ansluta sessionen till dockerhosten i vm:
##### Rancher-host
```
docker-machine create -d virtualbox --virtualbox-memory 4096 --virtualbox-boot2docker-url=https://github.com/rancher/os/releases/download/v1.2.0/rancheros.iso rancher-host
eval $(docker-machine env rancher-host)
```

Nu kommer 2 virtuella maskiner finnas uppsatta baserat på en lättviktig linuxdist med docker förinstallerat.

För att se vilka virtuella maskiner som är uppskapade kör:
```
docker-machine ls
``` 
och borde få liknande resultat som outputen nedan:
```
NAME            ACTIVE   DRIVER       STATE     URL                         SWARM   DOCKER        ERRORS
rancher-server  -        virtualbox   Running   tcp://192.168.99.106:2376           v17.09.1-ce
rancher-host    -        virtualbox   Running   tcp://192.168.99.107:2376           v17.09.1-ce
```

### Sätt upp och konfigurera rancher

**Rancher server** kommer att köra i en dockercontainer på den virtuella maskinen som heter **rancher-server**.

Välj den terminalsessionen som är ansluten till **rancher-server** och kör:
```
docker run -d --restart=unless-stopped -p 8080:8080 rancher/server
```

Efter att containern har startat upp exponeras rancher-gui via "http://**${rancher-server-ip}**:8080"

Nästa steg är att lägga till en rancher-host. 

Navigera till **INFRASTRUCTURE -> HOSTS -> ADD HOST**

![Add Host](src/main/resources/images/add_host.png?raw=true)

Följ instruktionerna och kopiera kommandot som genereras (utan sudo).

Kör detta kommando från din terminalsession som är ansluten till rancher-host.

Efter en kort stund borde en ny **host** ha anslutit sig till **rancher-server**.

![Add Host](src/main/resources/images/new_host.png?raw=true)

Nu finns ett kluster uppsatt med en **rancher-server** och en **rancher-host**!

### Satt upp och konfigurera Jenkins

Jenkins kommer att användas för att bygga koden och publicera docker image, samt att deploya docker imagen i rancher.

Jenkins kommer att konfigureras via en **docker-compose** fil som ser ut som följande:
```yaml
version: '2'
   services:
     jenkins:
       image: getintodevops/jenkins-withdocker:lts
       network_mode: bridge
       ports:
       - 8081:8080/tcp
       volumes:
       - /var/run/docker.sock:/var/run/docker.sock

```

Navigera till **STACKS -> USER -> ADD STACK**.

Nanmge stacken och klistra in **docker-compose** filen i textboxen. Tryck sedan på **CREATE**

![Add Host](src/main/resources/images/add_jenkins.png?raw=true)


