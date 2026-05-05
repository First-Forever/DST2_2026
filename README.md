# Precision Medicine Matching System

Java Servlet/JSP web application for pharmacogenomic knowledge browsing, ANNOVAR sample matching, user permission management, and DeepSeek-backed assistant Q&A.

## Features

- User registration and login.
- Role-based access for normal users, professional users, and administrators.
- Knowledge-base pages for drugs, drug labels, dosing guidelines, and professional drug information.
- Keyword and filter-based biomedical record search.
- ANNOVAR output upload and genomic sample matching.
- DeepSeek AI assistant available from the `/assistant` page.
- Administrator user-management page for approving elevated permissions.

## Tech Stack

- Java 11
- Maven WAR project
- Servlet API 4.0
- JSP / JSTL
- MySQL
- Gson
- Bootstrap 4
- jQuery

## Project Structure

```text
src/main/java/cn/edu/zju
  bean/          Domain objects
  cmd/           Data import command
  controller/    Request handlers registered by DispatchServlet
  crawler/       Data collection helpers
  dao/           JDBC data access classes
  dbutils/       Database connection helper
  servlet/       Dispatcher and view access filter
  service/       External service clients, including DeepSeek

src/main/resources/app.properties
src/main/sql/dbconfig.sql
src/main/sql/schema.sql
src/main/webapp/views
src/main/webapp/static
```

## Requirements

- JDK 11 or newer
- Maven 3
- MySQL 8 or compatible MySQL server
- Tomcat 9 or another servlet container that supports `javax.servlet`

## Configuration

Application settings are in:

```text
src/main/resources/app.properties
```

Default database settings:

```properties
jdbc.url=jdbc:mysql://127.0.0.1:3306/biomed?serverTimezone=GMT%2B8
jdbc.username=biomed
jdbc.password=biomed
```

DeepSeek settings:

```properties
deepseek.apiKey=
deepseek.baseUrl=https://api.deepseek.com
deepseek.model=deepseek-chat
deepseek.temperature=0.2
deepseek.maxTokens=800
```

Prefer setting the API key through an environment variable instead of committing it to source control:

```bash
DEEPSEEK_API_KEY=your_api_key_here
```

If running through IntelliJ or Tomcat, add `DEEPSEEK_API_KEY` to the run configuration environment variables and restart the server.

## Database Setup

Create the database and database user:

```bash
mysql -u root -p < src/main/sql/dbconfig.sql
```

Create tables:

```bash
mysql -u root -p < src/main/sql/schema.sql
```

Adjust `app.properties` if your local database account or password is different.

## Import Data

The importer reads these data files from the project root:

```text
drugs.data
drugLabels.data
dosingGuidelines.data
drugProfessionalInfo.data
```

Run `cn.edu.zju.cmd.PharmGKBImporter` from the IDE after the database schema is ready.

Supported import arguments:

```text
drugs
labels
guidelines
professional-info
```

With no arguments, the importer loads all supported datasets.

## Build

Run tests:

```bash
mvn test
```

Build the WAR package:

```bash
mvn package
```

The WAR is generated at:

```text
target/haining_biomed.war
```

Deploy the WAR to Tomcat 9 or another compatible servlet container.

## Main Routes

```text
/                         Login page
/register                 Registration page
/dashboard                Dashboard
/matchingIndex            ANNOVAR upload page
/samples                  Uploaded samples
/matching                 Matching result page
/drugs                    Drug knowledge base
/drugLabels               Drug labels
/dosingGuideline          Dosing guidelines
/drugProfessionalInfo     Professional drug information
/assistant                DeepSeek AI assistant
/admin/users              User management
```

## Permissions

- `NORMAL_USER`: general system access.
- `PROFESSIONAL_USER`: access to professional drug information after approval.
- `ADMIN`: user management and permission approval.

Professional users with a ZJU email are approved automatically. Administrators must use a ZJU email. The first administrator account is approved automatically.

## Notes

This project is intended for biomedical informatics learning, research, and decision-support workflows. It should not replace professional medical diagnosis, treatment, or prescribing decisions.

Do not commit real database passwords or DeepSeek API keys to Git.
