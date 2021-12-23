# Users
## Postman examples (manual testing)
<!Post here some (no pun intended) Postman examples for your microservice and what to expect>
- *Register user*:
<!To be put...>

- *Get user by username*:
<!To be put...>

- *Get user by user ID*:
<!To be put...>

- *Get user by role*:
<!To be put...>

- *Change role*:
<!To be put...>

- *Delete user*:
<!To be put...>


## Testing naming convention
We decided to use the following naming convention for the tests in the users microservice:
```test<MethodWeWantToTest><ConditionWeWantTest>()```\
And for the classes it is in the format: ```<ClassWeWantToTest>Test.java```.

## Other notes
We have a test class to test our ```UsersMain.java``` class which is named ```UsersMainTest.java```.
This class is there solely to cover the main Users class during testing.