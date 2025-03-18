pipeline {
    agent any
    options {
        // Отключаем автоматический checkout
        skipDefaultCheckout(true)
    }

    environment {
        APP_NAME = "project-manager-backend"
        DOCKER_IMAGE = "project-manager-backend:latest"
        HOST_PORT = "8888"
        CONTAINER_PORT = "8080"
        DEPLOY_PATH = "/home/gnevilkoko/backend-docker-image"
    }

    stages {
        stage('Checkout') {
            steps {
                // Выполняем checkout вручную, указывая URL и при необходимости credentials
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: '*/master']],
                    userRemoteConfigs: [[
                        url: 'https://github.com/Administracja-Team/ProjectManagerBackend.git',
                        credentialsId: 'docker-credentials-gnevilkoko'
                    ]]
                ])
                stash name: 'source', includes: '**'
            }
        }
        stage('Build Maven') {
            steps {
                unstash 'source'
                // Выведем список файлов для проверки, где находится pom.xml
                sh 'ls -la'
                // Если pom.xml находится в подпапке, например, backend, то укажите ее в рабочей директории:
                sh '''
                  docker run --rm \
                    -v "$PWD":/app \
                    -v "$HOME/.m2":/root/.m2 \
                    -w /app \
                    maven:3.8.6-openjdk-22 \
                    mvn clean package -DskipTests
                '''
            }
        }

        stage('Build Docker Image') {
            steps {
                sh "docker build -t ${DOCKER_IMAGE} ."
            }
        }
        stage('Deploy') {
            steps {
                sh """
                  docker stop ${APP_NAME} || true
                  docker rm ${APP_NAME} || true
                """
                sh "docker run -d --name ${APP_NAME} -p ${HOST_PORT}:${CONTAINER_PORT} --env-file ${DEPLOY_PATH}/.env ${DOCKER_IMAGE}"
            }
        }
    }

    post {
        success {
            echo 'Build and deployment succeeded!'
        }
        failure {
            echo 'Build or deployment failed.'
        }
    }
}
