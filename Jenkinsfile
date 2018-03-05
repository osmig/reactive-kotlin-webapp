pipeline {

    agent {
        docker {
            image 'adopteunops/rancher-compose-tools'
        }
    }

    stages {

        stage('Deploy Stack') {
            steps {
                sh 'rancher-compose -url http://18.216.122.197:8080/ --access-key C5656FB1AC54E71CF570 --secret-key bCKh5Ww8t9siGSFDc6xT7X615gjNRiMKb86GPDdS -file kotlin-reactive-compose-rancher.yml up -d --force-recreate --confirm-upgrade --pull'
            }
        }
    }
}