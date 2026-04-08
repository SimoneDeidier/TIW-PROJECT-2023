# TIW Project 2023

[![Java](https://img.shields.io/badge/Java-8%2B-ED8B00?logo=openjdk&logoColor=white)](https://www.java.com/)
[![Apache Tomcat](https://img.shields.io/badge/Apache%20Tomcat-Servlet%20Container-F8DC75?logo=apachetomcat&logoColor=black)](https://tomcat.apache.org/)
[![MySQL](https://img.shields.io/badge/MySQL-Database-4479A1?logo=mysql&logoColor=white)](https://www.mysql.com/)
[![HTML5](https://img.shields.io/badge/HTML5-Pure%20HTML-E34F26?logo=html5&logoColor=white)](https://developer.mozilla.org/en-US/docs/Web/HTML)
[![JavaScript](https://img.shields.io/badge/JavaScript-RIA-F7DF1E?logo=javascript&logoColor=black)](https://developer.mozilla.org/en-US/docs/Web/JavaScript)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)
[![Course](https://img.shields.io/badge/Course-Tecnologie%20Informatiche%20per%20il%20Web-2E86C1)](project-documents/tex-sources/Documentazione%20-%20De%20Ciechi%20-%20Deidier%20-%20Gruppo%209.tex)

Final project for the university course *Tecnologie Informatiche per il Web*.

This repository contains two implementations of the same specification:

- a traditional multi-page application built with **pure HTML** and server-side rendering;
- a **Rich Internet Application (RIA)** version that keeps the page reactive with asynchronous client-server interactions.

The project was developed by a two-person team under Professor Piero Fraternali.

## Quick Preview

| Pure HTML | RIA |
| --- | --- |
| ![Pure HTML IFML home page](project-documents/IFML/IFML%20-%20HOME%20-%20pure-html.jpeg) | ![RIA IFML home page](project-documents/IFML/IFML%20-%20HOME%20-%20RIA.jpeg) |

The documentation also includes the login flow used by both versions:

![Login flow](project-documents/IFML/IFML%20-%20login.jpeg)

## Overview

The application models a hierarchical taxonomy used to classify satellite images. After authentication, users can manage a shared tree of categories that is visible to everyone.

The supported operations are:

- create a new category under a chosen parent;
- copy an entire subtree to another position in the taxonomy;
- log in, register, and log out;
- in the RIA version, rename a category inline without reloading the page.

The taxonomy has a business constraint of at most 9 children per category, with children numbered from 1 to 9.

The repository includes the source code for both implementations as well as the generated Java bytecode under the `build/` directories.

## Project Versions

### Pure HTML

The pure HTML application follows a classic request/response approach. Every interaction triggers a full page refresh and the server returns the updated view.

Main characteristics:

- form-based login and category management;
- server-side rendering of the home page;
- copy workflow driven by standard navigation and form submissions;
- servlet filters used to protect authenticated routes and prevent browser caching.

### RIA

The RIA version keeps the application on a single page after login and updates the interface through asynchronous requests.

Main characteristics:

- single-page home view;
- asynchronous category loading and updates;
- drag and drop copy workflow with confirmation dialog;
- inline category renaming;
- client-side DOM updates supported by JavaScript utilities.

## Technologies

- Java Servlets
- JDBC
- MySQL
- Apache Tomcat
- HTML5 and CSS3
- Vanilla JavaScript for the RIA frontend

## Highlights

- Same functional specification implemented with two different web architectures.
- Shared hierarchical taxonomy stored in a self-referencing MySQL table.
- Secure access through session checks and anti-caching filters.
- Rich client-side interaction in the RIA version with drag and drop and inline editing.
- Complete project documentation with ER, IFML, and sequence diagrams.

## Repository Structure

```
TIW-PROJECT-2023/
├── database/
│   └── database.sql                  # Database schema and seed data
├── project-documents/
│   ├── diagramma-ER/                 # ER diagram
│   ├── IFML/                         # IFML diagrams
│   ├── sequence-diagrams/            # Sequence diagrams for both versions
│   ├── tex-sources/                  # Full project documentation source
│   └── Documentazione - De Ciechi - Deidier - Gruppo 9.pdf  # Rendered documentation
├── pure-html/                        # Pure HTML version
│   ├── src/main/java/                # Beans, controllers, DAOs, filters, exceptions
│   └── src/main/webapp/              # HTML pages, CSS, web.xml
└── RIA/                              # Rich Internet Application version
    ├── src/main/java/                # Beans, controllers, DAOs, filters, exceptions
    └── src/main/webapp/              # HTML, JS resources, CSS, web.xml
```

## Database

The database is defined in [database/database.sql](database/database.sql) and contains two tables:

- `user` for authentication;
- `category` for the hierarchical taxonomy.

The `category` table is self-referencing through `parentID`, which allows the tree structure to be stored directly in MySQL.

Seed users included in the dump:

- `root` / `password`
- `user` / `password`

## Configuration

Both applications use the same MySQL database and read the connection settings from `WEB-INF/web.xml`.

Before deploying, update the following parameters to match your local environment:

- `dbUrl`
- `dbUser`
- `dbPassword`
- `dbDriver`

The default configuration in the repository points to a local MySQL instance and uses the seed credentials from the SQL dump.

## How to Run

1. Create the MySQL schema by executing [database/database.sql](database/database.sql).
2. Check the `web.xml` file of the selected version and adjust the database parameters if needed.
3. Deploy the `pure-html` or `RIA` web application on Apache Tomcat.
4. Open the context path assigned by Tomcat, then log in with one of the seed accounts.

There is no Maven or Gradle build descriptor in the repository, so the project is intended to be imported and deployed as a standard Java web application from an IDE or servlet container.

Suggested test accounts:

- `root` / `password`
- `user` / `password`

## Documentation

The repository includes the original project documentation and design artifacts in [project-documents](project-documents).

Useful references:

- [Full LaTeX documentation source](project-documents/tex-sources/Documentazione%20-%20De%20Ciechi%20-%20Deidier%20-%20Gruppo%209.tex)
- [Rendered PDF documentation](project-documents/Documentazione%20-%20De%20Ciechi%20-%20Deidier%20-%20Gruppo%209.pdf)
- [Entity-Relationship diagram](project-documents/diagramma-ER/diagramma-ER.drawio)
- [Pure HTML sequence diagrams](project-documents/sequence-diagrams/pure-html)
- [RIA sequence diagrams](project-documents/sequence-diagrams/RIA)

## Authors

- [De Ciechi Samuele](https://github.com/Samdec01)
- [Deidier Simone](https://github.com/SimoneDeidier)

## License

This project is released under the [GNU GPL v3.0](LICENSE).
