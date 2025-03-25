# 📦 Creating a .deb Package for the Chat Application

This guide explains how to package your `chat.jar` file into a `.deb` package so users can install it and run `lc32` from the terminal.

---

## **📌 Prerequisites**
- A compiled `chat.jar` file
- A Linux system with `dpkg` installed

---

## **1️⃣ Create the Folder Structure**
Create the necessary directories:
```sh
mkdir -p lc32-deb/DEBIAN
mkdir -p lc32-deb/usr/bin
mkdir -p lc32-deb/usr/share/lc32
```

---

## **2️⃣ Move the JAR File**
Copy `chat.jar` to `/usr/share/lc32/` inside the package structure:
```sh
cp chat.jar lc32-deb/usr/share/lc32/
```

---

## **3️⃣ Create a Launcher Script**
Create a script that allows users to run `lc32` in the terminal:
```sh
touch lc32-deb/usr/bin/lc32
chmod +x lc32-deb/usr/bin/lc32
```
Edit `lc32-deb/usr/bin/lc32` and add the following content:
```sh
#!/bin/bash
java -jar /usr/share/lc32/chat.jar
```

---

## **4️⃣ Create the Control File**
Create a control file that defines package metadata:
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

---

## **5️⃣ Set Correct Permissions**
Ensure proper permissions for files and folders:
```sh
chmod 755 lc32-deb/usr/bin/lc32
chmod 755 lc32-deb/DEBIAN
```

---

## **6️⃣ Build the .deb Package**
Run the following command to create the `.deb` package:
```sh
dpkg-deb --build lc32-deb
```
This will generate `lc32-deb.deb` in the current directory.

---

## **7️⃣ Install the .deb Package**
Install the package using:
```sh
sudo dpkg -i lc32-deb.deb
```

If you encounter dependency issues, fix them by running:
```sh
sudo apt --fix-broken install
```

---

## **8️⃣ Run the Application**
Once installed, you can run the chat application using:
```sh
lc32
```

---

## **🚀 Done!**
Now, users can install your `.deb` package and start the chat application by simply running `lc32` from the terminal. 🎉
