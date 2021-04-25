# Extended CrowdS

Extended CrowdS is extensions of [CrowdS](https://bitbucket.org/jowalle/crowds/src/master/) open-source crowdsensing system that allows distribution of computational tasks.

There are a few changes provided in this extension of CrowdS:
* Parametrisation of sensitive variable in the CrowdS PHP server.
* Dockerize the PHP server side.
* Introduce Bluetooth integration of the Android application. Through the Bluetooth integration, the app can connect to a
Smartwatch and create tasks once it receives information from the Smartwatch.

## Prerequisites to running the server in a docker container
* **docker** - install docker-ce package from the official Docker repo
* **docker-compose** - install docker-compose, minimum version 3.2
* **source code** - download the source code from current repository

## How to run the server in a docker container
1. Set environment variables to the desired values. Following are all the variables used in the project. Default values for them can be found in the docker-compose.yml file under CrowdS Server\php-apache-mysql
    * PHP_VERSION
    * DB_URL
    * DB_USERNAME
    * DB_PASSWORD
    * DB_NAME
    * FIREBASE_API_KEY
    * PHP_URL
    * APACHE_VERSION
    * MYSQL_VERSION
    * DB_ROOT_PASSWORD

3. Go to the docker-compose.yml file and executed the command **docker-compose up --build**. This should start all the containers.

## How to run the server natively
Please, check the [Server Documentation.pdf](https://github.com/viktoriya-kutsarova/extended-crowds/blob/master/docs/Server%20Documentation.pdf)
located under ***docs/Server Documentation.pdf***.

## How to install the Android application

There are two options on how to install the Android application on a Smartphone:
1. Download and install the pre-build [apk](https://github.com/viktoriya-kutsarova/extended-crowds/blob/master/cs/app/build/outputs/apk/debug/app-debug.apk) 
from the repository. It is located under ***cs/app/build/outputs/apk/debug/app-debug.apk***.
2. Use Android Studio to import the project and build the project.
 


