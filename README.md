# CSE2115 - Project (sem-repo-13a)

![Aggregated coverage](https://gitlab.ewi.tudelft.nl/cse2115/2021-2022/sem-group-13a/sem-repo-13a/badges/main/coverage.svg)

### Running

All microservices require a valid database connection in order to work. Setting-up this database is a one-time job.
There are two viable approaches. The first approach is to create a docker image (recommended) by going into the `docker`
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

```shell
gradle microservices:<service-name>:bootRun
```

It is recommended to run this task with Gradle version >=6.7. Older versions of gradle may not work.

### Testing
```
gradle test
```

To generate a coverage report:
```
gradle jacocoTestCoverageVerification
```


And
```
gradle jacocoTestReport
```
The coverage report is generated in: build/reports/jacoco/test/html, which does not get pushed to the repo. Open index.html in your browser to see the report. 

### Static analysis
```
gradle checkStyleMain
gradle checkStyleTest
gradle pmdMain
gradle pmdTest
```

### Notes
- You should have a local .gitignore file to make sure that any OS-specific and IDE-specific files do not get pushed to the repo (e.g. .idea). These files do not belong in the .gitignore on the repo.
- If you change the name of the repo to something other than template, you should also edit the build.gradle file.
- You can add issue and merge request templates in the .gitlab folder on your repo. 