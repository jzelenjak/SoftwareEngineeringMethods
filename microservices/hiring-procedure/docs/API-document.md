# Hiring Procedure - API Document

## Apply

```
POST /api/hiring-procedure/apply?courseId=<...>
```

Endpoint for sending out submissions. The header of the request should contain the JWT of the user's session. Only users with **student** and **TA** permissions have access to this endpoint.

| Query parameter | Value                                                 |
| --------------- | ----------------------------------------------------- |
| courseId        | The ID of the course that the user wishes to apply to |

| Response code | Reason                                                       |
| ------------- | ------------------------------------------------------------ |
| 200 OK        | Successful completion                                        |
| 403 FORBIDDEN | User is not permitted to access the endpoint,  user has applied already, or submission period expired |
| 404 NOT FOUND | The course ID is invalid (course does not exist)             |

---

## Withdraw

```
POST /api/hiring-procedure/withdraw?courseId=<...>
```

Endpoint for withdrawing submissions. The header of the request should contain the JWT of the user's session. Only users with the **admin**, **student** or **TA** role are permitted to access this endpoint.

| Query parameter | Value                                                        |
| --------------- | ------------------------------------------------------------ |
| courseId        | The ID of the course that the user wishes to withdraw their submission from |

| Response code          | Reason                                                    |
| ---------------------- | --------------------------------------------------------- |
| 200 OK                 | Successful completion                                     |
| 403 FORBIDDEN          | User is not permitted to access the endpoint              |
| 405 METHOD NOT ALLOWED | Submission does not exist, or has been processed already |

---

## Reject

```
POST /api/hiring-procedure/reject?submissionId=<...>
```

Endpoint for rejecting an submission that is associated to the provided ID. The header of the request should contain the JWT of the user's session. Only users with the **admin** or **lecturer** role are permitted to access this endpoint.

| Query parameter | Value                               |
| --------------- | ----------------------------------- |
| submissionId   | The ID of the submission to reject |

| Response code          | Reason                                       |
| ---------------------- | -------------------------------------------- |
| 200 OK                 | Successful completion                        |
| 403 FORBIDDEN          | User is not permitted to access the endpoint |
| 404 NOT FOUND          | Submission does not exist                   |
| 405 METHOD NOT ALLOWED | Submission has been processed already       |

---

## Hire

```
POST /api/hiring-procedure/hire-TA?userId=<...>&courseId=<...>
```

Endpoint for hiring a candidate TA for a specific course. The header of the request should contain the JWT of the user's session. Only users with **admin** or **lecturer** permissions have access.

| Query parameter | Value                                              |
| --------------- | -------------------------------------------------- |
| userId          | The ID of the user associated to the submission   |
| courseId        | The ID of the course associated to the submission |

| Response code          | Reason                                                       |
| ---------------------- | ------------------------------------------------------------ |
| 200 OK                 | Successful completion                                        |
| 403 FORBIDDEN          | User is not permitted to access the endpoint, or no submission was found |
| 404 NOT FOUND          | Course or user does not exist                                |
| 405 METHOD NOT ALLOWED | Submission has been processed already                       |

---

## Retrieval

```
GET  /api/hiring-procedure/get-all-submissions?courseId=<...>
```

Endpoint for retrieving all candidate TAs for a specific course. The header of the request should contain the JWT of the user's session. Only users with **lecturer** or **admin** permissions have access.

| Query parameter | Value                                               |
| --------------- | --------------------------------------------------- |
| courseId        | The ID of the course associated to the submissions |

| Response code | Reason                                                       |
| ------------- | ------------------------------------------------------------ |
| 200 OK        | Successful completion                                        |
| 403 FORBIDDEN | User is not permitted to access the endpoint, or no submission was found |
| 404 NOT FOUND | The course does not exist                                    |

---

```
GET  /api/hiring-procedure/get-submissions?userId=<...>
```

Endpoint for retrieving all submissions of a specific user. The header of the request should contain the JWT of the user's session. Only users with **lecturer** or **admin** permissions have access. The same applies for users that have the **same** ID as the ID that is specified in the request.

| Query parameter | Value                                             |
| --------------- | ------------------------------------------------- |
| userId          | The ID of the user associated to the submissions |

| Response code | Reason                                                       |
| ------------- | ------------------------------------------------------------ |
| 200 OK        | Successful completion                                        |
| 403 FORBIDDEN | User is not permitted to access the endpoint, or no submission was found |

---

## Contracts

```
GET  /api/hiring-procedure/get-contract?courseId=<...>
```

Endpoint for requesting a template contract. The header of the request should contain the JWT of the user's session. Only candidate TAs are allowed to use this endpoint.

| Query parameter | Value                                           |
| --------------- | ----------------------------------------------- |
| courseId        | The ID of the course to fetch the contract from |

| Response code | Reason                                                       |
| ------------- | ------------------------------------------------------------ |
| 200 OK        | Successful completion                                        |
| 403 FORBIDDEN | User is not permitted to access the endpoint, or no submission was found |
| 404 NOT FOUND | The specified course does not exist                          |

---

```
GET  /api/hiring-procedure/get-contract?userId=<...>&courseId=<...>
```

Endpoint for requesting a template contract. The header of the request should contain the JWT of the user's session. Only lecturers and admins are allowed to use this endpoint.

| Query parameter | Value                                                |
| --------------- | ---------------------------------------------------- |
| userId          | The ID of the user for which to fetch the contract   |
| courseId        | The ID of the course for which to fetch the contract |

| Response code | Reason                                                       |
| ------------- | ------------------------------------------------------------ |
| 200 OK        | Successful completion                                        |
| 403 FORBIDDEN | User is not permitted to access the endpoint, or no submission was found |
| 404 NOT FOUND | The specified course does not exist                          |

---

## Set Max Hours

```
POST  /api/hiring-procedure/set-max-hours?submissionId=<...>
```

Endpoint for setting the maximum allowed contractual hours for an submission. The header of the request should contain the JWT of the user's session. Only lecturers and admins are allowed to use this endpoint.

| Query parameter | Value                                                       |
| --------------- | ----------------------------------------------------------- |
| submissionId   | The ID of the submission for which to change the max hours |

**Expected body format**:

```json
{
	"maxHours" : 200
}
```

| Response code   | Reason                                                       |
| --------------- | ------------------------------------------------------------ |
| 200 OK          | Successful completion                                        |
| 400 BAD REQUEST | Body is not according to guideline                           |
| 403 FORBIDDEN   | User is not permitted to access the endpoint                 |
| 404 NOT FOUND   | The specified submission does not exist                     |

---

## Get Max Hours

```
GET  /api/hiring-procedure/get-max-hours?courseId=<...>
```

Endpoint for getting the maximum allowed contractual hours for an submission. The header of the request should contain the JWT of the user's session. Only TAs are allowed to use this endpoint.

| Query parameter | Value                                                       |
| --------------- | ----------------------------------------------------------- |
| courseId        | The ID of the course associated to the submission          |

| Response code | Reason                                                       |
| ------------- | ------------------------------------------------------------ |
| 200 OK        | Successful completion                                        |
| 403 FORBIDDEN | User is not permitted to access the endpoint                 |
| 404 NOT FOUND | The specified submission does not exist                     |

---

```
GET  /api/hiring-procedure/get-max-hours?userId=<...>&courseId=<...>
```

Endpoint for getting the maximum allowed contractual hours for an submission. The header of the request should contain the JWT of the user's session. Only lecturers and admins are allowed to use this endpoint.

| Query parameter | Value                                                       |
| --------------- | ----------------------------------------------------------- |
| userId          | The ID of the user associated to the submission            |
| courseId        | The ID of the course associated to the submission          |

| Response code | Reason                                                       |
| ------------- | ------------------------------------------------------------ |
| 200 OK        | Successful completion                                        |
| 403 FORBIDDEN | User is not permitted to access the endpoint                 |
| 404 NOT FOUND | The specified submission does not exist                     |
---

## Rate

```
POST  /api/hiring-procedure/rate?submissionId=<...>
```

Endpoint for rating a TA. The header of the request should contain the JWT of the user's session. Only lecturers and admins are allowed to use this endpoint.

| Query parameter | Value                                                       |
| --------------- | ----------------------------------------------------------- |
| submissionId   | The ID of the submission that will be rated                |

**Expected body format**:

```json
{
	"rating" : 8.7
}
```

| Response code   | Reason                                                       |
| --------------- | ------------------------------------------------------------ |
| 200 OK          | Successful completion                                        |
| 400 BAD REQUEST | Body is not according to guideline                           |
| 400 BAD REQUEST | Rating is not between 0 and 10                               |
| 403 FORBIDDEN   | User is not permitted to access the endpoint                 |
| 403 FORBIDDEN   | The specified submission is not approved                    |
| 404 NOT FOUND   | The specified submission does not exist                     |

---

## Get Rating

```
GET  /api/hiring-procedure/get-rating?courseId=<...>
```

Endpoint for getting a TA's rating for a course. The header of the request should contain the JWT of the user's session. Only TAs are allowed to use this endpoint.

| Query parameter | Value                                                       |
| --------------- | ----------------------------------------------------------- |
| courseId        | The ID of the course associated to the TA position          |

| Response code | Reason                                                       |
| ------------- | ------------------------------------------------------------ |
| 200 OK        | Successful completion                                        |
| 403 FORBIDDEN | User is not permitted to access the endpoint                 |
| 403 FORBIDDEN | The specified submission is not approved or not rated yet   |
| 404 NOT FOUND | The specified position does not exist                        |

---

```
GET  /api/hiring-procedure/get-rating?userId=<...>&courseId=<...>
```

Endpoint for getting a TA's rating for a course. The header of the request should contain the JWT of the user's session. Only lecturers and admins are allowed to use this endpoint.

| Query parameter | Value                                                       |
| --------------- | ----------------------------------------------------------- |
| userId          | The ID of the user associated to the TA position            |
| courseId        | The ID of the course associated to the TA position          |

| Response code | Reason                                                       |
| ------------- | ------------------------------------------------------------ |
| 200 OK        | Successful completion                                        |
| 403 FORBIDDEN | User is not permitted to access the endpoint                 |
| 403 FORBIDDEN | The specified submission is not approved or not rated yet   |
| 404 NOT FOUND | The specified submission does not exist                     |
---

## Get a student's submissions

```
GET /api/hiring-procedure/get-student?userId=<..>
```

Endpoint for getting all the submissions of a student. The header of the request should contain the JWT of the user's session. Only lecturers and admins are allowed to use this endpoint.

| Query parameter | Value                                                       |
| --------------- | ----------------------------------------------------------- |
| userId          | The ID of the user for which to retrieve the submissions    |

| Response code | Reason                                                       |
| ------------- | ------------------------------------------------------------ |
| 200 OK        | Successful completion                                        |
| 403 FORBIDDEN | User is not permitted to access the endpoint                 |
| 404 NOT FOUND | The specified user does not exist or has no submissions      |

```
GET /api/hiring-procedure/get-student
```

Endpoint for getting all the submissions of the requesting student. The header of the request should contain the JWT of the user's session. Only students and TAs are allowed to use this endpoint.

| Response code | Reason                                                       |
| ------------- | ------------------------------------------------------------ |
| 200 OK        | Successful completion                                        |
| 403 FORBIDDEN | User is not permitted to access the endpoint                 |
| 404 NOT FOUND | The specified user has no submissions                        |