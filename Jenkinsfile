// Runs the notification tests in Jenkins.
//
// Needs (set up once in Jenkins):
//   Tools:        a Maven installation named "Maven"
//   Credentials:  untangled-password       (Secret text) - app login password
//                 report-email-password    (Secret text) - Gmail app password for sending the report
//                 gmail-credentials-json   (Secret file) - credentials.json
//                 gmail-stored-credential  (Secret file) - tokens/StoredCredential from your Mac

pipeline {
    agent any

    tools {
        maven 'Maven'
    }

    triggers {
        // Weekdays, around 9 AM (Jenkins picks the exact minute)
        cron('H 9 * * 1-5')
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
            // Keep the reports, screenshots and Excel results with each build
            archiveArtifacts artifacts: 'reports/**, screenshots/**, testdata/*.xlsx', allowEmptyArchive: true

            // Remove the Gmail login files from the workspace
            sh 'rm -f src/test/resources/credentials.json tokens/StoredCredential'
        }
    }
}mvn clean test -Dbrowser.headless=true