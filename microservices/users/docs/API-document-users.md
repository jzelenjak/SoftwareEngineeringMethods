# Users - API Document

## Register

```
POST   /api/users/register
```

Endpoint for registering a user. This endpoint requires no privileges. For security reasons, everyone is registered as a student initially, but can be promoted by an admin later.
This endpoint also registers the user with the authentication microservice. *NB! The token will NOT be sent back - Use `/api/auth/login` to get it.*
The information with respect to the user is placed in the body of the request. This request body should look similar to the JSON object below.

```json
{
	"username" : "aimpostor",
	"password" : "amogusamogus",
	"firstName" : "Amogus",
	"lastName" : "Impostor"
}
```

Upon calling this endpoint, the generated user ID is returned in a JSON format. See the example below.

```json
{
	"userId" : 5002222
}
```

| Response code   | Reason                                                              |
|-----------------|---------------------------------------------------------------------|
| 200 OK          | Successful completion                                               |
| 400 BAD REQUEST | Invalid request format                                              |
| 409 CONFLICT    | There already exists a user with the same username (aka NetID)      |
| 4xx             | Error while registering the user in the authentication microservice |

---

## Fetch users

```
GET     /api/users/by_username?username=aimpostor
```

Endpoint for getting a user by the username. This endpoint requires **admin** or **lecturer** privileges.

Upon calling this endpoint, a user object is returned in a JSON format. See the example below.

```json
{
	"userId" : 4242442,
	"username" :"aimpostor",
	"firstName" :"Amogus",
	"lastName" : "Impostor",
	"role" : "STUDENT"
}
```

The *username* used in the query string is the username of the user.

| Response code    | Reason                                                  |
|------------------|---------------------------------------------------------|
| 200 OK           | Successful completion                                   |
| 400 BAD REQUEST  | Invalid request format                                  |
| 401 UNAUTHORIZED | JWT token is missing or does not start with 'Bearer '   |
| 403 FORBIDDEN    | The operation is forbidden for the requester            |
| 404 NOT FOUND    | The user with the specified username has not been found |

---

```
GET     /api/users/by_userid?userId=4242442
```

Endpoint for getting a user by the userId. This endpoint requires **admin** or **lecturer** privileges. The response object is similar to that of the `/api/users/by_username?username=aimpostor` endpoint.

```json
{
	"userId" : 4242442,
	"username" :"aimpostor",
	"firstName" :"Amogus",
	"lastName" : "Impostor",
	"role" : "STUDENT"
}
```

The *userId* used in the query string is the user ID of the user.

| Response code    | Reason                                                 |
|------------------|--------------------------------------------------------|
| 200 OK           | Successful completion                                  |
| 400 BAD REQUEST  | Invalid request format                                 |
| 401 UNAUTHORIZED | JWT token is missing or does not start with 'Bearer '  |
| 403 FORBIDDEN    | The operation is forbidden for the requester           |
| 404 NOT FOUND    | The user with the specified user ID has not been found |

---

```
GET     /api/users/by_first_name?firstName=Amogus
```

Endpoint for getting users by the first name. This endpoint requires **admin** privileges. The response object is similar to that of the `/api/users/by_username?username=aimpostor` endpoint.

```json
[
  {
	"userId" : 4242442,
	"username" :"aimpostor",
	"firstName" :"Amogus",
	"lastName" : "Impostor",
	"role" : "STUDENT"
  },
  {
    "..." : "..."
  }
]
```

The *firstName* used in the query string is the first name of a user.

| Response code    | Reason                                                 |
|------------------|--------------------------------------------------------|
| 200 OK           | Successful completion                                  |
| 400 BAD REQUEST  | Invalid request format                                 |
| 401 UNAUTHORIZED | JWT token is missing or does not start with 'Bearer '  |
| 403 FORBIDDEN    | The operation is forbidden for the requester           |
| 404 NOT FOUND    | No users with the specified first name have been found |

---

```
GET     /api/users/by_last_name?lastName=Impostor
```

Endpoint for getting users by the last name. This endpoint requires **admin** privileges. The response object is similar to that of the `/api/users/by_username?username=aimpostor` endpoint.

```json
[
  {
	"userId" : 4242442,
	"username" :"aimpostor",
	"firstName" :"Amogus",
	"lastName" : "Impostor",
	"role" : "STUDENT"
  },
  {
    "..." : "..."
  }
]
```

The *lastName* used in the query string is the last name of a user.

| Response code    | Reason                                                |
|------------------|-------------------------------------------------------|
| 200 OK           | Successful completion                                 |
| 400 BAD REQUEST  | Invalid request format                                |
| 401 UNAUTHORIZED | JWT token is missing or does not start with 'Bearer ' |
| 403 FORBIDDEN    | The operation is forbidden for the requester          |
| 404 NOT FOUND    | No users with the specified last name have been found |

---


```
GET    /api/users/by_role?role=STUDENT
```

Endpoint for getting all users with the given role. This endpoint requires **admin** or **lecturer** privileges.

Upon calling this endpoint, a list of user objects is returned in a JSON format. See the example below.

```json
[
	{
		"userId" : 4242442,
		"username" : "aimpostor",
		"firstName" : "Amogus",
		"lastName" : "Impostor",
		"role" : "STUDENT"
	},
	{
		"..." : "..."
	}
]
```

The *role* used in the query string is the role of the user (can also be lower-cased).

| Response code    | Reason                                                |
|------------------|-------------------------------------------------------|
| 200 OK           | Successful completion                                 |
| 400 BAD REQUEST  | Invalid request format, or role does not exist        |
| 401 UNAUTHORIZED | JWT token is missing or does not start with 'Bearer ' |
| 403 FORBIDDEN    | The operation is forbidden for the requester          |
| 404 NOT FOUND    | No users with the specified role have been found      |

---

## Changing users

```
PUT    /api/users/change_role
```

Endpoint for changing the role of a user. This endpoint requires **admin** privileges. It also changes the role in Authentication Microservice. The information with respect to changing the role is placed in the body of the request. This request body should look similar to the JSON object below.

```json
{
	"userId" : 4242442,
	"role" : "ADMIN"
}
```

| Response code    | Reason                                                                       |
|------------------|------------------------------------------------------------------------------|
| 200 OK           | Successful completion                                                        |
| 400 BAD REQUEST  | Invalid request format, or role does not exist                               |
| 401 UNAUTHORIZED | JWT token is missing or does not start with 'Bearer '                        |
| 403 FORBIDDEN    | The operation is forbidden for the requester                                 |
| 404 NOT FOUND    | The user with the specified user ID has not been found                       |
| 4xx              | Error while changing the role of the user in the authentication microservice |

---

```
PUT    /api/users/change_first_name
```

Endpoint for changing the first name of a user. This endpoint requires **admin** privileges. The information with respect to changing the first name is placed in the body of the request. This request body should look similar to the JSON object below. Note that the username will remain the same.

```json
{
	"userId" : 4242442,
	"firstName" : "AmogusAmogus"
}
```

| Response code    | Reason                                                                       |
|------------------|------------------------------------------------------------------------------|
| 200 OK           | Successful completion                                                        |
| 400 BAD REQUEST  | Invalid request format                                                       |
| 401 UNAUTHORIZED | JWT token is missing or does not start with 'Bearer '                        |
| 403 FORBIDDEN    | The operation is forbidden for the requester                                 |
| 404 NOT FOUND    | The user with the specified user ID has not been found                       |

---

```
PUT    /api/users/change_last_name
```

Endpoint for changing the last name of a user. This endpoint requires **admin** privileges. The information with respect to changing the last name is placed in the body of the request. This request body should look similar to the JSON object below. Note that the username will remain the same.

```json
{
	"userId" : 4242442,
	"lastName" : "Imposter"
}
```

| Response code    | Reason                                                                       |
|------------------|------------------------------------------------------------------------------|
| 200 OK           | Successful completion                                                        |
| 400 BAD REQUEST  | Invalid request format                                                       |
| 401 UNAUTHORIZED | JWT token is missing or does not start with 'Bearer '                        |
| 403 FORBIDDEN    | The operation is forbidden for the requester                                 |
| 404 NOT FOUND    | The user with the specified user ID has not been found                       |

---

## Delete By User ID

```
DELETE /api/users/delete?userId=4242442
```

Endpoint for deleting a user. This endpoint requires **admin** privileges. It also deletes the user in Authentication Microservice.

The *userId* used in the query string is the user ID of the user.

| Response code    | Reason                                                                       |
|------------------|------------------------------------------------------------------------------|
| 200 OK           | Successful completion                                                        |
| 400 BAD REQUEST  | Invalid request format                                                       |
| 401 UNAUTHORIZED | JWT token is missing or does not start with 'Bearer '                        |
| 403 FORBIDDEN    | The operation is forbidden for the requester                                 |
| 404 NOT FOUND    | The user with the specified user ID has not been found                       |
| 4xx              | Error while changing the role of the user in the authentication microservice |

