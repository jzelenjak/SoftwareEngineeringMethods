# API for Authentication microservice

## Register

**Description**: Endpoint for registering a user. No privileges are needed. For security reasons, everyone is registered as a student initially, but can be promoted by a lecturer or an admin later.

**Request path:** */api/auth/register*

**HTTP method:** POST

**Expected body format:**

```json
{
    "username" : "jegor",

    "password" : "amogus",

    "userId" : "5221334"
}
```

**Success:**

200 OK

**Failure:**

_409 CONFLICT_ if the user with the given username (netid) already exists.

```json
{
    "message" : "User with netid jegor already exists!"
}
```


## Change password

**Description**: Endpoint for changing the password. Can be done by everyone for their own account. Requires a valid JWT token.

**Request path:** */api/auth/change_password*

**HTTP method:** PUT

**Expected body format:**

```json
{
    "username" : "jegor",

    "newPassword" : "sus"
}
```

_NB! Include the JWT token in &#39;Authorization&#39; header and make sure it starts with &#39;Bearer &#39; prefix._

**Success:**

_200 OK_

**Failure:**

403 FORBIDDEN (default by Spring Boot Security)

_403 FORBIDDEN_ if the username does not correspond to the the id in the token.

```json
{
    "message" : "You are not amogus and are not allowed to change password!"
}
```


## Login

**Description**: Endpoint for logging in. Requires valid credentials. In case of success, sends back the JWT token.

**Request path:** */api/auth/login*

**HTTP method:** GET

**Expected format:**

```json
{
    "username" : "jegor?",

    "password" : "sus"
}
```

**Success:**

_200 OK_

&#39;Authorization&#39; : &#39;Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqZWdvcj8iLCJyb2xlIjoiU1RVREVOVCIsImlhdCI6MTYzODM2NzE5NCwiZXhwIjoxNjM4MzY3MTk0fQ.a0WJ2NbP4ytAtGpd5PlVU\_mvrEGLpcCxcCNYy8AgNom3IplBTViNZuP0WTgymhXJZU8k-YYRQYR8DxZrgT7w6A&#39;

(Header)

**Failure:**

_403 FORBIDDEN_ if invalid credentials have been provided.

```json
{
    "message" : "Invalid credentials."
}
```


## Change Role

**Description**: Endpoint for changing the role of a user. Can only be done by admins or lecturers (a lecturer can only change a user to be a TA, candidate TA or a student), whereas an admin can do everything.

**Request path:** */api/auth/change_role*

**HTTP method:** PUT

**Expected format:**

```json
{
    "username" : "jegor?",

    "role" : "LECTURER"
}
```

_NB! Include the JWT token in &#39;Authorization&#39; header and make sure it starts with &#39;Bearer &#39; prefix._

**Success:**

_200 OK_

**Failure:**

_403 FORBIDDEN_ (default by Spring Boot Security)

_403 FORBIDDEN_ if a lecturer attempts to do what they are not allowed to do.

```json
{
    "message" : "You are not allowed to do that as a lecturer!"
}
```


## Delete

**Description**: Endpoint for deleting a user. Can only be done by admins.

**Request path:** */api/auth/delete*

**HTTP method:** DELETE

**Expected format:**

```json
{
    "username" : "jegor?"
}
```

_NB! Include the JWT token in &#39;Authorization&#39; header and make sure it starts with &#39;Bearer &#39; prefix._

**Success:**

_200 OK_

**Failure:**

_403 FORBIDDEN_ if the requester is not an admin.