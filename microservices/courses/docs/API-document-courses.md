# Courses API Document
This document shows how to make use of the endpoints for the courses microservice.   
It contains a description of the endpoint, what the courses microservice does, and the required input and output format as well as the response requests.
## General information
The courses microservice manages everything related to courses such as grades and the lecturers who teach a certain course.

For each course entity, there can be multiple grade entities associated with it along with the lecturers that teach that course.   
Each grade entity, the students Id is stored, the courses Id and also the grade itself.

Each endpoint within this microservice authenticates itself using the JWT tokens placed in the headers of the HTTP request.   These JWT Tokens are added by the authentication microservice  upon the initial connection with the applciation.

Furthermore, each endpoint written in the CourseController Class has a corresponding method in the CourseService class with a similar name. The service layer handles all the interactions with the database while the controller layer handles the requests.

---
## Creating a new course
```  
POST /api/courses/create
```  
This endpoint is for creating  a new course and stores it in the database.   
The input can be either a CourseRequest object or a JSON object in the format below.  **Note: you must be an admin to create a course**

Expected Input Format:
```json  
{  
 "courseCode" : "CSE2216", 
 "startDate" : "2022-01-08T16:51:32.7123609Z", 
 "finishDate" : "2022-01-08T16:51:32.7123609Z", 
 "numStudents" : 400
}  
```  

The expected output format:
```json  
{  
 "courseId" : 2, 
 "courseCode" : "CSE2216",
 "startDate" : "2022-01-08T16:51:32.7123609Z", 
 "finishDate" : "2022-01-08T16:51:32.7123609Z", 
 "numStudents" : 400
}  
```  
| Response code | Reason |  
|---------------|--------|  
|200 OK | Successful Completion|  
|400 Bad Request | The failed to create the course|  
|404 Not Found | The endpoint was not found|  
|403 Forbidden | The user doesn't have permission for this endpoint|  
  
---

## Get Course By Code
```  
GET "/api/courses/get/courses/{code}"  
```  
This endpoint returns a list of courses with the same course code as requested by the user.  
The course code is provided in the URL as a path variable of the endpoint in the "{code}" section.  
**Note: you must be an admin, student or lecturer get a course by course code**

Example input:
```  
GET /api/courses/get/coruses/CSE2215  
```  

Example output:  

The output is a JSON object containing multiple JSON objects of the same format as the courseResponse as above.  
It should look something like this:
```json  
[{
    "coruseId" : 2,
    "courseCode" : "CSE2216",
    "startDate" : "2022-01-08T16:51:32.7123609Z",
    "finishDate" : "2022-01-08T16:51:32.7123609Z",
    "numStudents" : 400
},  
{
    "coruseId" : 3,
    "courseCxode" : "CSE2200",
    "startDate" : "2022-01-08T16:51:32.7123609Z",
    "finishDate" : "2022-01-08T16:51:32.7123609Z",
    "numStudents" : 400
}]  
```  

| Response code | Reason |  
|---------------|--------|  
|200 OK | Successful Completion|  
|404 Not Found | Could not find courses for that code|  
|403 Forbidden | The user doesn't have permission for this endpoint|

  ---

## Get Course by Id
```
GET "/api/courses/get/{id}"
```
The endpoint returns the course with the requested ID. Each course when first created is stored in the database is assigned a unique ID. In order to use this endpoint the user has to provide the id as a path variable in the URL (in the {id} section) when making the GET request. The endpoint returns back a CourseResponse object in a JSON output format.
**Note: you must be an admin, student or lecturer to get course by id**

Example input:
```
GET "/api/courses/get/1"
```

Example Output format:
```JSON
{  
 "courseId" : 2, 
"courseCode" : "CSE2216",
"startDate" : "2022-01-08T16:51:32.7123609Z", 
"finishDate" : "2022-01-08T16:51:32.7123609Z", 
"numStudents" : 400
}  
```
| Response code | Reason |  
|---------------|--------|  
|200 OK | Successful Completion|  
|404 Not Found | Could not find course with that id|  
|403 Forbidden | The user doesn't have permission for this endpoint|

  ---

## Get Multiple Courses
```
POST "/api/courses/get-multiple"
```

This endpoint gives back a list that contains the information of multiple courses .
**Note: you must be an admin, student or lecturer to get multiple courses**

Expected Input:
```JSON
{
  "courseIds" : [1, 7, 3, 2]
}
```

The input is a JSON object as given in the format above. It consists of a "courseIds" parameter which contains a list of course ids.

Expected Output:
```JSON
[{"coruseId" : 2,
  "courseCode" : "CSE2216",
  "startDate" : "2022-01-08T16:51:32.7123609Z",
  "finishDate" : "2022-01-08T16:51:32.7123609Z",
  "numStudents" : 400
},  
{"coruseId" : 3,
  "courseCode" : "CSE2200",
  "startDate" : "2022-01-08T16:51:32.7123609Z",
  "finishDate" : "2022-01-08T16:51:32.7123609Z",
  "numStudents" : 400}]  
```
It is in the same format as the endpoint for getting courses by course code.

| Response code | Reason |  
|---------------|--------|  
|200 OK | Successful Completion|  
|404 Not Found | The endpoint was not found|  
|403 Forbidden | The user doesn't have permission for this endpoint|

  ---

## Get All Editions Of a Course
```
GET "/api/courses/get-all-editions"
```

This endpoint returns all the courses with the same course code as the requested course. To use this endpoint, the id of the course must be passed as a query parameter as shown below.

**Note: you must be an admin, student or lecturer to get all editions of a course**

Expected Input:

```
GET /api/courses/get-all-editions?courseId=54344
```

Expected Output:
```JSON
{
  "courseIds" : [123457,345678,7867789]
}
```
The output is a JSON object containing a list of course ids of courses with the same course code.

| Response code | Reason |  
|---------------|--------|  
|200 OK | Successful Completion|  
|400 Bad Request | The failed to get course editions|  
|404 Not Found | The endpoint was not found|  
|403 Forbidden | The user doesn't have permission for this endpoint|

  ---

## Get Multiple User Grades
``` 
GET "/api/courses/statistics/user-grade"
```

This endpoint returns back a map of grades with their corresponding user ids for a specific course based on some input parameters. In order to use this endpoint you must provide a JSON object as input with the parameters listed below. Furthermore the output is also a JSON object

**Note you must be an admin or lecturer to get multiple user grades**

Expected Input
```JSON
{
"courseId" : 1,
"amount": 8,
"minGrade" : 7.5,
"userIds" : [523423,42341,32423]
}

```
The "courseId" is the id of the course for which we want to get the grades for. The second parameter "amount" is the maximum number of grades we want. The third parameter "minGrade" is the minimum grade the student must have to be selected. The final parameters "userIds" is a list of all the student numbers for which we want the grades for.

Expected output:
```JSON
{ 
"523423" : 7.5, 
"32423" : 6.5 
}
```

The expected output is a JSON object (Map) which has the student numbers as the keys and the grade as the values. Note: The output is always ordered in descending order of grade with the highest grade first and the lowest grade last.

| Response code | Reason |  
|---------------|--------|  
|200 OK | Successful Completion|  
|400 Bad Request | The failed to get multiple user grades|  
|404 Not Found | The endpoint was not found|  
|403 Forbidden | The user doesn't have permission for this endpoint|

  ---

## Delete Course
```
DELETE "/api/courses/delete/{id}"
```
Deletes the given course from the database. The input required is the Id of the course specified as a path variable {id} in the URL. Below is an example of this.

**Note: you must be an admin to delete a course**

Expected Input:
```
DELETE "/api/courses/delete/1"
```

Expected Output:
A boolean object saying true or false.

| Response code | Reason |  
|---------------|--------|  
|200 OK | Successful Completion|  
|400 Bad Request | The failed to delete course|  
|404 Not Found | The endpoint was not found|  
|403 Forbidden | The user doesn't have permission for this endpoint|

  ---

## Add Grade
```
POST "/api/courses/create/grade"
```
This endpoint adds a grade for a given user and course and saves it in the database. For the input you can make use of the GradeRequest class when making the HTTP request or make use of equivalent JSON object in the format below.

**Note you must be an admin or lecturer  to add a grade.**

Expected input:
```JSON
{
"courseId" : 1,
"grade" : 7.6,
"userId" : 543289
}
```

Expected output:
A string saying "true" or "false".

| Response code | Reason |  
|---------------|--------|  
|200 OK | Successful Completion|  
|400 Bad Request | No request was provided OR could not add grade to database|  
|404 Not Found | The endpoint was not found|  
|403 Forbidden | The user doesn't have permission for this endpoint|

  ---

## Get Grade Of a User
```
GET "/api/courses/get/grade/{userid}/{courseid}"
```

This endpoint returns the grade for the user for a specific course. This endpoint makes use of 2 path variables one for the userid which is {userid} and the other for the courses id which is {courseid}.
**Note you must be an admin, student or lecturer  to get the grade of a user**

Expected input:
```
GET "/api/courses/get/grade/5310824/2"
```

Expected output:
It is a floating point number in the following format:
```
"7.0"
```
| Response code | Reason |  
|---------------|--------|  
|200 OK | Successful Completion|  
|404 Not Found | Could not find the grade for user|  
|403 Forbidden | The user doesn't have permission for this endpoint|

  ---

## Get Courses of a lecturer
```
GET "/api/courses/get/lecturer/courses/{lecturerid}"
```

This endpoint returns all the all the course ids for the courses that a specific lecturer teaches. The input required is the id of the lecturer which is given as a path variable {lecturerid}.

**Note you must be an admin, student or lecturer  to get the courses that a lecturer is associated with.**

Example input:
```
GET "/api/courses/get/lecturer/courses/5639163"
```

Example output:
```JSON
"[1,5,9,2]"
```

| Response code | Reason |  
|---------------|--------|  
|200 OK | Successful Completion|  
|404 Not Found | Could not find any courses for lecturer|  
|403 Forbidden | The user doesn't have permission for this endpoint|

  ---

## Assign a lecturer to a course
```
POST "/api/courses/assign/lecturer/{lecturerid}/{courseid}"
```

This endpoint assigns a lecturer to a specific course and stores relationship in the database. There are 2 path variables, one is the id of the lecturer whom we want to connect to a course given by {lecturerid} and the second path variable is the id of the course we want the lecturer to connect to given by {courseid}.

**Note: you must be an admin to assign a lecturer to a course**

Expected Input:
```
POST "/api/courses/assign/lecturer/5839662/4"
```

Expected output:
A string saying "true" if the operation has completed succesfully.

| Response code | Reason |  
|---------------|--------|  
|200 OK | Successful Completion|  
|400 Bad Request | The failed to assign lecturer to course|  
|404 Not Found | The endpoint was not found|  
|403 Forbidden | The user doesn't have permission for this endpoint|

  ---

## Does a lecturer teach a course?
```
GET "/get/teaches/{lecturerId}/{courseId}"
```

This endpoint checks if a lecturer teaches a specific course. There are 2 path variables for this endpoint. The first is the id of the lecturer which is given by  {lecturerId} and the second is the id of the course given by {courseid}.

**Note: you must be admin, lecturer or student in order to check if a lecturer teaches a course.**

Expected input:
```
GET "/get/teaches/5839662/4"
```

Expected output:
A string saying "true" if the operation completed succesfully.

| Response code | Reason |  
|---------------|--------|  
|200 OK | Successful Completion|  
|404 Not Found | Lecturer does not teach course|  
|403 Forbidden | The user doesn't have permission for this endpoint|