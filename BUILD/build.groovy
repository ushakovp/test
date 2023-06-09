node {
    checkout scm
    stage('Build Docker Image') {
        sh "docker build -t java-autotests -f Dockerfile ."
    }
    stage('Run Tests') {
        try {
            sh "docker run --name my-container --network=host java-autotests gradle test --no-daemon -Djavax.net.ssl.trustStore=$JAVA_HOME/lib/security/cacerts -Djavax.net.ssl.trustStorePassword=changeit"
        } catch (ignored) {
            currentBuild.result = 'FAILURE'
        }
    }
    stage('Copy Allure Results'){
        try{
            sh "docker cp my-container:/app/build/allure-results ${WORKSPACE}"
        }catch (ignored) {
            currentBuild.result = 'FAILURE'
        }
    }
    stage('Remove container'){
        sh "docker rm -f my-container"
    }
    stage('Reports') {
        allure([
                includeProperties: false,
                jdk              : '',
                properties       : [],
                reportBuildPolicy: 'ALWAYS',
                results          : [[path: "allure-results"]]
        ])
    }
}
