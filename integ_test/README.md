# Integration Testing

Location for tests that may pollute the environment for the rest of the tests.
Tests in this project need to be careful to ensure that they clean themselves up correctly.

These tests are run in a separate process from the unit tests which ensures that any state
these tests leave behind won't impact the state of other unit tests.
