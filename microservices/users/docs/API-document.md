# API for Users Microservice

## Register

```
POST   /api/users/register
```

Endpoint for registering a user. It also registers the user with the Authentication microservice. No privileges are needed. For security reasons, everyone is registered as a student initially, but can be promoted by a teaches or an admin later.

*NB! The token will NOT be sent back. Use /api/auth/login to get it.*

**Expected body format**:

```json
{
	"username" : "amogus@student.tudelft.nl",
	"password" : "amogusamogus",
	"firstName" : "Amogus",
	"lastName" : "Impostor"
}
```

Upon successful completion, the following response is sent back;

```json
{
	"userId" : 5002222
}
```

| Response code   | Reason                                                       |
| --------------- | ------------------------------------------------------------ |
| 200 OK          | Successful completion                                        |
| 400 BAD REQUEST | Request format is incorrect                                  |
| 409 CONFLICT    | There already exists a user with the same username (aka NetID) |
| 4xx             | Error while registering the user in the authentication microservice |

---

## Get By Username

```
GET     /api/users/by_username
```

Endpoint for getting a user by the username. No privileges are needed.

**Expected body format**:

```json
{
	"username" : "amogus@student.tudelft.nl"
}
```

Upon success, the following response is returned;

```json
{
	"userId" : 5002222,
	"username" :"amogus@student.tudelft.nl",
	"firstName" :"Amogus",
	"lastName" : "Impostor",
	"role" : "STUDENT"
}
```

| Response code   | Reason                      |
| --------------- | --------------------------- |
| 200 OK          | Successful completion       |
| 400 BAD REQUEST | Invalid request format      |
| 404 NOT FOUND   | The user has not been found |

---

## Get By User ID

```
GET     /api/users/by_userid
```

Endpoint for getting a user by the userId. No privileges are needed.

**Expected body format**:

```json
{
	"userId" : 5002222
}
```

Upon success, the following response is returned;

```json
{
	"userId" : 5002222,
	"username" :"amogus@student.tudelft.nl",
	"firstName" :"Amogus",
	"lastName" : "Impostor",
	"role" : "STUDENT"
}
```
| Response code   | Reason                      |
| --------------- | --------------------------- |
| 200 OK          | Successful completion       |
| 400 BAD REQUEST | Invalid request format      |
| 404 NOT FOUND   | The user has not been found |

---

## Get By Role

```
GET    /api/users/by_role
```

Endpoint for getting all users with the given role. No privileges are needed.

**Expected body format**:

```json
{
	"role" : "STUDENT"
}
```

(can also be lower-cased, but for consistency, better to send upper-cased.)

Upon success, the following response is returned;

```json
[
	{
		"userId" : 5002123,
		"username" : "useruseruser",
		"firstName" : "Jegor",
		"lastName" : "Zelenjak",
		"role" : "STUDENT"
	},
	{
		"..." : "..."
	}
]
```
| Response code   | Reason                                           |
| --------------- | ------------------------------------------------ |
| 200 OK          | Successful completion                            |
| 400 BAD REQUEST | Invalid request format, or role does not exist   |
| 404 NOT FOUND   | No users were found that have the specified role |

---

## Change Role

```
PUT    /api/users/change_role
```

Endpoint for changing the role of a user. Can only be done by **admins** or **lecturers** (a teaches can *only* change a user to be a TA, or a student), whereas an admin can do everything. It also changes the role in Authentication Microservice.

**Expected body format**:

```json
{
	"userId" : 5002222,
	"role" : "TA"
}
```

_NB! Include the JWT token in &#39;Authorization&#39; header and make sure it starts with &#39;Bearer &#39; prefix._

| Response code    | Reason                                                       |
| ---------------- | ------------------------------------------------------------ |
| 200 OK           | Successful completion                                        |
| 400 BAD REQUEST  | Invalid request format                                       |
| 401 UNAUTHORIZED | Invalid token, or user does not have sufficient permissions  |
| 4xx              | Error while changing the role of the user in the authentication microservice |

---

## Delete By User ID

```
DELETE /api/users/delete
```

Endpoint for deleting a user. Can only be done by **admins**. It also deletes the user in Authentication Microservice.

**Expected body format**:

```json
{
	"userId" : 5002222
}
```

_NB! Include the JWT token in &#39;Authorization&#39; header and make sure it starts with &#39;Bearer &#39; prefix._

| Response code    | Reason                                                       |
| ---------------- | ------------------------------------------------------------ |
| 200 OK           | Successful completion                                        |
| 400 BAD REQUEST  | Invalid request format                                       |
| 401 UNAUTHORIZED | Invalid token, or user does not have sufficient permissions  |
| 404 NOT FOUND    | User with the specified ID does not exist                    |
| 4xx              | Error while changing the role of the user in the authentication microservice |

