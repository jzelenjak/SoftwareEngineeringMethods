# Courses API Document
This document shows how to make use of the endpoints for the courses microservice. 
It contains a description of the endpoint, what the courses microservice does, and the required input format and output.
## General information
The courses microservice manages everything related to courses such as grades and the lecturers who teach a certain course.
For each course entity, there can be multiple grade entities associated with it along with the lecturers that teach that course. 
Each grade entity, the students Id is stored, the courses Id and also the grade itself.

Each endpoint within this microservice authenticates itself using the JWT tokens placed in the headers of the HTTP request. 
These 

## Creating a new course
```
POST /api/courses/
```
This endpoint is for creating  a new course and stores it in the database. 
The input can be either a CourseRequest object or a JSON object in the format below.

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
  "coruseId" : 2,
  "courseCode" : "CSE2216",  
  "startDate" : "2022-01-08T16:51:32.7123609Z",
  "finishDate" : "2022-01-08T16:51:32.7123609Z",
  "numStudents" : 400
}
```
| Response code | Reason |
|---------------|--------|
|200 OK | Successful Completion|
|400 Bad Request | The failed to create|
|404 Not Found | The endpoint was not found|
|403 Forbidden | The user doesn't have permission for this endpoint|

## Get Course By Code
```
GET "/api/courses/get/courses/{code}"
```
This endpoint returns a list of courses with the same course code as requested by the user.
The course code is provided in the URL as a path variable of the endpoint in the "{code}" section.

Example input:
```
GET /api/courses/get/coruses/CSE2215
```

Example output:
The output is a JSON object containing multiple JSON objects of the same format as the courseResponse as above.
It should look something like this:
```json

```