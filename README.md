## Rancher-CICD-labb

**Slides inför labben finns här: https://docs.google.com/presentation/d/1zZRw4TJNE-1OlxU2xOL86M9pORpOw95fWPH8wh7CqcM/edit?usp=sharing**

Rancher är ett container-management system som har som uppdrag att managera och orkestrera docker-baserade kluster för att köra applikationer.

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

![New Host](src/main/resources/images/new_host.png?raw=true)

Nu finns ett kluster uppsatt med en **rancher-server** och en **rancher-host**!

### Sätt upp och konfigurera Jenkins

Jenkins kommer att användas för att bygga koden och publicera docker image, samt att deploya docker imagen i rancher.


#### Lägg till Jenkins
Jenkins kommer att konfigureras via en **docker-compose** fil som ser ut som följande:
```
version: '2'
services:
    jenkins:
       image: getintodevops/jenkins-withdocker:lts
       network_mode: bridge
       ports:
       - 8081:8080/tcp
       volumes:
       - /var/run/docker.sock:/var/run/docker.sock
       - /home/docker/jenkins_home:/var/jenkins_home/
```

Navigera till **STACKS -> USER -> ADD STACK**.

Namnge stacken och klistra in **docker-compose** filen i textboxen. Tryck sedan på **CREATE**

![Add Jenkins Stack](src/main/resources/images/add_jenkins.png?raw=true)

När containern har startat upp kan man komma åt jenkins GUI via: "http://**${rancher-host-ip}**:8081"

#### Starta Jenkins första gången
I loggen för Jenkins-containern kan man hitta initiala lösenordet för att starta Jenkins för första gången.

För att läsa loggen av en container, lista alla körande containers (docker ps) och kör följande i **rancer-host**
```
docker logs -f ${container-id}
```

Installera **Suggested Plugins** och skapa sedan en **admin-användare**.

#### Jenkins tools

Jenkins behöver två huvudsakliga verktyg för att kunna köra byggena för detta projekt.

* **Gradle** (för att compilera och hantera docker-images)
* **rancher-compose** (api för att prata med rancher-server för att kunna deploya applikationen på rätt miljö)

##### Gradle
För att installera **gradle**, navigera till **Manage Jenkins -> Global Tool Configuration** och se till att det ser ut som nedan och spara inställningarna:

![Install Gradle](src/main/resources/images/global_tools_gradle.png?raw=true)

##### Rancher-compose

Rancher-compose är inte ett standardverktyg i Jenkins så vi måste lägga till det via ett plugin som heter **CustomTools**

Navigera till **Manage Jenkins -> Manage Plugins**. Sök efter CustomTools, installera och starta om Jenkins.
Navigera till **Manage Jenkins -> Global Tool Configuration** och se till att det ser ut som nedan och spara inställningarna:
**Viktigt är att länka till binären för rancher-compose:**


https://github.com/rancher/rancher-compose/releases/download/v0.12.5/rancher-compose-linux-amd64-v0.12.5.tar.gz

![Install Rancher-Compose](src/main/resources/images/custom_tools_rancher_compose.png?raw=true)

Nu är Jenkins förberett för att köra jobben.

### Sätt upp och konfigurera Nexus

Vi kommer att använda oss av nexus för att lägga upp docker images i ett privat repo

#### Lägg till Nexus
På samma sätt som vi lade till Jenkins kommer vi nu att lägga till Nexus i rancher med hjälp av en docker-compose fil:
```
version: '2'
services:
  nexus:
    image: sonatype/nexus3
    network_mode: bridge
    volumes:
      - "nexus-data:/nexus-data"
    ports:
      - "8082:8081"
      - "8083:8083"
      - "8084:8084"
```

Gå till Rancher-server-gui och navigera till **STACKS -> USER -> ADD STACK**.

Namnge stacken och klistra in **docker-compose** filen i textboxen. Tryck sedan på **CREATE**

![Add Nexus](src/main/resources/images/add_nexus.png?raw=true)

När containern har startat upp kan man komma åt nexus GUI via: "http://**${rancher-host-ip}**:8082"


#### Konfigurera Nexus

Vi kommer att skapa upp tre stycken docker-repon i nexus:
* **hosted:**  privat repo för de docker Images vi vill lagra i nexus
* **proxy:** ett repo som agerar proxy mot dockerhub för att hämta public images.
* **group:** ett repo som samlar de två ovanstående under en url.

##### Private Repo

Logga in i nexus med (admin/admin123) och navigera till **Settings (Kugghjul) -> Repositories -> Create Repository -> docker-hosted**

Se till att konfigurationen ser ut som följande:

![docker-hosted](src/main/resources/images/docker_hosted_conf.png?raw=true)

##### Proxy Repo

Navigera till **Settings (Kugghjul) -> Repositories -> Create Repository -> docker-proxy**

Se till att konfigurationen ser ut som följande:

![docker-proxy](src/main/resources/images/docker_proxy_config.png?raw=true)

##### Group Repo
Navigera till **Settings (Kugghjul) -> Repositories -> Create Repository -> docker-group**

Se till att konfigurationen ser ut som följande:

![docker-proxy](src/main/resources/images/docker_group_config.png?raw=true)


#### Konfigurera docker-engine för Insecure-Registries
För denna labben kommer kommunikationen att ske via http, så vi måste lägga till något som kallas **insecure-registries** i docker-engine på den hosten som kör jenkins.

Vi behöver använda ssh för accessa den virtuella maskinen. Det kan vi göra med hjälp av docker-machine.

Vi behöver lägga till / modifiera en fil som heter daemon.json som ligger under **/etc/docker** som ska se ut som nedan, 
**fast med ip-adressen för rancher-hosten:**
```
docker-machine ssh rancher-host
sudo vi /etc/docker/daemon.json
```
```
{
  "insecure-registries": [
    "http://${docker-host-ip}:8083",
    "http://${docker-host-ip}:8084"
  ],
  "disable-legacy-registry": true
}
```

för att ändringarna ska gälla krävs omstart av docker. Lättast är att logga ut från vm och sedan starta om maskinen med hjälp av docker-machine:
```
docker-machine restart rancher-host
```

Vänta till vm är uppstartad och alla containrar är igång.

Sedan kan vi logga in på vårt privata nexus-repo från rancher-hosten:
```
docker-machine ssh rancher-host
docker login -u admin -p admin123 http://${docker-host-ip}:8083
docker login -u admin -p admin123 http://${docker-host-ip}:8084
```

Se till att båda returnerar
```
Login Succeeded
```

Nu är Nexus uppsatt och konfigurerat för att publisera egna docker images från vår host!

### Lägga till jenkins Pipelines

Nu är allt redo för att lägga till våra jenkins-pipelines för att kunna bygga och deploya en applikation.

Applikationen finns i detta repo. Vi kommer att behöva ändra en del parametrar i projektet för att matcha individuella konfigurationer.
Dessa konfigurationsfiler i projektet kommer att refereras till i jenkins.

#### Github Repo

För att kunna göra anpassade ändringar i projektet:

Forka detta repo, alternativt ladda ner projektet och skapa upp ett nytt github-repo ifrån detta.

#### Nytt Pipeline jobb för CI

Gå till Jenkins GUI, navigera till **New Item** och se till att det ser ut som följande:
![Add pipeline ci](src/main/resources/images/jenkins_ci_pipeline.png?raw=true)

Länka till url för projektet repo:
![Add pipeline ci](src/main/resources/images/github_repo_conf.png?raw=true)

Under **Pipeline**, länka till git clone urlen, samt till Jenkinfile-build:
![Add pipeline ci](src/main/resources/images/pipeline_script_config.png?raw=true)

##### Modifieringar av parametrar i projektet för första ci-pipeline jobbet

I projektet build.gradle finns en variabel som pekar ut det private docker-repot som gradle ska publisera docker-imagen till.
Byt nedanstående variabel till det uppsatta repots url:
```
def privateRepo = '${rancher-host-ip}:8083'
```

committa ändringarna och pusha till github-repot.

##### Kör igång CI jobbet

Nu kan vi äntligen testa att köra pipeline-jobbet i jenkins.
Med lite tur så lyckas jobbet pusha en docker image till det private docker-repot.

Om jobbet går grönt, gå till nexus GUI och kontrollera att docker imagen har dykt upp under **docker-hosted**

#### Nytt Pipeline jobb för CD

Nu har jenkins byggt och pushat en docker-image. Nästa steg är att deploya docker imagen i vår rancher-host.
Vi kommer att lägga till ett nytt jenkins-pipeline jobb för detta.

Gå till Jenkins GUI, navigera till **New Item** och se till att det ser ut som följande:
![Add pipeline ci](src/main/resources/images/jenkins_cd_pipeline.png?raw=true)

Länka till url för projektet repo:
![Add pipeline ci](src/main/resources/images/github_repo_conf.png?raw=true)

Under **Pipeline**, länka till git clone urlen, samt till Jenkinsfile-deploy:
![Add pipeline ci](src/main/resources/images/pipeline_script_deploy_config.png?raw=true)

##### Modifieringar av parametrar i projektet för första cd-pipeline jobbet

I projektet finns en fil som heter Jenkinsfile-deploy. 
Det är pipeline jobbet som kommer att provisionera ut vår applikation med hjälp av rancher.

För att sätta rätt värde på variablerna i filen behöver vi först se till att vi skapar upp api-credentials i vår rancher-server.
Gå till Rancher GUI och Navigera till **API -> ADVANCED OPTIONS -> ADD ENVIRONMENT KEY** och namnge nyckeln.

det kommer att genereras en Access Key och en Secret Key. kopiera dessa parametrar och ersätt variblerna i Jenkinsfile-deploy.
```
def rancherServerUrl = 'http://${rancher-server-ip}:8080/'
def rancherAccessKey = '${ny-genererad-access-key}'
def rancherSecretKey = '${ny-genererad-secret-key}'
```

Sista ändringen behöver göras i filen **kotlin-reactive-compose-single-service.yml**

Byt följande image:
```
image: ${rancher-host-ip}:8083/demoapp
```

Committa ändringarna och pusha till github.

##### Kör igång CD jobbet

Nu kan vi testa att köra pipeline-jobbet i jenkins.
Lyckas jobbet,  gå till rancher GUI och kontrollera att vår applikation har blivit deplpoyad i som en ny stack i rancher.