# Hacker News Client

In this project is developed a basic Hacker News client. It fetches the top stories and shows them in a table giving the user the option to filter and sort posts by various criteria.

The main purpose of this project is to build a close to real-world app, that displays useful data and retrieves data from a server.

## Essential Features

- **Real-Time Data:** Fetches the latest top stories directly from the official Hacker News Firebase API.
- **Development & Production Modes:** Supports a local simulated server for development and testing and seamless data-fetching mode for production.
- **Loading States:** Displays dynamic loading indicators while stories and individual posts are being fetched.
- **Customizable Views:** Uses modular, reusable components to display posts in a structured and accessible table.

## Technologies used

This project leverages modern functional programming and standard web technologies for safety and efficiency:

- **Elm:** The main language used for building the frontend. It provides a solid architecture (Model-View-Update), guaranteeing no runtime exceptions.
- **HTML/CSS:** Used to render the UI components.
- **TypeScript & Node.js:** Used in the backend scripts to provide a local mock API server for development.
- **Elm-Test:** Ensures components and data structures remain reliable and bug-free.