# CSE2115 - Project (sem-repo-13a)

![Aggregated coverage](https://gitlab.ewi.tudelft.nl/cse2115/2021-2022/sem-group-13a/sem-repo-13a/badges/main/coverage.svg)

---

**Table of contents**

1. [About](#about)
2. [Running](#running)
   1. [Setup](#setup)
   2. [Starting a microservice](#starting-a-microservice)
   3. [Admin account](#admin-account)
3. [Testing](#testing)
4. [Static analysis](#static-analysis)
5. [Contributors](#contributors)

## About

This mono-repository consists of a variety of microservices that can be used to facilitate the hiring process for 
teaching assistants, including the declaration of their hours.

All distinct microservices can be found in the `microservices` directory accompanied by a `README.md` file  that explains 
its functionality/purpose.

## Running

### Setup

All microservices require a valid database connection in order to work. Setting-up this database is a **one-time job**.
There are two viable approaches. The first approach is to create a [docker](https://www.docker.com/) image (recommended) by going into the `docker`
directory and executing the following command:

```shell
# Linux/OSX
./create-docker-db.sh

# Windows
.\create-docker-db.bat
```

The second alternative is to create a local postgres setup on port 5432 that is similar to the `docker/init.sql` script.
This means that it is required to configure the `sem-13a` user and the corresponding databases.

After having finished the setup, you can run a microservice using the following command:

### Starting a microservice

All microservices can be started independently by using the following command.

```shell
gradle microservices:<service-name>:bootRun
```

It is recommended to run this task with Gradle version >=6.7. Older versions of gradle may not work.

### Admin account

By default, every newly registered member is initially a student. This is mainly due to security reasons, because a 
student has the least amount of permissions. Since it is necessary to promote certain users (i.e., change their roles), 
there must be a way to perform such an action, namely an administrator. Thus, we run into a chicken-and-egg problem. 
In order to solve this problem, a root user with default credentials is added to the repository by default. 
This account is permitted to change the roles of others.

The default username and password for the root user are located in the `application.properties` file in the [authentication](microservices/authentication/src/main/resources/application.properties) microservice. These default values are as follows.

```ini
username=root@tudelft.nl
password=!wAg+enDr<e*@ne:rRvX%
```

It is possible to change these values by editing the configuration file.

## Testing

All microservices contain a document that explains how the microservice was tested, and what testing conventions were used. 
The tests themselves can be run using the command below.

```shell
# Run test suite per microservice
gradle microservices:<service-name>:test

# Run all tests for all microservices at once
gradle test
```

In order to generate a coverage report, the following command can be used.

```shell
gradle microservices:<service-name>:jacocoTestCoverageVerification microservices:<service-name>:jacocoTestReport
```

It is also possible to generate an aggregated version of the coverage reports of all microservices using the following command.

```shell
gradle codeCoverageReport
```

The aggregated code coverage report is generated in: `build/reports/jacoco/codeCoverageReport/html`. For standalone 
microservices, the coverage report is generated in the local `build/reports/jacoco/test/html` folder of the microservice itself.

## Static analysis

During the development of the project, the static analysis tools [Checkstyle](https://checkstyle.sourceforge.io/) and 
[PMD](https://pmd.github.io/) were used as means to improve the overall quality and structure of the code. 
These static analysis tool can be run by using the commands below. The tasks with the postfix `Main` perform static 
analysis on the regular codebase, whereas the tasks with the postfix `Test` focus on the code in the test folders.

```shell
gradle checkStyleMain
gradle checkStyleTest
gradle pmdMain
gradle pmdTest
```

## Contributors

The following users have contributed to this project.

| 📸 | Name | Email |
|---|---|---|
| <img src="https://secure.gravatar.com/avatar/6831344d0915a958d3922e4bf36159fe?s=400&d=identicon" width="50px"> | Andy Li | b.x.li@student.tudelft.nl |
| <img src="https://gitlab.ewi.tudelft.nl/uploads/-/system/user/avatar/3100/avatar.png?width=400" width="50px"> | Jegor Zelenjak | A.V.Pacurar-1@student.tudelft.nl |
| <img src="https://gitlab.ewi.tudelft.nl/uploads/-/system/user/avatar/3586/avatar.png?width=400" width="50px"> | Jeroen Bastenhof | j.bastenhof@student.tudelft.nl |
| <img src="https://gitlab.ewi.tudelft.nl/uploads/-/system/user/avatar/3545/avatar.png?width=400" width="50px"> | Mehmet Sözüdüz | M.A.Sozuduz@student.tudelft.nl |
| <img src="https://secure.gravatar.com/avatar/01c95f4b875a5b2334d5d9b0feaa515c?s=400&d=identicon" width="50px"> | Mihnea Toader | m.a.toader-1@student.tudelft.nl |
| <img src="https://secure.gravatar.com/avatar/e6ea65f4cc0d5ac1082ce38a1a391d0a?s=400&d=identicon" width="50px"> | Taneshwar Parankusam | t.p.parankusam@student.tudelft.nl |
