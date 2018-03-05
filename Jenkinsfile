node {
    def rancherComposeHome = tool 'rancher-compose'

    stage ('Checkout') {
        checkout scm
    }

    stage ('Deploy') {
        sh "/var/jenkins_home/tools/com.cloudbees.jenkins.plugins.customtools.CustomTool/rancher-compose/rancher-compose-v0.12.5/rancher-compose -url http://rancher.agent.an3ll.se:8080/ --access-key C5656FB1AC54E71CF570 --secret-key bCKh5Ww8t9siGSFDc6xT7X615gjNRiMKb86GPDdS -file /Users/joakimanell/r2m/interna-projekt/reactive-webapp/kotlin-reactive-compose-rancher.yml up -d --force-recreate --confirm-upgrade --pull"
   }
}
