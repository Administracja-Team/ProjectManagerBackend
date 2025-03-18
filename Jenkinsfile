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
                sh '''
                  docker run --rm \
                    -v "${WORKSPACE}":/app:ro \
                    -v "$HOME/.m2":/root/.m2 \
                    -w /app \
                    gnevilkoko:openjdk21-maven \
                    mvn clean package -DskipTests
                '''
            }
        }
        stage('Build Docker Image') {
            steps {
                // Собираем Docker-образ по Dockerfile (он должен находиться в корне репозитория)
                sh "docker build -t ${DOCKER_IMAGE} ."
            }
        }
        stage('Deploy') {
            steps {
                // Если контейнер с приложением уже запущен, останавливаем и удаляем его
                sh "docker stop ${APP_NAME} || true"
                sh "docker rm ${APP_NAME} || true"
                // Запускаем новый контейнер с приложением
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
