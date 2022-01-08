# Hour Management - API Document

## Fetch declarations

```
GET  /api/hour-management/declaration
```

Endpoint for fetching all stored declarations. This includes both approved, and unapproved declarations. This endpoint requires **admin** or **lecturer** privileges.

Upon calling this endpoint, a list of declaration objects is returned in a JSON format. See the example below.

```json
[
    {
        "declarationId": 12345,
        "studentId": 54321,
        "courseId": 78910,
        "approved": true,
        "declaredHours": 10.5,
        "declarationData": "<date>"
    },
    {
        "...": "..."
    }
]
```

| Response code | Reason                                       |
| ------------- | -------------------------------------------- |
| 200 OK        | Successful completion                        |
| 404 NOT FOUND | No declarations in the system                |
| 403 FORBIDDEN | User is not permitted to access the endpoint |

---

```
GET  /api/hour-management/declaration/{id}
```

Endpoint used to fetch a single declaration. Only accessible by **admins** and **lecturers**. The response object is similar to that of the `/api/hour-management/declaration` endpoint;

```json
{
    "declarationId": 12345,
    "studentId": 54321,
    "courseId": 78910,
    "approved": true,
    "declaredHours": 10.5,
    "declarationData": "<date>"
}
```

The *id* used within the path is the ID of the declaration.

| Response code | Reason                                       |
| ------------- | -------------------------------------------- |
| 200 OK        | Successful completion                        |
| 404 NOT FOUND | No declarations in the system                |
| 403 FORBIDDEN | User is not permitted to access the endpoint |

---

```
GET  /api/hour-management/declaration/unapproved
```

Endpoint used for fetching *all* unapproved hour declarations. Only **admins** and **lecturers** have permission to access this endpoint. The response object is similar to that of the `/api/hour-management/declaration` endpoint;

```json
[
    {
        "declarationId": 12345,
        "studentId": 54321,
        "courseId": 78910,
        "approved": true,
        "declaredHours": 10.5,
        "declarationData": "<date>"
    },
    {
        "...": "..."
    }
]
```

| Response code | Reason                                       |
| ------------- | -------------------------------------------- |
| 200 OK        | Successful completion                        |
| 404 NOT FOUND | No declarations in the system                |
| 403 FORBIDDEN | User is not permitted to access the endpoint |

---

```
GET  /api/hour-management/declaration/student/{id}
```

Endpoint used for fetching the declarations of specific student specified by the *id* in the request path. Only **admins** and **lecturers** have permission to access this endpoint. (Planned to make it so that the user himself can access this as well). The response object is similar to that of the `/api/hour-management/declaration` endpoint;

```json
[
    {
        "declarationId": 12345,
        "studentId": 54321,
        "courseId": 78910,
        "approved": true,
        "declaredHours": 10.5,
        "declarationData": "<date>"
    },
    {
        "...": "..."
    }
]
```

The *id* used within the path is the ID of the declaration.

| Response code   | Reason                                       |
| --------------- | -------------------------------------------- |
| 200 OK          | Successful completion                        |
| 400 BAD REQUEST | No declarations by user in the system        |
| 403 FORBIDDEN   | User is not permitted to access the endpoint |

---

## Declare hours

```
POST /api/hour-management/declaration
```

Endpoint used to declare hours. Only **admins** and **TAs** are permitted to declare hours. The information with respect to the declaration is placed in the body of the request. This request body should look similar to the JSON object below.

```json
{
    "studentId": 54321,
    "courseId": 78910,
    "declaredHours": 10.5
}
```

| Response code   | Reason                                                             |
| --------------- | ------------------------------------------------------------------ |
| 200 OK          | Successful completion                                              |
| 403 FORBIDDEN   | User is not permitted to access the endpoint                       |
| 400 BAD REQUEST | Declaration is not within valid course time or contract is invalid |

---

## Reject declarations

```
DELETE  /api/hour-management/{id}/reject
```

Endpoint used to reject an hour declaration. Only **admins** and **lecturers** are permitted to reject hour declarations. Upon rejection, a notification is sent to the TA.

The *id* used within the path is the ID of the declaration.

| Response code   | Reason                                                   |
| --------------- | -------------------------------------------------------- |
| 200 OK          | Successful completion                                    |
| 400 BAD REQUEST | Declaration does not exist                               |
| 404 NOT FOUND   | Declaration has already been approved                    |
| 403 FORBIDDEN   | User is not permitted to access the endpoint             |

---

## Approve declarations

```
PUT  /api/hour-management/{id}/approve
```

Endpoint used to approve an hour declaration. Only **admins** and **lecturers** are permitted to approve hour declarations. Upon approval, a notification is sent to the TA.

The *id* used within the path is the ID of the declaration.

| Response code   | Reason                                                   |
| --------------- | -------------------------------------------------------- |
| 200 OK          | Successful completion                                    |
| 400 BAD REQUEST | Declaration does not exist                               |
| 404 NOT FOUND   | Declaration has already been approved                    |
| 403 FORBIDDEN   | User is not permitted to access the endpoint             |

---

## Statistics

```
POST  /api/hour-management/declaration/statistics/total-hours
```

Endpoint for retrieving the total amount of hours worked by a TA on a specific course. This endpoint is only accessible by **admins**, **lecturers**, and **TAs**. All requests towards this endpoint require the use of the following body used to request the desired data;

```json
{
    "studentId": 54321,
    "courseId": 78910
}
```

| Response code | Reason                                                   |
| ------------- | -------------------------------------------------------- |
| 200 OK        | Successful completion                                    |
| 404 NOT FOUND | No statistics found for the specified student and course |
| 403 FORBIDDEN | User is not permitted to access the endpoint             |

---

```
POST /api/hour-management/statistics/total-user-hours
```

Endpoint for retrieving the total amount of hours worked per TA per course given a list of student ids and course id. This endpoint is accessible by **admins**, **lecturers**. All requests towards this endpoint require the use of the following body used to request the desired data;

```json
{
  "amount": 1,
  "minHours": 1,
  "studentIds": [1234, 5678],
  "courseIds": [4321, 8765]
}
```

The response object gives a map-like structure, where student ids are matched to their total hours. They are ordered by the total hours and are limited by the amount provided.

```json
{
  "1234": 12,
  "5678": 5
}
```


| Response code | Reason                                                     |
| ------------- | ---------------------------------------------------------- |
| 200 OK        | Successful completion                                      |
| 404 NOT FOUND | No statistics found for the specified students and courses |
| 403 FORBIDDEN | User is not permitted to access the endpoint               |

---

```
POST /api/hour-management/statistics/aggregation-stats
```

Endpoint for retrieving the mode, median, and standard deviation of hours worked given a list of student ids and course id. This endpoint is accessible by **admins**, **lecturers**. All requests towards this endpoint require the use of the following body used to request the desired data;

```json
{
  "studentIds": [1234, 5678],
  "courseIds": [4321, 8765]
}
```

The response object is an aggregation statistics object that contains the mode, median, and standard deviation.

```json
{
  "mode": 12.1,
  "median": 5,
  "standardDeviation": 123.551
}
```

| Response code | Reason                                                     |
| ------------- | ---------------------------------------------------------- |
| 200 OK        | Successful completion                                      |
| 404 NOT FOUND | No statistics found for the specified students and courses |
| 403 FORBIDDEN | User is not permitted to access the endpoint               |