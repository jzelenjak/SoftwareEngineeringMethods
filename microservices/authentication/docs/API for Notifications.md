# API for Notifications

## General API path: /api/auth/notifications


## Getting notifications from a specific user:

**URL:** */api/auth/notifications/get*

**HTTP method:** GET

**Expected body format:**

```json
{
  "userId" : 5535552
}
```

**Successful:** 200 OK

```json
[
  {
    "message" : "Hey there, you are hired!",
    "notificationDate" : "18:55 10-12-2021 Europe/Berlin"
  },

  {
    "message" : "...",
    "notificationDate" : "..."
  }
]
```

**Important notes:** Only admin can do this. Add admin JWT token to AUTHORIZATION header.


## Adding new notifications to the database:

**URL:** */api/auth/notifications/add*

**HTTP method:** POST

**Expected body format:**

```json

{
  "userId" : 4556673,
  "message" : "some message"
}
```

**Successful:** 200 OK

**Important notes:** Only admin/lecturer can do this. Add admin/lecturer JWT token to AUTHORIZATION header.


## Changing user from existing notification:

**URL:** */api/auth/notifications/change_user*

**HTTP method:** PUT

**Expected body format:**

```json
{
  "notificationId" : 342,
  "newUser" : 5676889
}
```

**Successful:** 200 OK

**Important notes:** Only admin can do this. Add admin JWT token to AUTHORIZATION header.


## Changing message from existing notification:

**URL:** */api/auth/notifications/change_message*

**HTTP method:** PUT

**Expected body format:**

```json
{
  "notificationId" : 443,
  "newMessage" : "some new message"
}
```

**Successful:** 200 OK

**Important notes:** Only admin can do this. Add admin JWT token to AUTHORIZATION header.


## Deleting existing notification by notificationId:

**URL:** */api/auth/notifications/delete*

**HTTP method:** DELETE

**Expected body format:**

```json
{
  "notificationId" : 445
}
```
**Successful:** 200 OK

**Important notes:** Only admin can do this. Add admin JWT token to AUTHORIZATION header.


## Deleting existing notification(s) by userId:

**URL:** */api/auth/notifications/delete_user*

**HTTP method:** DELETE

**Expected body format:**

```json
{
  "userId" : 5643221
}
```

**Successful:** 200 OK

**Important notes:** Only admin can do this. Add admin JWT token to AUTHORIZATION header.


