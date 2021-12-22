# Hiring Procedure - API Document

## Apply

```
POST /api/hiring-procedure/apply?courseId=<...>
```

Endpoint for sending out applications. The header of the request should contain the JWT of the user's session. Only users with **student** and **TA** permissions have access to this endpoint.

| Query parameter | Value                                                 |
| --------------- | ----------------------------------------------------- |
| courseId        | The ID of the course that the user wishes to apply to |

| Response code | Reason                                                       |
| ------------- | ------------------------------------------------------------ |
| 200 OK        | Successful completion                                        |
| 403 FORBIDDEN | User is not permitted to access the endpoint,  user has applied already, or application period expired |
| 404 NOT FOUND | The course ID is invalid (course does not exist)             |

---

## Withdraw

```
POST /api/hiring-procedure/withdraw?courseId=<...>
```

Endpoint for withdrawing applications. The header of the request should contain the JWT of the user's session. Only users with the **admin**, **student** or **TA** role are permitted to access this endpoint.

| Query parameter | Value                                                        |
| --------------- | ------------------------------------------------------------ |
| courseId        | The ID of the course that the user wishes to withdraw their application from |

| Response code          | Reason                                                    |
| ---------------------- | --------------------------------------------------------- |
| 200 OK                 | Successful completion                                     |
| 403 FORBIDDEN          | User is not permitted to access the endpoint              |
| 405 METHOD NOT ALLOWED | Application does not exist, or has been processed already |

---

## Reject

```
POST /api/hiring-procedure/reject?applicationId=<...>
```

Endpoint for rejecting an application that is associated to the provided ID. The header of the request should contain the JWT of the user's session. Only users with the **admin** or **lecturer** role are permitted to access this endpoint.

| Query parameter | Value                               |
| --------------- | ----------------------------------- |
| applicationId   | The ID of the application to reject |

| Response code          | Reason                                       |
| ---------------------- | -------------------------------------------- |
| 200 OK                 | Successful completion                        |
| 403 FORBIDDEN          | User is not permitted to access the endpoint |
| 404 NOT FOUND          | Application does not exist                   |
| 405 METHOD NOT ALLOWED | Application has been processed already       |

---

## Hire

```
POST /api/hiring-procedure/hire-TA?userId=<...>&courseId=<...>
```

Endpoint for hiring a candidate TA for a specific course. The header of the request should contain the JWT of the user's session. Only users with **admin** or **lecturer** permissions have access.

| Query parameter | Value                                              |
| --------------- | -------------------------------------------------- |
| userId          | The ID of the user associated to the application   |
| courseId        | The ID of the course associated to the application |

| Response code          | Reason                                                       |
| ---------------------- | ------------------------------------------------------------ |
| 200 OK                 | Successful completion                                        |
| 403 FORBIDDEN          | User is not permitted to access the endpoint, or no application was found |
| 404 NOT FOUND          | Course or user does not exist                                |
| 405 METHOD NOT ALLOWED | Application has been processed already                       |

---

## Retrieval

```
GET  /api/hiring-procedure/get-all-applications?courseId=<...>
```

Endpoint for retrieving all candidate TAs for a specific course. The header of the request should contain the JWT of the user's session. Only users with **lecturer** or **admin** permissions have access.

| Query parameter | Value                                               |
| --------------- | --------------------------------------------------- |
| courseId        | The ID of the course associated to the applications |

| Response code | Reason                                                       |
| ------------- | ------------------------------------------------------------ |
| 200 OK        | Successful completion                                        |
| 403 FORBIDDEN | User is not permitted to access the endpoint, or no application was found |
| 404 NOT FOUND | The course does not exist                                    |

---

```
GET  /api/hiring-procedure/get-applications?userId=<...>
```

Endpoint for retrieving all applications of a specific user. The header of the request should contain the JWT of the user's session. Only users with **lecturer** or **admin** permissions have access. The same applies for users that have the **same** ID as the ID that is specified in the request.

| Query parameter | Value                                             |
| --------------- | ------------------------------------------------- |
| userId          | The ID of the user associated to the applications |

| Response code | Reason                                                       |
| ------------- | ------------------------------------------------------------ |
| 200 OK        | Successful completion                                        |
| 403 FORBIDDEN | User is not permitted to access the endpoint, or no application was found |

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
| 403 FORBIDDEN | User is not permitted to access the endpoint, or no application was found |
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
| 403 FORBIDDEN | User is not permitted to access the endpoint, or no application was found |
| 404 NOT FOUND | The specified course does not exist                          |

---

## Set Max Hours

```
POST  /api/hiring-procedure/set-max-hours?applicationId=<...>
```

Endpoint for setting the maximum allowed contractual hours for an application. The header of the request should contain the JWT of the user's session. Only lecturers and admins are allowed to use this endpoint.

| Query parameter | Value                                                       |
| --------------- | ----------------------------------------------------------- |
| applicationId   | The ID of the application for which to change the max hours |

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
| 404 NOT FOUND   | The specified application does not exist                     |

---

## Get Max Hours

```
GET  /api/hiring-procedure/get-max-hours?courseId=<...>
```

Endpoint for getting the maximum allowed contractual hours for an application. The header of the request should contain the JWT of the user's session. Only TAs are allowed to use this endpoint.

| Query parameter | Value                                                       |
| --------------- | ----------------------------------------------------------- |
| courseId        | The ID of the course associated to the application          |

| Response code | Reason                                                       |
| ------------- | ------------------------------------------------------------ |
| 200 OK        | Successful completion                                        |
| 403 FORBIDDEN | User is not permitted to access the endpoint                 |
| 404 NOT FOUND | The specified application does not exist                     |

---

```
GET  /api/hiring-procedure/get-max-hours?userId=<...>&courseId=<...>
```

Endpoint for getting the maximum allowed contractual hours for an application. The header of the request should contain the JWT of the user's session. Only lecturers and admins are allowed to use this endpoint.

| Query parameter | Value                                                       |
| --------------- | ----------------------------------------------------------- |
| userId          | The ID of the user associated to the application            |
| courseId        | The ID of the course associated to the application          |

| Response code | Reason                                                       |
| ------------- | ------------------------------------------------------------ |
| 200 OK        | Successful completion                                        |
| 403 FORBIDDEN | User is not permitted to access the endpoint                 |
| 404 NOT FOUND | The specified application does not exist                     |
---

## Rate

```
POST  /api/hiring-procedure/rate?applicationId=<...>
```

Endpoint for rating a TA. The header of the request should contain the JWT of the user's session. Only lecturers and admins are allowed to use this endpoint.

| Query parameter | Value                                                       |
| --------------- | ----------------------------------------------------------- |
| applicationId   | The ID of the application that will be rated                |

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
| 403 FORBIDDEN   | The specified application is not approved                    |
| 404 NOT FOUND   | The specified application does not exist                     |

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
| 403 FORBIDDEN | The specified application is not approved or not rated yet   |
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
| 403 FORBIDDEN | The specified application is not approved or not rated yet   |
| 404 NOT FOUND | The specified application does not exist                     |