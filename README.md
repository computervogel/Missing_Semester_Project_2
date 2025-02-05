# Missing Semester - Group Project

### Contributors:
Maximilian Fink | k12102698

Paul Dobner-Dobenau | k12005600

Matteo Schweitzer | k12102764

### Description

This project is a **JavaFX-based password manager**, allowing users to store and manage their passwords. 
Additionally, it features an **image generation functionality** that creates images based on the passwords to help you remember them better.
This is partially inspired by the [*Password strength xkcd*](https://xkcd.com/936/).
The application provides a user-friendly graphical interface and supports password storage with metadata such as website name and creation date.

**⚠ Important Note:** This password manager **is not actually secure**. The password and user files are stored in plain text without encryption. This project is **a proof of concept** rather than a production-ready security tool.

### Features
- **Basic Password Management**: Store website credentials (⚠ not securely).
- **JavaFX UI**: A graphical user interface for easy interaction.
- **Image Generation**: Uses an external API to generate images related to stored passwords.
- **Data Persistence**: Stores password entries in local text files.
- **Table View**: Displays stored passwords in an interactive table format.

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


---

### Image Generation API
For image generation, we utilize **[LocalAI](https://localai.io/)** running in a **Docker container**.
For our tests we used a **[Llama 3 8B](https://huggingface.co/NousResearch/Hermes-2-Pro-Llama-3-8B-GGUF)** LLM Variant to generate prompts,
based on the given password, which are then passed to a **[Stable Diffusion](https://huggingface.co/Lykon/DreamShaper)** Variant to create images.

LocalAI follows the **OpenAI API standard**, meaning that the API calls are compatible with OpenAI services (ChatGPT & DALL·E).
However, **to reduce costs, we opted for a local AI solution instead of OpenAI’s paid API**.

### Setting Up LocalAI

To set up the LocalAI Docker Container follow the instructions on their website or Github page:

- https://github.com/mudler/LocalAI or
- https://localai.io/

We used the standard docker commands provided in the instructions: 

```
docker run -p 8080:8080 --name local-ai -ti localai/localai:latest-aio-cpu
# Do you have a Nvidia GPUs? Use this instead
# CUDA 11
# docker run -p 8080:8080 --gpus all --name local-ai -ti localai/localai:latest-aio-gpu-nvidia-cuda-11
# CUDA 12
# docker run -p 8080:8080 --gpus all --name local-ai -ti localai/localai:latest-aio-gpu-nvidia-cuda-12
```

When first starting the container the first time it will take some time to download all the model files.

When using the CPU version and depending on the hardware, the API calls might run into a timeout. 
The Timeout can be increased in the ImageGenerator class for both the LLM call and Image Generator call.

---
### Usage

1.  Start Java Application and LocalAI Docker Container on localhost port 8080 for Image Generation.
2. Create a user account and login
3. Add websites and passwords
4. Use generated images to better remember passwords


### Examples

There is already a user **admin** with password **admin** that has a few example entries.

Here a demo video:

https://github.com/user-attachments/assets/f02572b0-b68e-4a9d-a92f-899a2fb445b7

---

## Future Improvements
- Encrypt stored passwords to improve security.  
- Implement **a proper authentication system**.  
- Allow users to choose between API for image generation.

---
