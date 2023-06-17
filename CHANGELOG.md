## 1.6.5

#### 1.7.1

- fix field name (`visibleChunkMap` -> `field_219252_f`)

#### 1.7.0

- add block entity inspection command
    - `block_entity summary`
    - `block_entity locations <registry_name>`

#### 1.6.2

- optimize file transfer size (gzip)

#### 1.6.1

- fix bug (loaded a client class in server)

#### 1.6.0

- add `entity summary <classname/name/registry_name>` command
- add `entity locations <classname/name/registry_name> <name>` command

#### 1.5.1

- recipe conversion now transform one by one, skipping failed ones.
- add printj command (`/the-toolkits recipe <ns> <path> printj <recipeId>`) to try to convert for a single recipe
- add search command (`/the-toolkits search <recipeId>`) to search for the serializer
