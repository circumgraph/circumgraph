# Circumgraph

Schema based object database with GraphQL-support and advanced search. 

* **Object storage** configured via a GraphQL schema
* Auto-generated **GraphQL API**
* **Polymorphic storage**, interfaces and unions are fully supported
* **Querying and full text search**, configure indexing via directives
* **Docker friendly**, config via environment, schema can be embedded in custom image

## Status

⚠️ This branch contains an early version that is in development. 

The code is provided as a proof of concept and isn't ready for production use 
yet. For testing Docker images of the main branch are published automatically
to Github Packages.

Missing features:

* Multi-lingual querying
* Schema evolution
* Live backups
* Support for authentication via JWT
* Support for high availability

## Configuration

Circumgraph is configured via a GraphQL schema that is used to define the
types available to store and how they can be queried.

```graphql
type Book implements Entity {
  title: String! @index(type: TYPE_AHEAD) @sortable

  authors: [Author!]! @index

  published: LocalDate @index @sortable

  isbn: String @index(type: TOKEN)
  isbn13: String @index(type: TOKEN)

  pages: Int! @index
}

type Author implements Entity {
  name: String! @index(type: TYPE_AHEAD) @sortable

  books: [Book] @relation(field: authors)
}
```

## GraphQL API

The schema is used to generate a GraphQL API that can be used to mutate and
query objects.

### Store an object

```graphql
mutation {
  storeBook(
    mutation: {
      title: "A Short History of Nearly Everything",
      authors: {
        set: [ "authorIdHere" ]
      },
      published: "2004-09-14",
      isbn: "076790818X",
      isbn13: "9780767908184",
      pages: 544
    }
  ) {
    id
  }
}
```

### Delete an object

```graphql
mutation {
  deleteBook(id: "idOfBook") {
    success
  }
}
```

### Get single object

```graphql
query {
  book {
    get(id: "ffff") {
      id
      title
    }
  }
}
```

### Querying

Get the objects directly:

```graphql
query {
  book {
    search(
      criteria: [
        { field: { title: { match: "history" } } }
      ],
      sort: [
        { field: TITLE, ascending: true }
      ]
    ) {
      nodes {
        id
        title
      }
    }
  }
}
```

Get objects with extras such as score:

```graphql
query {
  book {
    search(
      criteria: [
        { field: { title: { match: "history" } } }
      ]
    ) {
      edges {
        score

        node {
          id
          title
        }
      }
    }
  }
}
```

Pagination is supported via cursors:

```graphql
query($cursor: String) {
  book {
    search(
      first: 10,
      after: $cursor,
      criteria: [
        { field: { title: { match: "history" } } }
      ]
    ) {
      pageInfo {
        hasNextPage
        endCursor
      }

      edges {
        cursor

        node {
          id
          title
        }
      }
    }
  }
}
```

## Polymorphism

Objects in Circumgraph can resolve to different types, this allows for modeling
of some advanced use cases, such as a page tree:

```graphql
interface Page implements Entity {
  title: String! @index(type: TYPE_AHEAD)

  parent: Page @index

  children: [Page] @relation(field: parent)
}

type HomePage implements Page {
  intro: String!
}

type ProductsPage implements Page {
}

type BlogPost implements Page {
  body: String!
}
```

## Running via Docker

For experimenting the easiest way is probably to use Docker Compose:

```yaml
version: "3.9"

services:
  cicrcumgraph:
    image: ghcr.io/circumgraph/circumgraph:latest
    ports:
      - 8080:8080
    volumes:
      - ./config:/config
      - ./data:/data
```

This will look load `.gql` files in the local directory `config` and store data
in `data`.

## Embedding config in Docker image

The Docker image will look for configuration in the `/config` directory. A good
way to distribute and deploy a certain schema is to create a custom Docker image
with the config embedded:

```dockerfile
FROM ghcr.io/circumgraph/circumgraph:latest

# Copy configuration to the /config directory
COPY ./config /config
```
