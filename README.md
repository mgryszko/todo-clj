# Overview

Yet another todo manager. Pet project developed for two reasons:
* practice Clojure
* implement [hexagonal architecture](http://alistair.cockburn.us/Hexagonal+architecture) in a functional language

The functionality is very simple - you can add, edit, delete a todo and list all todos.

# Usage

## CLI

```
lein bin
target/todo action [task_number] [task_description]

actions:
    add "task to be done"
    update 1 "task to be updated"
    delete 2
    list
```

## REST
```
lein ring server

http POST :3000/todos task="do something"
http GET :3000/todos
http GET :3000/todos/1
http PUT :3000/todos/1 task="maybe tomorrow"
http DELETE :3000/todos/1
```

# Structure

## Production code

* **core** - namespace `todo.core`
* **delivery mechanism**
  * **CLI** - namespaces under `todo.infrastructure.cli`
  * **REST** - namespaces under `todo.infrastructure.rest`
* **file repository adapter** - namespaces under `todo.infrastructure.file`

## Tests

* unit tests of core
* integration tests of file repository adapter
* system tests - roundtrip tests via CLI and REST. Using mainly front door (public API). Back door only for cleaning the todo file

To run all tests: `lein midje` or at REPL startup: `lein repl`

## External libraries

* [environ](https://github.com/weavejester/environ) for configuring the `todo.txt` location. and clj-stacktrace
* [Ring](https://github.com/ring-clojure/ring)
* [Compojure](https://github.com/weavejester/compojure)
* [Liberator](http://clojure-liberator.github.io/liberator/)

