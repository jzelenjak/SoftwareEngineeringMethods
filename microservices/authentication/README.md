# Authentication
<!Insert here a short description what your microservice is for and what the different users can do.>
Authentication is necessary to identify, authorize and authenticate the users using our application. We use this with the help of JWT tokens and send these with every request the user makes (to the different microservices).

## File description
<!Insert here a short description of the purpose of each file in your microservice.>
**Controllers**:
- ```AuthController.java```: Takes care of all the authentication API, following the ```<host><port>/api/auth/<request>``` path. Such as _login, register, changing password, changing role, deleting user_ endpoints.
- ```NotificationController.java```: Takes care of all the notification API, following the ```<host><port>/api/auth/notifications/<request>``` path. Such as _adding new notifications, changing user/message from notification, deleting notifications by id/user_ endpoints.

**Entities**:
- ```Notification.java```: which is a class to store notification data in a Notification object.
- ```UserData.java```: which is a class to store user data in a UserData object.

**JWT related classes**:
- ```JwtConfig.java```: which is a class to set up the JWT token configuration we use to identify, authorize and authenticate our users.
- ```JwtSecretKey.java```: which is a class to set up the configuration for the JWT secret key.
- ```JwtTokenFilter.java```: which is a class that acts as a filter for validating JWT tokens.
- ```JwtTokenProvider.java```: which is a class that provides utilities related to JWT token. It uses the JWT library as well. But most importantly it takes care of the creation of the JWT tokens and returning the Authentication object we use for filtering (in the ```JwtTokenFilter.java```).

**Repositories**:
- ```NotificationDataRepository.java```: which is the repository for storing the notification data (Notification entities).
- ```UserDataRepository.java```: which is the repository for storing the user data (UserData entities).

**Security related classes**:
- ```AuthSecurityConfig.java```: which takes care of the security configuration used by Spring. 
- ```PasswordConfig.java```: which takes care of the configuration for the password encoder.
- ```UserRole.java```: which is an enum class of all the different types of users we can have. _E.g. ADMIN / LECTURER / TA / STUDENT_

**Service**:
- ```AuthService.java```: which is a class that represents a service which communicates with the database containing the user data.
- ```NotificationService.java```: which is a class that represents a service which communicates with the database containing the notification data.

```AuthenticationMain.java```: the main class of our authentication microservice.