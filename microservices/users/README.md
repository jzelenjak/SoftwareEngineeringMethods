# Users
<!Insert here a short description what your microservice is for and what the different users can do.>
Users microservice is necessary to perform CRUD operations on users of our application.
Namely, we can _register a user_ (can be done by anyone; for security reasons everyone is initially registered as a student), _query them by user ID, username and role_ (can only be done by lecturers and admins), _change the role of a user_ (can only be done by admins) and _delete a user_ (can only be done by admins).
We check the permissions with the help of JWT tokens in every request the user makes (when it is required).

In addition, in users microservice _a **unique** user ID_ is generated by the database, which is used by other microservices as the identifier for a user.

Finally, for _register_, _change role_ and _delete_ endpoints, this microservice makes a (asynchronous) request to the authentication microservice to synchronise the changes. We have some mechanisms to make those operations atomic (either in both microservices, or in neither) in case of an error response code from the authentication microservice; however, these mechanisms might fail in case of the network failure.

## File description
<!Insert here a short description of the purpose of each file in your microservice.>
**Controllers**:
- ```UserController.java```: Takes care of all the users API, following the ```<host><port>/api/users/<request>``` path. Such as _registering, fetching user by username, user ID and role, changing the role, and deleting user_ endpoints.

**Entities**:
- ```User.java```: which is a class to store the data in a User object (database-generated _user ID_, unique _username_, not blank and not null _first and last name_, _role_).
- ```UserRole.java```: which is an enum to store the available user roles: _STUDENT_, _LECTURER_ and _ADMIN_.

**Repositories**:
- ```UserRepository.java```: which is the repository for storing the user data (User entities).

**Service**:
- ```UserService.java```: which is a class that represents a service which communicates with the database containing the user data.

**Config**:
- ```GatewayConfig.java```: which is a class that is used to read the gateway host and port from the properties file.

```UsersMain.java```: the main class of our users microservice.