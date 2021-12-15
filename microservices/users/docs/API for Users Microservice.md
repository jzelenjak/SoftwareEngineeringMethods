# API for Users Microservice

## Register

**Description**: Endpoint for registering a user. It also registers the user with the Authentication microservice. No privileges are needed. For security reasons, everyone is registered as a student initially, but can be promoted by a lecturer or an admin later.
				 NB! The token will NOT be sent back. Use */api/auth/login* to get it.

**Request path**: */api/users/register*

**HTTP method**: POST

**Request format**:

```json
{
	"username" : "amogus@student.tudelft.nl",

	"password" : "amogusamogus",

	"firstName" : "Amogus",

	"lastName" : "Impostor"
}
```

**Success**:

_200 OK_

```json
{
	"userId" : 5002222
}
```

**Failure**:

_409 CONFLICT_ if the user with the same username (aka netID) is already in the database

_400 BAD REQUEST_ if the request is not according to the format

_Some error code_ if there has been a failure in registering the user with Authentication Microservice.



## Get By Username

**Description**: Endpoint for getting a user by the username. No privileges are needed.

**Request path**: */api/users/by_username*

**HTTP method**: GET

**Request format**:

```json
{
	"username" : "amogus@student.tudelft.nl"
}
```

**Success**:

```json
{

	"userId" : 5002222,
	"username" :"amogus@student.tudelft.nl",
	"firstName" :"Amogus",
	"lastName" : "Impostor",
	"role" : "STUDENT"

}
```

**Failure**:

_404 NOT FOUND_ if the user has not been found

_400 BAD REQUEST_ if the request is not according to the format



## Get By User ID

**Description**: Endpoint for getting a user by the userId. No privileges are needed.

**Request path**: *api/users/by_userid*

**HTTP method**: GET

**Request format**:

```json
{
	"userId" : 5002222
}
```

**Success**:

```json
{
	"userId" : 5002222,
	"username" :"amogus@student.tudelft.nl",
	"firstName" :"Amogus",
	"lastName" : "Impostor",
	"role" : "STUDENT"
}
```
**Failure**:

_404 NOT FOUND_ if the user is not found

_400 BAD REQUEST_ if the provided user ID is not a number or if the request is not according to the format




## Get By Role

**Description**: Endpoint for getting all users with the given role. No privileges are needed.

**Request path**: */api/users/by_role*

**HTTP method**: GET

**Request format**:

```json
{
	"role" : "STUDENT"
}
```

(can also be lower-cased, but for consistency, better to send upper-cased)

**Success**:

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
**Failure**:

_404 NOT FOUND_ if no user is found

_400 BAD REQUEST_ if the provided role is not STUDENT, CANDIDATE_TA, TA, LECTURER OR ADMIN or if the request is not according to the format



## Change Role

**Description**: Endpoint for changing the role of a user. Can only be done by admins or lecturers (a lecturer can only change a user to be a TA, candidate TA or a student), whereas an admin can do everything. It also changes the role in Authentication Microservice.

**Request path**: */api/users/change_role*

**HTTP method** : PUT

**Request format**:

```json
{
	"userId" : 5002222,

	"role" : "TA"
}
```

_NB! Include the JWT token in &#39;Authorization&#39; header and make sure it starts with &#39;Bearer &#39; prefix._

**Success**:

200 OK

```json
{
	"message" : "Changed the role successfully!"
}
```

**Failure**:

_401 UNAUTHORIZED_ if the token is invalid or the user does not have the permissions

(NB! The token must be in the &#39;Authorization&#39; header and must start with &#39;Bearer &#39; prefix)

_400 BAD REQUEST_ if the request does not follow the format

_Some error code_ if there has been a failure in changing the role in authentication service.



## Delete By User ID

**Description**: Endpoint for deleting a user. Can only be done by admins. It also deletes the user in Authentication Microservice.

**Request path**: */api/users/delete*

**HTTP method**: DELETE

**Request format**:

```json
{
	"userId" : 5002222
}
```

_NB! Include the JWT token in &#39;Authorization&#39; header and make sure it starts with &#39;Bearer &#39; prefix._

**Success**:

200 OK

```json
{
	"message" : "User deleted successfully!"
}
```

**Failure**:

400 BAD REQUEST if the provided user ID is not a number or the request does not follow the format

404 NOT FOUND if user with the provided user ID has not been found.

401 UNAUTHORIZED if the requester does not have enough permissions.

(NB! The token must be in the &#39;Authorization&#39; header and must start with &#39;Bearer &#39; prefix)

_Some error code_ if there has been a failure in deleting the user in authentication service.