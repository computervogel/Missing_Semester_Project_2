# Missing Semester - Group Project

### Contributors:
Maximilian Fink | k12102698

Paul Dobner-Dobenau | k12005600

Matteo Schweitzer | k12102764

### Description
This project is a JavaFX-based password manager that allows users to securely store and manage their passwords. It also features an image generation functionality using an external API. The application provides a user-friendly graphical interface and supports password storage with metadata such as website name and creation date.

### Features
- Secure Password Management: Store website credentials securely.
- JavaFX UI: A modern graphical user interface for easy interaction.
- Image Generation: Uses an external API to generate images related to stored passwords.
- Data Persistence: Stores password entries locally.
- Table View: Displays stored passwords in an interactive table format.

### Dependencies
- Java 17 or higher
- JavaFX (for UI rendering)
- Gson (for JSON handling)
- OkHttp (for API requests)

### Project Structure
```bash
Missing_Semester_Project_2/
├── src/main/java/main/
│   ├── ImageGenerator.java
│   ├── Main.java
│   ├── PasswordEntry.java
├── images/
├── lib/
├── .gitignore
├── accounts.txt
├── passwords.txt
├── pom.xml
```
#### Directory Structure Example
- `src/main/java/main/` - Contains the main application code.
- `ImageGenerator.java` - Handles API calls for generating images.
- `Main.java` - JavaFX main application class.
- `PasswordEntry.java` - Data model for storing password entries.
- `images/` - Stores generated images.
- `lib/` - Contains external libraries.
- `accounts.txt` & `passwords.txt` - Local storage for account credentials.
- `pom.xml` - Maven configuration file.
### Installation


### Usage
Start Java Application and Docker Container for Image Generation Engine

### Examples
