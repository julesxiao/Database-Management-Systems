## Overview
This project implements core MongoDB functionality in Java, loosely inspired by the official MongoDB Java driver. The driver enables Java applications to connect to and interact with a MongoDB database.

Unlike the official driver, this implementation also handles data management by reading and writing JSON files to disk, allowing for a lightweight, file-based storage system.

## Key Features
• Database Connectivity. Enables Java applications to interact with a MongoDB-like database.

• Custom Data Management. Reads and writes data in JSON format, mimicking MongoDB document storage.

• Lightweight & Standalone. Does not require an actual MongoDB server, making it ideal for testing and offline use.

• CRUD Operations. Supports basic Create, Read, Update, and Delete operations on stored JSON data.

## Usage
Initialize the Driver – Set up the connection and specify the storage location.

Perform CRUD Operations – Insert, retrieve, update, or delete data using Java methods.

Persistent Storage – Data is stored and retrieved from JSON files, ensuring persistence across sessions.

## Future Enhancements
• Implement support for indexing and query optimization.

• Expand functionality to support aggregations and complex queries.

• Improve error handling and logging for better debugging.

