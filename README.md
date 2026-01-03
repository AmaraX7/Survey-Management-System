# Survey Management System
Advanced survey platform with intelligent clustering algorithms for data analysis.

##  Features
- K-Means, K-Means++, and K-Medoids clustering
- KNN imputation for missing data
- Silhouette score analysis
- JavaFX desktop interface
- CSV import/export

##  Tech Stack
- Java 21
- JavaFX 21.0.9
- Maven
- JUnit
- Machine Learning algorithms

##  Prerequisites
- Java 21 or higher
- JavaFX SDK 21.0.9

##  Installation & Setup

### 1. Clone the repository
```bash
git clone https://github.com/AmaraX7/Survey-Management-System.git
cd Survey-Management-System
```

### 2. Download and setup JavaFX SDK
```bash
# Download JavaFX SDK 21.0.9 for Linux
wget https://download2.gluonhq.com/openjfx/21.0.9/openjfx-21.0.9_linux-x64_bin-sdk.zip

# Extract to lib directory
unzip openjfx-21.0.9_linux-x64_bin-sdk.zip -d lib/

# Rename folder (optional)
mv lib/javafx-sdk-21.0.9 lib/javafx-sdk-21.0.9

# Clean up
rm openjfx-21.0.9_linux-x64_bin-sdk.zip
```

### 3. Run the application
```bash
make all
make run-app
```

## Team
- Mohamed Amara El Houiti
- Ossama Chaer Dalerou
- Arnau Serra Florenciano
- Adam Ziani Hassun
