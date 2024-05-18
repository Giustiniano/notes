# Notes

This is the backend for an application allowing users to create, read, update and delete notes.

Such notes MUST have a title and a text body, MAY be tagged as `BUSINESS`, `PERSONAL` and `IMPORTANT`
and MAY have a little endian creation date (dd/mm/yyyy)

## APIs

The endpoint root is `/api/v1/notes`

### Notes creation

to create a note, send a `POST` request to the endpoint, with the `content-type` header set as `application/json` and the following json body:

* `title` string, required
* `body`  string, required
* `tags`  string array, optional, each item must be one of `BUSINESS`, `PERSONAL` or `IMPORTANT`
* `created` date string, optional, must be formatted as dd/mm/yyyy, e.g. `15/05/2024`

upon successful completion of the request, the endpoint will return the `CREATED` status code and a json response object with the same elements plus:

* `id` string, the UUIDv4 associated to this note

#### Status codes

* `CREATED` if the note is successfully created
* `BAD REQUEST` if title or body are either missing, empty or blank, if the tags are illegal or the date is badly 
formatted

#### Example

if the body of the POST request is
```json
{
  "title": "My first note", 
  "body": "This is my first note", 
  "tags": ["PERSONAL"]
}
```

the successful response will be 
```json
{
  "id": "fa15260f-505d-4695-a786-e57ef3ee5987",
  "title": "My first note",
  "body": "This is my first note",
  "tags": ["PERSONAL"]
}
```

### Note update
to update a note, send a `PUT` request to the endpoint root with the note id that you want to update as a path variable
e.g. `/api/v1/note/fa15260f-505d-4695-a786-e57ef3ee5987`

The format for the request body and its response are the same as for the `POST` endpoint, with the following additions:
* the path variable must be a valid UUIDv4

#### Status codes
`OK` if the note update was successful
`BAD REQUEST` same cases as the POST endpoint, plus:
* if the note id path parameter is not a valid UUIDv4
`NOT FOUND` if there's no note with the supplied id

### List notes
To get the list of available notes, send a `GET` request to the endpoint root.
Pagination is supported: the page number (starting at 0) can be passed as the `page` query parameter,
the page size can be passed as the `size` query parameter.

The response will contain the title and the creation date of each note (if present), plus the pagination links and
information about pagination, such as:
* the current page
* how many pages are present
* how many elements are present

#### Filtering
Notes can be filtered by one or more tags, by passing the `tags` paramater and the
list of allowed tags, e.g `BUSINESS,IMPORTANT`

#### Sorting
The list will have the most recent notes first

#### Successful response body example

```json
{
  "_embedded" : {
    "notes" : [ {
      "id" : "818b6f5e-7db9-4834-99ab-ef5e6fd5b12d",
      "title" : "Tagged note",
      "created" : "15/05/2024"
    } ]
  },
  "_links" : {
    "first" : {
      "href" : "http://localhost:53561/api/v1/note?page=0&size=3"
    },
    "prev" : {
      "href" : "http://localhost:53561/api/v1/note?page=0&size=3"
    },
    "self" : {
      "href" : "http://localhost:53561/api/v1/note?page=1&size=3"
    },
    "last" : {
      "href" : "http://localhost:53561/api/v1/note?page=0&size=3"
    }
  },
  "page" : {
    "size" : 3,
    "totalElements" : 3,
    "totalPages" : 1,
    "number" : 1
  }
}
```
#### Status codes

`OK` if the request is successful

### Get a note body
To get a note body (the text of the note). send a `GET` request to `api/v1/notes/body/<UUID of the note>`

The response is a single JSON object, with a single `body` key containing the note body.
#### Response body example
```json
{
  "body":"Hello hello world!"
}
```

#### Status codes
* `OK` if the note exists
* `NOT_FOUND` otherwise
* `BAD_REQUEST` if the note id is not a valid UUIDv4

### Delete a note
To delete a note, send a `DELETE` request to the same endpoint used for note update

#### Status codes
`NO CONTENT` if deletion is successful
`NOT_FOUND` if there's no note with the given ID
`BAD REQUEST` if the note ID is not a valid UUIDv4

### Notes statistics
To get statistics about a note, send a `GET` request to `/api/v1/notes/<note id>/statistics`
The response will contain a single JSON object with the note statistics.
At the moment, only the word count is supported: under the key `wordCount` you will find a map where the key is word
and the value is its frequency in the note's body. Entries are sorted showing the most frequent
word first.

#### Successful response body example
Given a note whose body is `Hello hello world`, the endpoint will return
```json
{
  "wordCount": {
    "hello": 2,
    "world": 1
  }
}
```

#### Status codes
See `Get a note body`

#### Word count implementation details

As you can see from the previous example, words are treated regardless of casing, and there must be at
least one space separating one from another.
Stop words e.g. `the`, `a`, `an` are not filtered out.
There's a test verifying its behavior against Joseph Conrad's Heart of Darkness (~ 212 KB), in my tests it took
47-53 milliseconds to perform the word count.
The behaviour against non-latin alphabets has **not** been tested.

## Monitoring endpoints
This application includes some Spring actuator endpoints, namely `health`,`info`,`env`,`metrics`
under the default `actuator` endpoint (e.g `/actuator/health`).
See https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html for more
details

## Deployment
You can deploy this application via docker using the provided docker-compose.yml file.
Just run `docker-compose up -d` to start it and `docker-compose logs` to see the logs

