# Notifications - API Document


## Get notifications of specific user

```
GET    /api/auth/notifications/get
```

Endpoint for retrieving all notifications for a particular user. The response consists of a JSON object that contains an optional list of notifications.

**Expected body format:**

```json
{
  "userId" : 5535552
}
```

Upon successful completion, the following response is returned;

```json
{
  "notifications": [
    {
      "message": "Hey there, you are hired!",
      "notificationDate" : "17:54 10-12-2021 Europe/Berlin"
    }
    {
      "message": "Hey there, you are fired!",
      "notificationDate": "16:20 25-12-2021 Europe/Berlin"
    }
  ]
}
```

_NB! Include the JWT token in &#39;Authorization&#39; header and make sure it starts with &#39;Bearer &#39; prefix._

| Response code | Reason                                       |
| ------------- | -------------------------------------------- |
| 200 OK        | Successful completion                        |
| 403 FORBIDDEN | User is not permitted to access the endpoint |

---

## Add new notification

```
POST   /api/auth/notifications/add
```

Endpoint used to add a notification for a particular user to the notification queue. Only **admins** and **lecturers** are permitted to send/generate notifications.

**Expected body format:**

```json
{
  "userId" : 4556673,
  "message" : "some message"
}
```

_NB! Include the JWT token in &#39;Authorization&#39; header and make sure it starts with &#39;Bearer &#39; prefix._

| Response code | Reason                                       |
| ------------- | -------------------------------------------- |
| 200 OK        | Successful completion                        |
| 403 FORBIDDEN | User is not permitted to access the endpoint |

---

## Change recipient of existing notification

```
PUT    /api/auth/notifications/change_user
```

Endpoint used to change the recipient of a notification. Can only be performed by **admins**.

**Expected body format:**

```json
{
  "notificationId" : 342,
  "newUser" : 5676889
}
```

_NB! Include the JWT token in &#39;Authorization&#39; header and make sure it starts with &#39;Bearer &#39; prefix._

| Response code | Reason                                       |
| ------------- | -------------------------------------------- |
| 200 OK        | Successful completion                        |
| 403 FORBIDDEN | User is not permitted to access the endpoint |

---

## Change message of existing notification

```
PUT    /api/auth/notifications/change_message
```

Endpoint used to change the message of a notification. Can only be performed by **admins**.

**Expected body format:**

```json
{
  "notificationId" : 443,
  "newMessage" : "some new message"
}
```

_NB! Include the JWT token in &#39;Authorization&#39; header and make sure it starts with &#39;Bearer &#39; prefix._

| Response code | Reason                                       |
| ------------- | -------------------------------------------- |
| 200 OK        | Successful completion                        |
| 403 FORBIDDEN | User is not permitted to access the endpoint |

---

## Deleting existing notification by notification ID

```
DELETE /api/auth/notifications/delete
```

Endpoint used to delete an existing notification by its ID. Can only be performed by **admins**.

**Expected body format:**

```json
{
  "notificationId" : 445
}
```
_NB! Include the JWT token in &#39;Authorization&#39; header and make sure it starts with &#39;Bearer &#39; prefix._

| Response code | Reason                                       |
| ------------- | -------------------------------------------- |
| 200 OK        | Successful completion                        |
| 403 FORBIDDEN | User is not permitted to access the endpoint |

---

## Deleting existing notification(s) by user ID:

```
DELETE /api/auth/notifications/delete_user
```

Endpoint for deleting existing notifications that are associated to the provided user ID. Can only be performed by **admins**.

**Expected body format:**

```json
{
  "userId" : 5643221
}
```

_NB! Include the JWT token in &#39;Authorization&#39; header and make sure it starts with &#39;Bearer &#39; prefix._

| Response code | Reason                                       |
| ------------- | -------------------------------------------- |
| 200 OK        | Successful completion                        |
| 403 FORBIDDEN | User is not permitted to access the endpoint |

