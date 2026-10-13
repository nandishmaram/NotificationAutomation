// Jenkins setup needed (one time):
//   Manage Jenkins -> Tools: Maven installation named "Maven"
//   Manage Jenkins -> Credentials:
//     untangled-password      (Secret text)
//     report-email-password   (Secret text)
//     gmail-credentials-json  (Secret file: credentials.json)
//     gmail-stored-credential (Secret file: tokens/StoredCredential)

pipeline {
    agent any

    tools {
        maven 'Maven'
    }

    // Runs automatically on weekdays around 9 AM
    triggers {
        cron('0 11 * * 1-5')
    }

    environment {
        UNTANGLED_PASSWORD    = credentials('untangled-password')
        REPORT_EMAIL_PASSWORD = credentials('report-email-password')
    }

    stages {
        stage('Get code') {
            steps {
                checkout scm
            }
        }

        stage('Add Gmail login files') {
            steps {
                withCredentials([
                    file(credentialsId: 'gmail-credentials-json', variable: 'GMAIL_CREDENTIALS'),
                    file(credentialsId: 'gmail-stored-credential', variable: 'GMAIL_TOKEN')
                ]) {
                    sh '''
                        cp "$GMAIL_CREDENTIALS" src/test/resources/credentials.json
                        mkdir -p tokens
                        cp "$GMAIL_TOKEN" tokens/StoredCredential
                    '''
                }
            }
        }

        stage('Run tests') {
            steps {
                sh 'mvn clean test -Dbrowser.headless=true'
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'reports/**, screenshots/**, testdata/*.xlsx', allowEmptyArchive: true
            sh 'rm -f src/test/resources/credentials.json tokens/StoredCredential'
        }
    }
}