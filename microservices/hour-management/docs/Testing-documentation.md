# Hour Management

This document contains the following testing-related topics:

1. [Postman examples (manual testing)](#postman-examples-manual-testing)
2. [Integration testing](#integration-testing)
3. [Testing naming conventions](#testing-naming-conventions)

## Postman examples (manual testing)

The following subsections contain manual tests for the Hour Management microservice. These tests can also be used as an example of how to use the service itself.

Almost all tests use randomized global variables which look as follows: `{{variable_name}}`. These variables are replaced with the actual values when the request is sent.

### Declare hours

1. **Let a TA declare hours for the course they TA**\
   ...
   ```json
   
   ```

### Fetch declarations

...

### Approve/reject declarations

...

### Statistics

...

## Integration testing

Except for the authentication library and remote microservice endpoints, no components have been mocked. All tested components interact with an in-memory H2 database (only during testing).

## Testing naming conventions
We decided to use the following naming convention for the tests in the hour-management microservice:
```test<MethodWeWantToTest><ConditionWeWantTest>()```\
And for the classes it is in the format: ```<ClassWeWantToTest>Test.java```.

Within the test methods we use the AAA (Arrange - Act - Assert) convention, and they are clearly separated by empty lines.

