# Akka project
This application is a REST server built on Scala.
## Requirements
* Java 8 installed
* SBT installed
* A PostgreSQL database called 'akka-api-rest'
## Usage
To compile the application, run the command bellow:
```sbtshell
sbt compile
```

To run the application, execute the following command:
```sbtshell
sbt run
```

To run the tests, the fork has to be enabled for them, if not, they can be executed from any IDE.

The API contract is defined in the postman collection file located in the root of the project ("api_contract.postman_collection").
## Functionality
* **Authentication**
  * Sign up
  * Login
  * Logout
* **Account**
  * Get info
  * Modify account data
  * Modify password
  * Modify email
  * Delete account
* **User administration** (only accessible for admin user)
  * Get user list
  * Get user
  * Modify user data
  * Delete user
* **Tasks**
  * Get tasks
  * Get task
  * Create task
  * Modify task
  * Delete task
  
Each user has a role, that can be USER or ADMIN. If the user has the ADMIN role, he can access protected endpoints like user administration, if not, the application returns a forbidden error message.

For authentication, the application uses JWT in order to handle the user's session in a stateless way.
The JWT contains the ID and the role of the user.

## Technologies
* Akka HTTP for the REST client
* Akka actors which serve as services interacting with the repositories
* Slick repositories to access the database
* PostgreSQL as database management system
* Flyway to create the tables and populate the database with the administrator user

## Architecture
* **core**: contains abstract classes and utility models
   * Generic models used in all the layers
   * Parent classes for the different layers:
      * BaseResource: abstract resource with helper methods and security validations
      * BaseEntity, BaseRepository and BaseTable: abstract classes using the repository pattern, in order to create a generic repository with the basic CRUD operations
      * BaseService: generic actor
* **client**: contains the routes, resources and models which are exchanged with the client
* **config**: contains the configuration about the database, security and flyway
* **domain**: contains the model exchanged between the client layer and the infrastructure layer
* **infrastructure**: contains the akka actors (services) and the different repositories together with the DAOs
 
#### Application made by Sergio Banegas for learning purposes
