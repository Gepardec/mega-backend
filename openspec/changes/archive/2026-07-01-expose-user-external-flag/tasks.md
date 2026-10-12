## 1. Domain

- [x] 1.1 Add `isExternal()` method to `ZepUsername` returning `value.startsWith("e")`
- [x] 1.2 Add `isExternal()` method to `User` delegating to `zepUsername.isExternal()`

## 2. OpenAPI Schema

- [x] 2.1 Add required `isExternal: boolean` field to the `User` schema in `schemas/user.yaml`

## 3. REST Adapter

- [x] 3.1 Add `@Mapping(target = "isExternal", source = "external")` to `UserRestMapper.toUserDto()`

## 4. Tests

- [x] 4.1 Unit test `ZepUsername.isExternal()`: username starting with "e" returns true, otherwise false
- [x] 4.2 Unit test `User.isExternal()`: delegates correctly to `ZepUsername`
- [x] 4.3 REST integration test for `GET /users/me`: response includes `isExternal` with the correct value
