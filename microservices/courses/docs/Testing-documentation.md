# Testing Documentation for Courses
For the courses microservice, manual testing was done with the help of Postman. 
In order to do this test, we ran the courses, gateway and authentication microservices
as the courses microservice isn't dependent on the other microservices, and these were required
for proper testing. 

Furthermore, due to this most of the functionality is tested in the integration tests
for the controller as the only things that are mocked are the HTTP requests sent to the controller. 
This means that the code from the controller to the database has been tested in those automated tests.
Because of that, we kept the manual tests shorter as the main focus is to see if the microservices are connected properly,
and to see if the endpoints are functional.

Due to th

## Postman examples
We tested each endpoint to see if it functions as intended. 
Before each request to the course microservice there is a request sent to the authentication microservice
to get the valid JWT token in the header as shown below. 

![img.png](images/authenticationHeader.png)

With the result being the following:
![img.png](images/authenticationResult.png)

### Creating a new course

**Sending the request:**
![img.png](images/courseCreationRequest.png)

**Recieving the response:**
![img_1.png](images/courseCreationResponse.png)
This clearly gives us a 200 OK response as shown in the image above. 

## Get Courses by course code
**Sending the request**
![img.png](images/getCourseByCode.png)
**Receiving the response**
![img.png](images/getCourseCodeResponse.png)
The request body is as expected, an array of json objects.

##Get Course by Id
**Sending the request**
![img.png](images/getCourseByIdRequest.png)
As you can see above, the {{courseId}} notation is actually a placeholder for the courseid
returned by the create courses request. In this test, the value it has is 1.

**Receiving the response:**
![img.png](images/getCourseByIdResponse.png)

## Get Multiple Courses
**Sending the request:**
![img.png](images/getMultipleCoursesRequest.png)

**Receiving the response**
![img.png](images/getMultipleCoursesResponse.png)

## Get all editions of a course
**Sending the request**
![img.png](images/getMultipleEditionsOfCourse.png)

**Receiving the response**
![img.png](images/getMultipleEditionsOfCourseResponse.png)

## Add grade
**Sending the request**
![img.png](images/addGrade.png)

**Receiving the response**
![img.png](images/addGradeResponse.png)

## Get multiple user grades
For this test, we had to create 3 grades before hand using the add grade method. 
The request bodies are shown below:
```JSON
{
"courseId" : 1,
"grade" : 5.2,
"userId" : 1
}
```
```JSON
{
  "courseId" : 1,
  "grade" : 9.9,
  "userId" : 2
}
```
```JSON
{
"courseId" : 1,
"grade" : 10,
"userId" : 3
}
```

**Sending the request**
![img_1.png](images/getMultipleUserGradesRequest.png)

**Receiving the response**
![img.png](images/getMultipleUserGradesResponse.png)

## Delete course
**Sending the request**
![img.png](images/deletingCourse.png)
**Receiving the response**
![img.png](images/deletingCourseResponse.png)

## Get grade of user for course
**Sending the request**
![img.png](images/getGradeOfUser.png)
**Receiving the reseponse**
![img.png](images/getGradeOfUserResponse.png)

## Assigning Lecturer to course
**Sending the request**
![img.png](images/assigningLecturerToCourseRequest.png)

**Receiving the response**
![img.png](images/assigningLecturerToCourse.png)

##Get courses of lecturer
**Sending the request**
![img.png](images/getCoursesOfLecturerRequest.png)
**Receiving the response**
![img.png](images/getCoursesOfLecturerResponse.png)

## Does lecturer teach course
**Sending the request**
![img.png](images/doesLecturerTeachCourseRequest.png)
**Receiving the response**
![img.png](images/doesLecturerTeachCourseResponse.png)