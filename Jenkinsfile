pipeline {
    agent any

    environment {
        APP_NAME = "project-manager-backend"                  // Имя контейнера и приложения
        DOCKER_IMAGE = "project-manager-backend:latest"         // Имя Docker-образа
        HOST_PORT = "8080"                                      // Порт, по которому доступно приложение на хосте
        CONTAINER_PORT = "8080"                                 // Порт внутри контейнера
        DEPLOY_PATH = "/home/gnevilkoko/backend-docker-image"          // Путь, где находится .env файл
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'master', url: 'https://github.com/Administracja-Team/ProjectManagerBackend.git'
            }
        }
        stage('Build Maven') {
            agent {
                docker {
                    image 'maven:3.8.6-openjdk-11'
                    args '-v $HOME/.m2:/root/.m2' // если нужно кэшировать зависимости
                }
            }
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }
        stage('Build Docker Image') {
            steps {
                script {
                    sh "docker build -t ${DOCKER_IMAGE} ."
                }
            }
        }
        stage('Deploy') {
            steps {
                script {
                    // Останавливаем и удаляем старый контейнер, если он запущен
                    sh """
                    docker stop ${APP_NAME} || true
                    docker rm ${APP_NAME} || true
                    """
                    // Запускаем новый контейнер, передавая переменные окружения из файла .env
                    sh "docker run -d --name ${APP_NAME} -p ${HOST_PORT}:${CONTAINER_PORT} --env-file ${DEPLOY_PATH}/.env ${DOCKER_IMAGE}"
                }
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
