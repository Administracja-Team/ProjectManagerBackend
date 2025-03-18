pipeline {
    agent any

    environment {
        APP_NAME = "project-manager-backend"
        DOCKER_IMAGE = "project-manager-backend:latest"
        HOST_PORT = "8888"
        CONTAINER_PORT = "8080"
    }

    stages {
        stage('Checkout') {
            steps {
                // Извлекаем код из репозитория
                checkout scm
            }
        }
        stage('Build Maven') {
            steps {
                // Запускаем Maven внутри контейнера с Java 21.
                // Используется переменная WORKSPACE, которая содержит путь к текущей сборке.
                sh '''
                  docker run --rm \
                    -v "${WORKSPACE}":/app \
                    -v "$HOME/.m2":/root/.m2 \
                    -w /app \
                    gnevilkoko:openjdk21-maven \
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
                sh "docker stop ${APP_NAME} || true"
                sh "docker rm ${APP_NAME} || true"
                sh "docker run -d --name ${APP_NAME} -p ${HOST_PORT}:${CONTAINER_PORT} ${DOCKER_IMAGE}"
            }
        }
    }

    post {
        success {
            echo 'Сборка и деплой прошли успешно!'
        }
        failure {
            echo 'Сборка или деплой завершились с ошибкой.'
        }
    }
}
