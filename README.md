HealthRx Hiring Test - Java Spring Boot

This is a Spring Boot application built for the HealthRx hiring challenge.

Overview

The application performs the following automated steps on startup:

Calls the generateWebhook API to retrieve a unique webhook URL and JWT access token.

Constructs a SQL query to solve the problem: "Find the highest salaried employee, per department, excluding payments made on the 1st of the month."

Submits the SQL query to the retrieved webhook URL using the JWT token for authorization.

Prerequisites

Java 17 or higher

Maven

How to Run

Clone the repository:

git clone <your-repo-url>
cd hiring-test


Build the JAR file:

mvn clean package


Run the JAR:

java -jar target/hiring-test-0.0.1-SNAPSHOT.jar


SQL Solution Logic

The SQL query uses a Common Table Expression (CTE) or Subquery to:

Filter out payments made on the 1st day of the month using DAY(PAYMENT_TIME) <> 1.

Aggregate the remaining salary per employee.

Use RANK() window function partitioned by Department to identify the top earner.

Calculate Age using TIMESTAMPDIFF.