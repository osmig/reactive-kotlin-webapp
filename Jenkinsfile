pipeline {

    agent {
        docker {
            image 'monostream/rancher-compose'
        }
    }

    stages {

        stage('Deploy Stack') {
            steps {
                sh 'rancher-compose -url http://rancher.agent.an3ll.se:8080/ --access-key C5656FB1AC54E71CF570 --secret-key bCKh5Ww8t9siGSFDc6xT7X615gjNRiMKb86GPDdS -file kotlin-reactive-compose-rancher.yml up -d --force-recreate --confirm-upgrade --pull'
            }
        }
    }
}