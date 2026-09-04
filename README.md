# Ticket Booking System with AI Assistant

## Overview

A full-stack ticket booking platform built with **Spring Boot** and **Angular**, featuring an AI-powered assistant with **RAG, tool calling, conversation memory and automatic fact-checking**.

The application runs locally using **Docker Compose**, with Ollama providing the AI models and Qdrant handling vector search. Spring AI supports these AI application patterns, including RAG, chat memory, tool calling, and model evaluation.

## Table of Contents

* [Description](#description)
* [Architecture](#architecture)
* [Tech Stack](#tech-stack)
* [AI Assistant](#ai-assistant)
* [Setup](#setup)
* [Deployment](#deployment)

## Description

The system supports two user roles:

* **Users** - browse events, book available seats, view their bookings and interact with the AI assistant.
* **Admins** - manage events and categories and reindex event data for AI search.

Authentication uses **JWT with refresh tokens stored in HTTP-only cookies**, with BCrypt used for password hashing.

The booking system uses pessimistic locking to prevent concurrent users from booking the same seat.

## Architecture

The application consists of:

* **Angular** - frontend application.
* **Spring Boot** - backend REST API and business logic.
* **MySQL** - users, events, categories, bookings and chat messages.
* **Qdrant** - vector database for semantic event search.
* **Ollama** - local LLM and embedding models.

## Tech Stack

The project is built using the following technologies:

* **Spring Boot** - backend application and REST API.
* **Spring AI** - AI integration, RAG and tool calling.
* **Angular** - frontend application.
* **Ollama** - local LLM and embedding models.
* **Qdrant** - vector database for semantic search and RAG.
* **MySQL** - relational database.
* **Spring Security** - authentication and authorization.
* **Spring Data JPA** - database access and persistence.
* **MapStruct** - DTO and entity mapping.
* **Docker & Docker Compose** - containerization and local deployment.


## AI Assistant

The assistant uses three local Ollama models:

* `qwen3:8b` - response generation and reasoning.
* `bespoke-minicheck:7b` - response fact-checking.
* `nomic-embed-text:v1.5` - event embeddings.

The assistant uses **RAG** to retrieve relevant events from Qdrant and can use application tools when real-time information is required.

Available tools include:

* List the user's bookings.
* Check whether the user has booked an event.
* Find upcoming events the user has not booked.

The last **10 messages per user** are used as conversation context.

Generated responses are validated by the fact-checking model and regenerated up to **3 times** if validation fails.

Qdrant is supported as a Spring AI vector store and provides similarity search over document embeddings.

## Setup

### Prerequisites

* Docker
* Docker Compose
* Ollama

### Ollama models

```bash
ollama pull qwen3:8b
ollama pull bespoke-minicheck:7b
ollama pull nomic-embed-text:v1.5
```

### Secrets

Create the following files in the `secrets/` directory:

```text
mysql_root_password.txt
jwt_secret_key.txt
```

### Local development

Start the application from the repository root:

```bash
docker compose up --build
```

Stop the application with:

```bash
docker compose down
```

### Seeded data

A development administrator account is included:

* Username: `admin`
* Password: `12345678`

The database also contains sample categories and events.

## Deployment

The application is containerized and can be deployed using Docker Compose.
