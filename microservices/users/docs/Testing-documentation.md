# Users

## Postman examples
<!Post here some (no pun intended) Postman examples for your microservice and what to expect>

**NB! Tokens have certain validity time. Thus, periodically you need to log in again to get a fresh one.**

### Register user

1. **Register a new user with username jzelenjak and blank first name.**\
   ![register new user with blank first name](images/registerUser/BlankFirstName/registerUserWithBlankFirstNameRequest.jpg)\
   Which gives a HttpStatus ```409 - CONFLICT```.\
   ![409 CONFLICT response](images/registerUser/BlankFirstName/registerUserWithBlankFirstNameResponse.jpg)

2. **Register the same new user with username jzelenjak and empty username.**\
   ![register new user with empty username](images/registerUser/EmptyUsername/registerUserWithEmptyUsernameRequest.jpg)\
   Which gives a HttpStatus ```409 - CONFLICT```.\
   ![409 CONFLICT response](images/registerUser/EmptyUsername/registerUserWithEmptyUsernameResponse.jpg)

3. **Register the same new user with username jzelenjak and the first and last name and password as on the image.**\
   ![register new user](images/registerUser/Success/registerUserSuccessfulRequest.jpg)\
   Which gives a HttpStatus ```200 - OK```.\
   ![200 OK response](images/registerUser/Success/registerUserSuccessfulResponseStatusCode.jpg)\
   And also gives the generated user ID for the new user.\
   ![generated user ID](images/registerUser/Success/registerUserSuccessfulResponseUserId.jpg)

4. **Register the same user with username jzelenjak as in the previous request.**
   ![register already existing user](images/registerUser/UserExists/registerUserUserExistsRequest.jpg)\
   Which gives a HttpStatus ```409 - CONFLICT```.\
   ![409 CONFLICT response](images/registerUser/UserExists/registerUserUserExistsResponse.jpg)


### Get user by username

1. **Get the user with username jzelenjak but put an invalid token into 'Authorization' header.**\
   ![get by username invalid token](images/getByUsername/InvalidToken/getByUsernameInvalidTokenRequest.jpg)\
   Which gives a HttpStatus ```403 - FORBIDDEN```.\
   ![403 FORBIDDEN response](images/getByUsername/InvalidToken/getByUsernameInvalidTokenResponse.jpg)

2. **Get a non-existing user with username jzalenjak and put a token for the root user or any admin or lecturer (as shown below).**\
   ![get by username not found](images/getByUsername/NotFound/getByUsernameNotFoundRequest.jpg)\
   Which gives a HttpStatus ```404 - NOT FOUND```.\
   ![404 NOT FOUND response](images/getByUsername/NotFound/getByUsernameNotFoundResponse.jpg)\
   To get a token for the root user, log in with Authentication Server with his credentials (as shown on the image).\
   ![log in as root user](images/getByUsername/NotFound/logInAsRoot.jpg)\
   Which gives back a token for the root user.\
   ![get back root token](images/getByUsername/NotFound/getBackRootToken.jpg)

3. **Get the user with username jzelenjak and put a token for the root user or any admin or lecturer (as shown above).**\
   ![get by username](images/getByUsername/Success/getByUsernameSuccessfulRequest.jpg)\
   Which gives a HttpStatus ```200 - OK```.\
   ![200 OK response](images/getByUsername/Success/getByUsernameSuccessfulResponseStatusCode.jpg)\
   And also gives the JSON representation of the requested user.\
   ![user JSON](images/getByUsername/Success/getByUsernameSuccessfulResponseUser.jpg)


### Get user by user ID

1. **Get the user with ID 3001001 but put do not put any token into 'Authorization' header.**\
   ![get by user ID no token](images/getByUserId/NoToken/getByUserIdNoTokenRequest.jpg)\
   Which gives a HttpStatus ```401 - UNAUTHORIZED```.\
   ![401 UNAUTHORIZED response](images/getByUserId/NoToken/getByUserIdNoTokenResponse.jpg)

2. **Get a non-existing user with user ID 3001002 and put a token for the root user or any admin or lecturer (as shown above).**\
   ![get by user ID not found](images/getByUserId/NotFound/getByUserIdNotFoundRequest.jpg)\
   Which gives a HttpStatus ```404 - NOT FOUND```.\
   ![404 NOT FOUND response](images/getByUserId/NotFound/getByUserIdNotFoundResponse.jpg)

3. **Get the user with user ID 3001001 and put a token for the root user or any admin or lecturer (as shown above).**\
   ![get by user ID](images/getByUserId/Success/getByUserIdSuccessfulRequest.jpg)\
   Which gives a HttpStatus ```200 - OK```.\
   ![200 OK response](images/getByUserId/Success/getByUserIdSuccessfulResponseStatusCode.jpg)\
   And also gives the JSON representation of the requested user.\
   ![user JSON](images/getByUserId/Success/getByUserIdSuccessfulResponseUser.jpg)


### Get users by role
0. **Register some new users (exactly as has been done above):**
- Student with username jbastenhof.\
   ![new student 1](images/getByRole/RegisterUsers/registerStudent1.jpg)\
- Student with username bxli.\
   ![new student 2](images/getByRole/RegisterUsers/registerStudent2.jpg)\
- Lecturer with username apanichella (initially student, permission upgrade happens in [Change role section](#change-role)).\
   ![new lecturer](images/getByRole/RegisterUsers/registerLecturer.jpg)

1. **Get users with role student but put a token for a student (as shown below).**\
   ![get by role forbidden](images/getByRole/ForbiddenStudent/getByRoleForbiddenStudentRequest.jpg)\
   Which gives a HttpStatus ```403 - FORBIDDEN```.\
   ![403 FORBIDDEN response](images/getByRole/ForbiddenStudent/getByRoleForbiddenStudentResponse.jpg)\
   To get a token for a student, log in with Authentication Server with their credentials (e.g. as shown on the image).\
   ![log in as student](images/getByRole/ForbiddenStudent/logInAsStudent.jpg)\
   Which gives back a token for the student.\
   ![get back student token](images/getByRole/ForbiddenStudent/getBackStudentToken.jpg)

2. **Get users with non-existing role moderator and put a token for a lecturer (as shown below) or the root user or any admin (as shown above).**\
   ![get by role invalid role](images/getByRole/InvalidRole/getByRoleInvalidRoleRequest.jpg)\
   Which gives a HttpStatus ```400 - BAD REQUEST```.\
   ![400 BAD REQUEST response](images/getByRole/InvalidRole/getByRoleInvalidRoleResponse.jpg)\
   To get a token for a lecturer, log in with Authentication Server with their credentials (e.g. as shown on the image).\
   ![log in as lecturer](images/getByRole/InvalidRole/logInAsLecturer.jpg)\
   Which gives back a token for the lecturer.\
   ![get back lecturer token](images/getByRole/InvalidRole/getBackLecturerToken.jpg)

3. **Get users with the role admin (currently there are no admins) and put a token for a lecturer or the root user or any admin (as shown above).**\
   ![get by role no users](images/getByRole/NoUsersFound/getByRoleNoUsersRequest.jpg)\
   Which gives a HttpStatus ```404 - NOT FOUND```.\
   ![400 NOT FOUND response](images/getByRole/NoUsersFound/getByRoleNoUsersResponse.jpg)\

4. **Get users with the role student and put a token for a lecturer or the root user or any admin (as shown above).**\
   ![get by role](images/getByRole/Success/getByRoleSuccessfulRequest.jpg)\
   Which gives a HttpStatus ```200 - OK```.\
   ![200 OK response](images/getByRole/Success/getByRoleSuccessfulResponseStatusCode.jpg)\
   And also gives a JSON list of the JSON representations of the requested users.\
   ![users JSON](images/getByRole/Success/getByRoleSuccessfulResponseUsers.jpg)


### Get user by first name

1. **Get users with the first name Jegor but put a token for a student (as has been shown above)**\
   ![get by first name forbidden](images/getByFirstName/ForbiddenStudent/getByFirstNameForbiddenStudentRequest.jpg)\
   Which gives a HttpStatus ```403 - FORBIDDEN```.\
   ![403 FORBIDDEN response](images/getByFirstName/ForbiddenStudent/getByFirstNameForbiddenStudentResponse.jpg)

2. **Get users with the first name Jeg (no such users) and put a token for the root user or any admin (as shown above).**\
   ![get by first name not found](images/getByFirstName/NotFound/getByFirstNameNotFoundRequest.jpg)\
   Which gives a HttpStatus ```404 - NOT FOUND```.\
   ![404 NOT FOUND response](images/getByFirstName/NotFound/getByFirstNameNotFoundResponse.jpg)

3. **Get users with the first name Jegor and put a token for the root user or any admin (as shown above).**\
   ![get by first name](images/getByFirstName/Success/getByFirstNameSuccessfulRequest.jpg)\
   Which gives a HttpStatus ```200 - OK```.\
   ![200 OK response](images/getByFirstName/Success/getByFirstNameSuccessfulResponseStatusCode.jpg)\
   And also gives a JSON list of the JSON representations of the requested users.\
   ![users JSON](images/getByFirstName/Success/getByFirstNameSuccessfulResponseUser.jpg)


### Get user by last name

1. **Get users with the last name Zelenjak but put a token for a lecturer (as has been shown above)**\
   ![get by last name forbidden](images/getByLastName/ForbiddenLecturer/getByLastNameForbiddenLecturerRequest.jpg)\
   Which gives a HttpStatus ```403 - FORBIDDEN```.\
   ![403 FORBIDDEN response](images/getByLastName/ForbiddenLecturer/getByLastNameForbiddenLecturerResponse.jpg)

2. **Get users with the last name Zelenjakius (no such users) and put a token for the root user or any admin (as shown above).**\
   ![get by last name not found](images/getByLastName/NotFound/getByLastNameNotFoundRequest.jpg)\
   Which gives a HttpStatus ```404 - NOT FOUND```.\
   ![404 NOT FOUND response](images/getByLastName/NotFound/getByLastNameNotFoundResponse.jpg)

3. **Get users with the last name Zelenjak and put a token for the root user or any admin (as shown above).**\
   ![get by last name](images/getByLastName/Success/getByLastNameSuccessfulRequest.jpg)\
   Which gives a HttpStatus ```200 - OK```.\
   ![200 OK response](images/getByLastName/Success/getByLastNameSuccessfulResponseStatusCode.jpg)\
   And also gives a JSON list of the JSON representations of the requested users.\
   ![users JSON](images/getByLastName/Success/getByLastNameSuccessfulResponseUser.jpg)


### Change first name

1. **Change first name of the user with user ID 3001001 but put a token for a lecturer (as has been shown above).**\
   ![change first name forbidden](images/changeFirstName/ForbiddenLecturer/changeFirstNameForbiddenLecturerRequest.jpg)\
   Which gives a HttpStatus ```403 - FORBIDDEN```.\
   ![403 FORBIDDEN response](images/changeFirstName/ForbiddenLecturer/changeFirstNameForbiddenLecturerResponse.jpg)

2. **Change first name of a non-existing user with user ID 3001000 and put a token for the root user or any admin (as shown above).**\
   ![change first name not found](images/changeFirstName/NotFound/changeFirstNameNotFoundRequest.jpg)\
   Which gives a HttpStatus ```404 - NOT FOUND```.\
   ![404 NOT FOUND response](images/changeFirstName/NotFound/changeFirstNameNotFoundResponse.jpg)

3. **Change first name of the user with user ID 3001001 and put a token for the root user or any admin (as shown above).**\
   ![change first name](images/changeFirstName/Success/changeFirstNameSuccessfulRequest.jpg)\
   Which gives a HttpStatus ```200 - OK```.\
   ![200 OK response](images/changeFirstName/Success/changeFirstNameSuccessfulResponse.jpg)\
   Verify the change by sending a `getUserByUserId` request (exactly as has been done above).\
   ![verify change first name](images/changeFirstName/Success/verifyChangeFirstNameRequest.jpg)\
   Which gives a HttpStatus ```200 - OK```.\
   ![200 OK response](images/changeFirstName/Success/verifyChangeFirstNameResponseStatusCode.jpg)\
   And also gives the JSON representation of the (updated) requested user.\
   ![user JSON](images/changeFirstName/Success/verifyChangeFirstNameResponseUser.jpg)


### Change last name

1. **Change last name of the user with user ID 3001001 but put a token for a student (as has been shown above).**\
   ![change last name forbidden](images/changeLastName/ForbiddenStudent/changeLastNameForbiddenStudentRequest.jpg)\
   Which gives a HttpStatus ```403 - FORBIDDEN```.\
   ![403 FORBIDDEN response](images/changeLastName/ForbiddenStudent/changeLastNameForbiddenStudentResponse.jpg)

2. **Change last name of a non-existing user with user ID 3001000 and put a token for the root user or any admin (as shown above).**\
   ![change last name not found](images/changeLastName/NotFound/changeLastNameNotFoundRequest.jpg)\
   Which gives a HttpStatus ```404 - NOT FOUND```.\
   ![404 NOT FOUND response](images/changeLastName/NotFound/changeLastNameNotFoundResponse.jpg)

3. **Change last name of the user with user ID 3001001 and put a token for the root user or any admin (as shown above).**\
   ![change first name](images/changeLastName/Success/changeLastNameSuccessfulRequest.jpg)\
   Which gives a HttpStatus ```200 - OK```.\
   ![200 OK response](images/changeLastName/Success/changeLastNameSuccessfulResponse.jpg)\
   Verify the change by sending a `getUserByUserId` request (exactly as has been done above).\
   ![verify change last name](images/changeLastName/Success/verifyChangeLastNameRequest.jpg)\
   Which gives a HttpStatus ```200 - OK```.\
   ![200 OK response](images/changeLastName/Success/verifyChangeLastNameResponseStatusCode.jpg)\
   And also gives the JSON representation of the (updated) requested user.\
   ![user JSON](images/changeLastName/Success/verifyChangeLastNameResponseUser.jpg)


### Change role

0. **Suppose we register the lecturer from [Get users by role section](#Get-users-by-role). Initially everyone is a student. We want to change the role.**\
   ![new lecturer](images/getByRole/RegisterUsers/registerLecturer.jpg)

1. **Change role of the user with user ID 3001001 but put a token for a student (as has been shown above).**\
   ![change role forbidden](images/changeRole/ForbiddenStudent/changeRoleForbiddenStudentRequest.jpg)\
   Which gives a HttpStatus ```403 - FORBIDDEN```.\
   ![403 FORBIDDEN response](images/changeRole/ForbiddenStudent/changeRoleForbiddenStudentResponse.jpg)

2. **Change role of a non-existing user with user ID 3001099 and put a token for the root user or any admin (as shown above).**\
   ![change role not found](images/changeRole/NotFound/changeRoleNotFoundRequest.jpg)\
   Which gives a HttpStatus ```404 - NOT FOUND```.\
   ![404 NOT FOUND response](images/changeRole/NotFound/changeRoleNotFoundResponse.jpg)

3. **Change role of the user with user ID 3001001 to a non-existing role king and put a token for the root user or any admin (as shown above).**\
   ![change role invalid role](images/changeRole/InvalidRole/changeRoleInvalidRoleRequest.jpg)\
   Which gives a HttpStatus ```400 - BAD REQUEST```.\
   ![400 BAD REQUEST response](images/changeRole/InvalidRole/changeRoleInvalidRoleResponse.jpg)

4. **Change role of the user with user ID 3001002 (the user ID of the lecturer registered as a student) and put a token for the root user or any admin (as shown above).**\
   ![change role](images/changeRole/Success/changeRoleSuccessfulRequest.jpg)\
   Which gives a HttpStatus ```200 - OK```.\
   ![200 OK response](images/changeRole/Success/changeRoleSuccessfulResponseStatusCode.jpg)\
   And also gives the success message.\
   ![success message](images/changeRole/Success/changeRoleSuccessfulResponseMessage.jpg)\
   We verify the change by sending a `getUserByUserId` request (exactly as has been done above).\
   ![verify change role](images/changeRole/Success/verifyChangeRoleRequest.jpg)\
   Which gives a HttpStatus ```200 - OK```.\
   ![200 OK response](images/changeRole/Success/verifyChangeRoleResponseStatusCode.jpg)\
   And also gives the JSON representation of the (updated) requested user.\
   ![user JSON](images/changeRole/Success/verifyChangeRoleResponseUser.jpg)


### Delete user by user ID

1. **Delete the user with user ID 3001001 but put a token for a student (as has been shown above).**\
   ![delete forbidden](images/deleteByUserId/ForbiddenStudent/deleteByUserIdForbiddenStudentRequest.jpg)\
   Which gives a HttpStatus ```403 - FORBIDDEN```.\
   ![403 FORBIDDEN response](images/deleteByUserId/ForbiddenStudent/deleteByUserIdForbiddenStudentResponse.jpg)

2. **Delete a non-existing user with user ID 30010042 and put a token for the root user or any admin (as shown above).**\
   ![delete not found](images/deleteByUserId/NotFound/deleteByUserIdNotFoundRequest.jpg)\
   Which gives a HttpStatus ```404 - NOT FOUND```.\
   ![404 NOT FOUND response](images/deleteByUserId/NotFound/deleteByUserIdNotFoundResponse.jpg)

3. **Delete the user with user ID 3001001 and put a token for the root user or any admin (as shown above).**\
   ![delete](images/deleteByUserId/Success/deleteByUserIdSuccessfulRequest.jpg)\
   Which gives a HttpStatus ```200 - OK```.\
   ![200 OK response](images/deleteByUserId/Success/deleteByUserIdSuccessfulResponseStatusCode.jpg)\
   And also gives the success message.\
   ![success message](images/deleteByUserId/Success/deleteByUserIdSuccessfulResponseMessage.jpg)\
   We verify the change by sending a `getUserByUserId` request (exactly as has been done above).\
   ![verify delete](images/deleteByUserId/Success/verifyDeleteByUserIdRequest.jpg)\
   Which gives a HttpStatus ```200 - OK```.\
   ![200 OK response](images/deleteByUserId/Success/verifyDeleteByUserIdResponse.jpg)


## Testing naming convention

We decided to use the following naming convention for the tests in the users microservice:
```test<MethodWeWantToTest><ConditionWeWantTest>()```\
And for the classes it is in the format: ```<ClassWeWantToTest>Test.java```.

## Integration testing

We decided to use H2 database for testing. Thus, UserService communicates with H2 repository in UserServiceTest and UserControllerTest.

Furthermore, we decided to use mockWebServer to test communication with other microservices.

## Other notes

We have a test class to test our ```UsersMain.java``` class which is named ```UsersMainTest.java```.
This class is there solely to cover the main Users class during testing.