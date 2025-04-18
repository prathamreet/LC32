# Chat Application 
﻿<img src="https://capsule-render.vercel.app/api?type=soft&color=gradient&height=10&section=header" width="1080" align="center"/>


### Windows: [download JAR ⬇️](https://github.com/prathamreet/LC32/raw/main/run/lc32.jar)
####
  ```sh
  java -jar lc32.jar
  ```
### Linux: [download DEB ⬇️](https://github.com/prathamreet/LC32/raw/main/run/lc32.deb)
####
  ```sh
  sudo dpkg -i lc32.deb
  ```
  ```sh
  lc32
  ```

﻿<img src="https://capsule-render.vercel.app/api?type=soft&color=gradient&height=10&section=header" width="1080" align="center"/>
# Creating and Running a JAR File

###  Prerequisites
- Java Development Kit (JDK) installed
- A Java project with `.java` source files

### Compiling Java Files
```sh
javac -d out src/*.java
```

### Creating a Manifest File
To specify the entry point, create a `manifest.txt` file inside the `out/` directory with the following content:
```
Main-Class: Main
```
 **Note:** Ensure there is an empty newline at the end of the file to avoid errors.

### Creating the JAR File
```sh
jar cfm lc32.jar out/manifest.txt -C out .
```

### Running the JAR File
```sh
java -jar lc32.jar
```


### Running Without a JAR File (`java Main` Method)
If you want to run the program without packaging it into a JAR file, use:
```sh
java -cp out Main
```
﻿<img src="https://capsule-render.vercel.app/api?type=soft&color=gradient&height=10&section=header" width="1080" align="center"/>


# Creating a .deb Package for the Chat Application


### Prerequisites
- A compiled `chat.jar` file
- A Linux system with `dpkg` installed


### Create the Folder Structure
```sh
mkdir -p lc32-deb/DEBIAN
mkdir -p lc32-deb/usr/bin
mkdir -p lc32-deb/usr/share/lc32
```


### Move the JAR File
```sh
cp chat.jar lc32-deb/usr/share/lc32/
```

### Create a Launcher Script
```sh
touch lc32-deb/usr/bin/lc32
chmod +x lc32-deb/usr/bin/lc32
```
### Edit `lc32-deb/usr/bin/lc32` and add the following content:
```sh
#!/bin/bash
java -jar /usr/share/lc32/chat.jar
```


### Create the Control File
```sh
nano lc32-deb/DEBIAN/control
```
Add the following content:
```
Package: lc32
Version: 1.0
Architecture: all
Maintainer: Your Name <your.email@example.com>
Depends: default-jre
Description: A simple LAN chat application
```


### Set Correct Permissions
```sh
chmod 755 lc32-deb/usr/bin/lc32
chmod 755 lc32-deb/DEBIAN
```


### Build the .deb Package
```sh
dpkg-deb --build lc32-deb
```


### Install the .deb Package
```sh
sudo dpkg -i lc32-deb.deb
```

If you encounter dependency issues, fix them by running:
```sh
sudo apt --fix-broken install
```


### Run the Application
```sh
lc32
```

﻿<img src="https://capsule-render.vercel.app/api?type=soft&color=gradient&height=10&section=header" width="1080" align="center"/>

