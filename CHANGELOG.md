## [0.5.1](https://github.com/circumgraph/circumgraph/compare/v0.5.0...v0.5.1) (2021-09-05)


### Bug Fixes

* update dependency io.vertx:vertx-web-graphql to v4.1.3 ([34b67a6](https://github.com/circumgraph/circumgraph/commit/34b67a6aff2fc3610108a836efa6aca61fa71222))

# [0.5.0](https://github.com/circumgraph/circumgraph/compare/v0.4.0...v0.5.0) (2021-09-04)


### Features

* **graphql:** Add support for Duration scalar ([12e186e](https://github.com/circumgraph/circumgraph/commit/12e186ec5e442e3387555d1e67994f74f53b6427)), closes [#4](https://github.com/circumgraph/circumgraph/issues/4)
* **graphql:** Add support for LocalDate scalar ([bc989e1](https://github.com/circumgraph/circumgraph/commit/bc989e1762e7cb8b01a86d899db3061bd273e605)), closes [#4](https://github.com/circumgraph/circumgraph/issues/4)
* **graphql:** Add support for LocalDateTime scalar ([3432c82](https://github.com/circumgraph/circumgraph/commit/3432c823e1384f0f92cec300a2af7c446f695c91)), closes [#4](https://github.com/circumgraph/circumgraph/issues/4)
* **graphql:** Add support for LocalTime scalar ([90c9795](https://github.com/circumgraph/circumgraph/commit/90c9795e67fd4d32f5773fd10bbcea35775c4315)), closes [#4](https://github.com/circumgraph/circumgraph/issues/4)
* **graphql:** Add support for OffsetDateTime scalar ([da1d537](https://github.com/circumgraph/circumgraph/commit/da1d537adb7c235649f3a5beaceff4b9b9fb9986)), closes [#4](https://github.com/circumgraph/circumgraph/issues/4)
* **graphql:** Add support for OffsetTime scalar ([3234f82](https://github.com/circumgraph/circumgraph/commit/3234f82ff3195824b57d435c00f34b29d2aadd44)), closes [#4](https://github.com/circumgraph/circumgraph/issues/4)
* **graphql:** Add support for ZonedDateTime scalar ([59bcd8d](https://github.com/circumgraph/circumgraph/commit/59bcd8d213366f5c010321bd05116b38f002b7dc)), closes [#4](https://github.com/circumgraph/circumgraph/issues/4)

# [0.4.0](https://github.com/circumgraph/circumgraph/compare/v0.3.0...v0.4.0) (2021-08-31)


### Features

* **storage:** Support for types that reference themselves ([aa1d0a8](https://github.com/circumgraph/circumgraph/commit/aa1d0a86a333085c1f824f8be06ccf931d5cd39e))

# [0.3.0](https://github.com/circumgraph/circumgraph/compare/v0.2.0...v0.3.0) (2021-08-29)


### Features

* Improve error reporting with source code snippets and better formatting ([9bfacea](https://github.com/circumgraph/circumgraph/commit/9bfaceaba7d65eaf24cbb4f9e16d1480b644543b))

# [0.2.0](https://github.com/circumgraph/circumgraph/compare/v0.1.1...v0.2.0) (2021-08-23)


### Bug Fixes

* **storage:** Fix some validation errors being supressed ([d87c190](https://github.com/circumgraph/circumgraph/commit/d87c19099466c4225b0f58c47ea2336300492ed6))
* **storage:** Validate polymorphic values, such as interfaces and unions ([c431b00](https://github.com/circumgraph/circumgraph/commit/c431b00c8a3899999be59ea1a7218cb994d7eb39))


### Features

* **graphql:** Add support for searches on union values ([92fb955](https://github.com/circumgraph/circumgraph/commit/92fb955da8d91ec7679b279ac14933a8f01cdca0))
* **graphql:** Friendlier error messages on validation issues ([67a25d8](https://github.com/circumgraph/circumgraph/commit/67a25d843ed291352c862bdd94639ab15e1ce1da))
* **graphql:** Support storing and querying enums ([470667f](https://github.com/circumgraph/circumgraph/commit/470667f66eef238a93899a2f3e8451315e2d8e02))
* **storage:** Support for indexing and querying of unions ([57f3180](https://github.com/circumgraph/circumgraph/commit/57f3180434675e70421d8d1273a7ba760c531e2a))

## [0.1.1](https://github.com/circumgraph/circumgraph/compare/v0.1.0...v0.1.1) (2021-08-17)


### Bug Fixes

* update dependency io.projectreactor:reactor-bom to v2020.0.10 ([ac46ce6](https://github.com/circumgraph/circumgraph/commit/ac46ce686e76b1a5d4ea90056ba04342e8eea790))

## 0.1.0 (2021-08-16)


### Features

* **app:** Add query parsing cache ([07c7bea](https://github.com/circumgraph/circumgraph/commit/07c7beaef48f5a0afcb474f4c0bc721973054b63))
* **app:** Add server app based on Quarkus ([9cc1f17](https://github.com/circumgraph/circumgraph/commit/9cc1f177882ad5bcc102ab89481bfa64fafdfa4b))
* **app:** Development mode that will reload if the config changes ([077ef1c](https://github.com/circumgraph/circumgraph/commit/077ef1cfae43aa3ae7bb92700bd860dbdade9446))
* **graphql:** [@relation](https://github.com/relation) directive for creating links to other entities ([1459633](https://github.com/circumgraph/circumgraph/commit/14596334b5d7c1169bbe6d48815ef7103f990849))
* **graphql:** Ability to limit results when fetching relations ([872e855](https://github.com/circumgraph/circumgraph/commit/872e855edeef4fa84d2aa8ed608a5bf57932f9bd))
* **graphql:** Add initial version of public-facing GraphQL API ([fd4d84b](https://github.com/circumgraph/circumgraph/commit/fd4d84b2f1e2f0eb6ad7fa8acac1a9b9a1a22550))
* **graphql:** Add mutation for deleting object ([9d38e60](https://github.com/circumgraph/circumgraph/commit/9d38e60d84f49855ffbf4345c3ade646a8997a51))
* **graphql:** Add query support for polymorphic objects ([754107d](https://github.com/circumgraph/circumgraph/commit/754107d50c86da14de7ae68430fdbeffc88d0f43))
* **graphql:** Generate fields for sub-objects ([446caf9](https://github.com/circumgraph/circumgraph/commit/446caf943089c1dade9c64683f43e0f5efa99e82))
* **graphql:** Querying of full text and type ahead strings ([454651f](https://github.com/circumgraph/circumgraph/commit/454651fe35aca0f3ce372dbe2b5a0555b8f2e0d5))
* **graphql:** Single criteria for full text searches regardless of type ahead ability ([c6603c3](https://github.com/circumgraph/circumgraph/commit/c6603c3d32ec9b435f06ba74a575dcd3592e6bec))
* **graphql:** Support for polymorphism on entity level ([6e09ebd](https://github.com/circumgraph/circumgraph/commit/6e09ebdc9a7268dcf3d8cd231216da1833cdb1e8))
* **graphql:** Support storing and fetching lists in GraphQL API ([c5d3989](https://github.com/circumgraph/circumgraph/commit/c5d39899dd36c8d283beaf55556c564f6e7bd365))
* **graphql:** Support storing and fetching references in GraphQL API ([8777f75](https://github.com/circumgraph/circumgraph/commit/8777f757b751fc1d15bf86123a04cc292e13b9f7))
* **graphql:** Support storing and fetching unions in GraphQL API ([8dc802b](https://github.com/circumgraph/circumgraph/commit/8dc802b747d126c0d72efe97497c4f849009874f))
* **graphql:** Wrap querying and mutation in a transaction ([3083883](https://github.com/circumgraph/circumgraph/commit/3083883ed975a030300cfb15cf63a21711b69fae))
* **grapqhl:** Add ability to search entities ([4cb9079](https://github.com/circumgraph/circumgraph/commit/4cb907966f0538667a8d91d6ca7ec0fd04e35d0f))
* **model:** Ability to check implements of StructuredDef ([dac32ad](https://github.com/circumgraph/circumgraph/commit/dac32adb57e585b28a30b4e34cdf3d50695c85a9))
* **model:** Add GraphQL model represenation ([f9535e6](https://github.com/circumgraph/circumgraph/commit/f9535e6e2a34cac6188ac1995eb3f6aadbeb87ad))
* **model:** Add SimpleValueDef for EnumDef and ScalarDef ([b8c75aa](https://github.com/circumgraph/circumgraph/commit/b8c75aa6df3e39abf416e76b89afef1f3add56fa))
* **model:** Easy access to arguments of DirectiveUse ([55d3669](https://github.com/circumgraph/circumgraph/commit/55d366950361ac491e64b791437e50cf1e8538ab))
* **model:** Implement DirectiveUse ([212150c](https://github.com/circumgraph/circumgraph/commit/212150c0f5e66d16b633b0ec17357198d88cad3e))
* **model:** Improve validation and merging of types ([ddb13a2](https://github.com/circumgraph/circumgraph/commit/ddb13a251dc179623d4df9c297f5dcb12cb79e5e))
* **model:** Improve validation by checking field output type exists ([83702fa](https://github.com/circumgraph/circumgraph/commit/83702faa38037b9b6864f4842018cbd4972c382b))
* **model:** Improve validation of interface implementations ([35c1f46](https://github.com/circumgraph/circumgraph/commit/35c1f46588428ecc2fd8f0d32a3e257b86538a00))
* **model:** Validate use of directives ([43f1f48](https://github.com/circumgraph/circumgraph/commit/43f1f4884ad0dc84111b1aeae7620b51e933b404))
* **schema-graphql:** Add initial model creation from GraphQL schema ([ffb027c](https://github.com/circumgraph/circumgraph/commit/ffb027ca424762a98224a00e699847eef26effe3))
* **schema-graphql:** Handle more GraphQL schema types including directives ([7341e64](https://github.com/circumgraph/circumgraph/commit/7341e6443a35e093a94aa7a893f4dbddbdaaa4b5))
* **schema-graphql:** Support extending types ([ee433a3](https://github.com/circumgraph/circumgraph/commit/ee433a3e0f90273658770f2c59abc8773df20e62))
* **storage:** Add initial code for indexing fields ([285df6e](https://github.com/circumgraph/circumgraph/commit/285df6ee015895bdfb32db122e592b229e2b1018))
* **storage:** Add initial code to perform a search ([7f91473](https://github.com/circumgraph/circumgraph/commit/7f914738ab68f9d38058ba8a56dbe75828918e5b))
* **storage:** Add method in StructuredMutation for easily setting simple value ([e870922](https://github.com/circumgraph/circumgraph/commit/e8709220bf2ab333f8317e6685b68d7d86782a6c))
* **storage:** Add mutation support for lists ([231801d](https://github.com/circumgraph/circumgraph/commit/231801dc7b1d813b5d8d55058accfaa7786a431d))
* **storage:** Add PageCursors for simplified pagination ([a92efeb](https://github.com/circumgraph/circumgraph/commit/a92efebc0913c8bec9a53690f9993c48dced0a11))
* **storage:** Add support for indexing int and floats ([5ae321d](https://github.com/circumgraph/circumgraph/commit/5ae321d6cdab453e78f48f7d865e9879c84b4fed))
* **storage:** Add support for indirect implementations of interfaces ([d2b4996](https://github.com/circumgraph/circumgraph/commit/d2b499684e84f45fb0afcb25f3a165266bfc7d03))
* **storage:** Indexing and querying of stored object references ([2ef2530](https://github.com/circumgraph/circumgraph/commit/2ef253018b74ef38aa05910c01b787d3a7f27327))
* **storage:** Inital implementation of storage ([12c0ab8](https://github.com/circumgraph/circumgraph/commit/12c0ab8ba2c4f7f8f9a98405077df96fa923475a))
* **storage:** Introduce mutation for null values ([ddf7b37](https://github.com/circumgraph/circumgraph/commit/ddf7b37e652945000f59fc6ba1441a899287042e))
* **storage:** ListSetMutation nows uses sub-mutations ([646c06b](https://github.com/circumgraph/circumgraph/commit/646c06b7636ededec8de756dba02fcd8374fd58a))
* **storage:** Mutating, validating and storing all GraphQL types ([67647b6](https://github.com/circumgraph/circumgraph/commit/67647b61c93422b7a9e1413b00b823c6ab771eef))
* **storage:** StoredEntityValue as root of stored objects ([92dfaf6](https://github.com/circumgraph/circumgraph/commit/92dfaf6755a05189ff4f5cf0038a03978e25f658))
* **storage:** Support descending into objects when indexing fields ([775b0e7](https://github.com/circumgraph/circumgraph/commit/775b0e7e669d85bc5deb955f701803af7252c055))
* **storage:** Support for querying polymorphic objects ([bfb1563](https://github.com/circumgraph/circumgraph/commit/bfb15630a17b6c1b71d6231ef3b78e305fc58230))
* **storage:** Support for references in stored objects ([53ba7bf](https://github.com/circumgraph/circumgraph/commit/53ba7bf985c373f195c2fb6378b95cfbae14f665))
* **values:** Add value representation ([7161041](https://github.com/circumgraph/circumgraph/commit/71610414c55f581be3e29f84941bdac27ff1e079))
* **values:** Method for accessing field directly in StructuredValue ([9f9c1c4](https://github.com/circumgraph/circumgraph/commit/9f9c1c44596dd8b9c13266d8463996b447868411))
* **values:** Methods for casting value of SimpleValue ([a407113](https://github.com/circumgraph/circumgraph/commit/a4071136e57be59be7ab0962f3b32a9f9e5ed08c))


### Bug Fixes

* **graphql-schema:** Fix format of reported locations ([bee1310](https://github.com/circumgraph/circumgraph/commit/bee13109318eeb9d0dbf1648b469ea9c33ef360e))
* **graphql:** Fix NPE when specifying any in field criterias ([cabbf0a](https://github.com/circumgraph/circumgraph/commit/cabbf0a53ec5fe3f3d76b04c5a9531a1c1dd6dd4))
* **graphql:** Support StoredObjectRef in lists ([13d88aa](https://github.com/circumgraph/circumgraph/commit/13d88aa815617d9b96d9e0df94e0701b13cbfae9))
* **model:** Fix crash during preparation of structured types ([a216169](https://github.com/circumgraph/circumgraph/commit/a216169304144e88700d6f08addb33be06e7972b))
* **model:** Validation now outputs message instead of code ([35a7767](https://github.com/circumgraph/circumgraph/commit/35a7767395ee0479f6b833a1778cdf8976420cfb))
* **schema-graphql:** Report directive locations correctly ([1fd2e43](https://github.com/circumgraph/circumgraph/commit/1fd2e4357b39f8f5f89bcd5919161cdc401aefb0))
* **storage:** Correct offset resolution ([cd93a17](https://github.com/circumgraph/circumgraph/commit/cd93a17822cec5954da577b1a932923664090fb1))
* **storage:** Disallow updating of id ([689de9e](https://github.com/circumgraph/circumgraph/commit/689de9e38ff2d6a5aadd42cb607cdc953e8be8aa))
* **storage:** Emit correct field values during indexing ([1370e58](https://github.com/circumgraph/circumgraph/commit/1370e58612a5ae914e5b8a134b5d305e0355eced))
* **storage:** Fix indexing of StoredObjectRef in lists ([2232c95](https://github.com/circumgraph/circumgraph/commit/2232c95f41b9d6f226ac9f234a170f72efc6a33f))
* **storage:** Fix validation of list values that are null ([e738880](https://github.com/circumgraph/circumgraph/commit/e738880d187502e1c0d33a9607a90b4084ce69a5))
* **storage:** Handle non-null for indexing ([a5267a3](https://github.com/circumgraph/circumgraph/commit/a5267a32b4bcf2d3baa5a4641619198b6c856f7a))
* **storage:** Raise graceful error when trying to update id of entity ([3f6ba6f](https://github.com/circumgraph/circumgraph/commit/3f6ba6f6e9edf5ef848aa20b9cfd6895decb6c51))
* **storage:** Resolve correct indexer when creating index ([2ed4aa5](https://github.com/circumgraph/circumgraph/commit/2ed4aa5e6739d65182532c7e029e3290944a8cf0))
* **storage:** Support indexing null values ([06d5a81](https://github.com/circumgraph/circumgraph/commit/06d5a81a935777c2bceb3c7b9d76f89cdfd3617b))
* **storage:** Validate that interfaces have implementations if they are used by entities ([c3bb3f7](https://github.com/circumgraph/circumgraph/commit/c3bb3f71f9f659c986e9e9f4316dbcd0c32f0ff2)), closes [#1](https://github.com/circumgraph/circumgraph/issues/1)
* **values:** Correct type of value of Int scalar ([1a9ac2d](https://github.com/circumgraph/circumgraph/commit/1a9ac2d9b6dd427ecf5037c1420eb657a63008af))
