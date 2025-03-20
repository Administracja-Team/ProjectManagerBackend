pipeline {
    agent any

    environment {
        APP_NAME = "project-manager-backend"
        DOCKER_IMAGE = "project-manager-backend:latest"
        HOST_PORT = "8888"
        CONTAINER_PORT = "8080"
        ENV_FILE_PATH = "/backend/.env"
        USER_DATA_FOLDER_PATH = "/backend/users"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Build Maven (JDK 21)') {
            steps {
                sh '''
                  export JAVA_HOME=/opt/openjdk21
                  export PATH=$JAVA_HOME/bin:$PATH
                  echo "Using Java version:"
                  java -version
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
                sh "docker run -d --name ${APP_NAME} -p ${HOST_PORT}:${CONTAINER_PORT} -v /users:/users --env-file ${ENV_FILE_PATH} ${DOCKER_IMAGE}"
            }
        }
    }

    post {
        success {
            echo 'Successfully builded and deployed'
        }
        failure {
            echo 'Failed to build and deploy'
        }
    }
}
