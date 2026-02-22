# E-Commerce Backend Service

A robust and scalable Order Management System developed in Java. The application provides a complete graphical user interface (GUI) designed to manage clients, products and orders within an e-commerce ecosystem.

It implements a clean **layered architecture** that strictly separates the Presentation Layer, Business Logic Layer (BLL) and Data Access Object Layer (DAO).

## Overview

The system allows users to seamlessly perform CRUD operations (Create, Read, Update, Delete) on clients and products. Furthermore, it supports processing product orders for clients, automatically computing the bill, generating order logs and updating the database. The database connection is handled securely and data manipulation is heavily generalized using **Java Reflection**.

### Essential Features
- **Client Management:** Register new clients, edit their profiles (name, address, email, phone number) and delete records.
- **Product Management:** Add new items to the inventory, modify product names, adjust prices and manage stock quantities.
- **Order Processing:** Allocate products to clients securely. Features built-in validations ensuring sufficient stock.
- **Automated Billing:** Generates comprehensive bills detailing client information, product unit prices and quantities, right after placing orders. Bills are logged for future reference.
- **Generic Data Access Object:** Uses Java Reflection to implement highly reusable and generic database operations (INSER, SELECT, UPDATE, DELETE) for any given Model Entity, heavily reducing boilerplate repository code.

## Technologies Used

- **Programming Language:** Java 23
- **User Interface:** Java Swing
- **Database Management:** MySQL
- **Build Automation Tool:** Maven
- **Core Concepts applied:**
	- Object-Oriented Programming (OOP)
	- Layered Architecture (Presentation, Business Logic, Data Access, Connection, Data Model)
	- Java Reflection
	- Java Streams API
