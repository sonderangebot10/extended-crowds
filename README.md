# Extended CrowdS

Extended CrowdS is extensions of [CrowdS](https://bitbucket.org/jowalle/crowds/src/master/) open-source crowdsensing system that allows distribution of computational tasks.

## Prerequisites to running the server
* **docker** - install docker-ce package from the official Docker repo
* **docker-compose** - install docker-compose, minimum version 3.2
* **source code** - download the source code from current repository

## How to run the server
* Set environment variables to the desired values. Following are all the variables used in the project. Default values for them can be found in the docker-compose.yml file under CrowdS Server\php-apache-mysql
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

* Update path to mapped volumes within the docker-compose.yml. The absolute path to **PHP\www\html** should be updated in the first part (before the colon) in the volumes section for both php and apache service.
Same should be performed for the mysql service, where the mapped volume is to **MySQL\data** and **MySQL\docker-entrypoint-initdb**.

* Go to the docker-compose.yml file and executed the command **docker-compose up**. This should start all the containers.

## How to install the Android application



