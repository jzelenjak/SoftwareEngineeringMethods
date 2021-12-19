# Authentication
## Postman examples (manual testing)
<!Post here some (no pun intended) Postman examples for your microservice and what to expect>
**Authentication**:
- ```Login with the root user with username root@tudelft.nl and password "!wAg+enDr<e*@ne:rRvX%".```\
  ![login root user](images/authentication/loginRootSuccess.png)\
  Which gives a HttpStatus ```200 - OK```.\
  ![200 OK response](images/authentication/responseRegisterNewUser.png)\
  And also sets the Authorization header with the JWT from the root user.\
  ![root user JWT](images/authentication/rootUserJWT.png)

- ```Register a new user with username abli and userId 5551234 and password "HashedPassword".```\
   ![register new user](images/authentication/registerNewUser.png)\
   Which gives a HttpStatus ```200 - OK```.\
   ![200 OK response](images/authentication/responseRegisterNewUser.png)

- ```Register the same user with username abli, but different userId 5551235 and password "HashedPassword".```\
  ![register new user same username](images/authentication/registerNewUserSameUsername.png)\
  Which gives a HttpStatus ```409 - CONFLICT```.\
  ![409 CONFLICT response](images/authentication/responseRegisterNewUserSameUsername.png)

- ```Register the same user with different username abxli, but same userId 5551234 and password "HashedPassword".```\
  ![register new user same userId](images/authentication/registerNewUserSameUserId.png)\
  Which gives a HttpStatus ```409 - CONFLICT```.\
  ![409 CONFLICT response](images/authentication/responseRegisterNewUserSameUserId.png)


- ```Login with username abli and password "HashedPassword".```\
  ![login success](images/authentication/loginSuccess.png)\
  Which gives a HttpStatus ```200 - OK```.\
  ![200 OK response](images/authentication/responseRegisterNewUser.png)\
  And sets the jwt token in the Authorization header.
  ![jwt token in authorization header](images/authentication/AuthorizationHeaderWithJWT.png)

- ```Login with username abli and wrong password "hashedpassword".```\
  ![login failed](images/authentication/loginFailed.png)\
  Which gives a HttpStatus ```403 - FORBIDDEN```.\
  ![403 FORBIDDEN response](images/authentication/responseLoginFailed.png)

- ```Change the user abli to TA with the root user without authorization header.```\
  ![change role failed](images/authentication/changeRoleFailed.png)\
  Which gives a HttpStatus ```403 - FORBIDDEN```.\
  ![img.png](images/authentication/responseForbiddenAccessDenied.png)

- ```Now we try the same but now with the authorization header from the root user.```\
  ![img.png](images/authentication/changeRoleSuccessWithRootUser.png)\
  Which gives a HttpStatus ```200 - OK```.\
  ![200 OK response](images/authentication/responseRegisterNewUser.png)


- ```Change the password of the root user with sending the authorization header containing the JWT from the root user.```\
  ![img.png](images/authentication/changePassword.png)\
  ![img.png](images/authentication/changeRoleSuccessWithRootUser.png)\
  Which gives a HttpStatus ```200 - OK```.\
  ![200 OK response](images/authentication/responseRegisterNewUser.png)

- ```Try logging in with the root user with the old password.```\
  Which gives a HttpStatus ```403 - FORBIDDEN```.\
  ![img.png](images/authentication/rootUserLoginFailed.png)

- ```Try again but now with the new password.```\
  ![img.png](images/authentication/loginRootUserNewPassword.png)\
  Which gives a HttpStatus ```200 - OK```.\
  ![200 OK response](images/authentication/responseRegisterNewUser.png)

- ```Delete the user abli with Authorization header containing the JWT from the root user.```\
  ![img.png](images/authentication/deleteUserSuccess.png)\
  Which gives a HttpStatus ```200 - OK```.\
  ![200 OK response](images/authentication/responseRegisterNewUser.png)


**Notifications**:
- ```Add a new notification as a root user.```\
  ![img.png](images/notifications/AddNotificationSuccess.png)\
  Which gives a HttpStatus ```200 - OK```.\
  ![200 OK response](images/authentication/responseRegisterNewUser.png)\
  Repeat the request 3 times.

- ```Now login again with the root user to see the notificiations.```\
  ![img.png](images/notifications/loginRootUserWithNotifications.png)

- ```Change the message from notification.```\
  ![img.png](images/notifications/changeNotificationsMessage.png)\
  Which gives a HttpStatus ```200 - OK```.\
  ![200 OK response](images/authentication/responseRegisterNewUser.png)

- ```Get all the notifications from the root user.```\
  ![img.png](images/notifications/getAllNotificationsFromUser.png)\
  Which gives a HttpStatus ```200 - OK```.\
  ![200 OK response](images/authentication/responseRegisterNewUser.png)

- ```Delete all notifications from the root user as the root user.```\
  ![img.png](images/notifications/deleteNotificationFromUserSuccess.png)\
  Which gives a HttpStatus ```200 - OK```.

- ```Now login again with the root user to see the notificiations.```\
  ![img.png](images/notifications/noNewNotificationsRootUser.png)\
  Which gives a HttpStatus ```200 - OK``` and also displays a message "No new notifications".


## Testing naming convention
We decided to use the following naming convention for the tests in the authentication microservice:
```<methodWeWantToTest><ConditionWeWantTest>Test()```

## Other notes
We have a test class to test our ```AuthenticationMain.java``` class which is named ```AuthenticationMainTest.java```. 
This class is there solely to cover the main Authentication class during testing.