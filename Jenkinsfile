pipeline {
    agent any

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
                git branch: 'master', url: 'https://github.com/Administracja-Team/ProjectManagerBackend.git'
            }
        }
        stage('Build Maven') {
            agent {
                docker {
                    image 'maven:3.8.6-openjdk-11'
                    args '-v $HOME/.m2:/root/.m2'
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
                    sh """
                    docker stop ${APP_NAME} || true
                    docker rm ${APP_NAME} || true
                    """
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
