# Authentication - API Document

## Register

```
POST   /api/auth/register
```

Endpoint for registering a user. No privileges are needed. For security reasons, everyone is registered as a student initially, but can be promoted by a lecturer or an admin later.

**Expected body format:**

```json
{
    "username" : "jegor",
    "password" : "amogus",
    "userId" : 5221334
}
```

| Response code | Reason                                                   |
| ------------- | -------------------------------------------------------- |
| 200 OK        | Successful completion                                    |
| 409 CONFLICT  | The user with the given username or NetID already exists |

In case there is a _CONFLICT_, the following response is returned;

```json
{
  "message" : "User with username jegor or NetID 5221334 already exists!"
}
```

---

## Change password

```
PUT    /api/auth/change_password
```

Endpoint for changing the password. Can be done by everyone for their own account. Requires a valid JWT token.

**Expected body format:**

```json
{
    "username" : "jegor",
    "newPassword" : "sus"
}
```

_NB! Include the JWT token in &#39;Authorization&#39; header and make sure it starts with &#39;Bearer &#39; prefix._

| Response code | Reason                                                       |
| ------------- | ------------------------------------------------------------ |
| 200 OK        | Successful completion                                        |
| 403 FORBIDDEN | Username does not correspond to the ID in the authorization token, or the token is missing |

---

## Login

```
POST   /api/auth/login
```

Endpoint for logging in. Requires valid credentials. In case of success, sends back the JWT token.

**Expected body format:**

```json
{
    "username" : "jegor?",
    "password" : "sus"
}
```

Upon successful login, all notifications that are available for the user that logs in are send to the user using the format below. The token that is provided by the authentication microservice can be found in the *Authorization* header value.

```json
{
  "notifications": [
    {
      "message": "Hey there, you are hired!",
      "notificationDate" : "17:55 10-12-2020 Europe/Berlin"
    },
    {
      "message": "Hey there, you are fired!",
      "notificationDate": "16:20 25-12-2021 Europe/Berlin"
    }
  ]
}
```

| Header attribute | Value                                                        |
| ---------------- | ------------------------------------------------------------ |
| Authorization    | Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqZWdvcj8iLCJyb2xlIjoiU1RVREVOVCIsImlhdCI6MTYzODM2NzE5NCwiZXhwIjoxNjM4MzY3MTk0fQ.a0WJ2NbP4ytAtGpd5PlVU\_mvrEGLpcCxcCNYy8AgNom3IplBTViNZuP0WTgymhXJZU8k-YYRQYR8DxZrgT7w6A |

| Response code | Reason                               |
| ------------- | ------------------------------------ |
| 200 OK        | Successful completion                |
| 403 FORBIDDEN | The provided credentials are invalid |

---

## Change Role

```
PUT    /api/auth/change_role
```

Endpoint for changing the role of a user. Can only be done by admins or lecturers (a lecturer can only change a user to be a TA, candidate TA or a student), whereas an admin can do everything.

**Expected body format:**

```json
{
    "username" : "jegor?",
    "role" : "LECTURER"
}
```

_NB! Include the JWT token in &#39;Authorization&#39; header and make sure it starts with &#39;Bearer &#39; prefix._

| Response code | Reason                                                       |
| ------------- | ------------------------------------------------------------ |
| 200 OK        | Successful completion                                        |
| 403 FORBIDDEN | The authorization token is invalid, or a lecturer attempts to promote a user to a role that is at least as high as the lecturer |

---

## Delete

```
DELETE /api/auth/delete
```

Endpoint for deleting a user. Can only be done by **admins**.

**Expected body format:**

```json
{
    "username" : "jegor?"
}
```

_NB! Include the JWT token in &#39;Authorization&#39; header and make sure it starts with &#39;Bearer &#39; prefix._

| Response code | Reason                                                       |
| ------------- | ------------------------------------------------------------ |
| 200 OK        | Successful completion                                        |
| 403 FORBIDDEN | The user is not authorized to perform the action. **Admin** privilege is required |