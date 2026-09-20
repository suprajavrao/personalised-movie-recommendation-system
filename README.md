# Personalised Movie Recommendation System

A Java-based desktop application that recommends movies based on the user's age, mood, preferred genre, and language.

## Overview

The Personalised Movie Recommendation System is built using Java Swing and provides a simple graphical interface for finding movies based on user preferences.

The system loads movie information from a CSV dataset and applies rule-based filtering to generate recommendations.

## Features

- Age-based content rating filtering
- Mood-based movie recommendations
- Genre-based filtering
- Language filtering
- IMDb rating-based sorting
- Customizable number of results
- Movie descriptions
- Input validation for age
- Interactive Java Swing GUI

## Technologies Used

- Java
- Java Swing
- Object-Oriented Programming (OOP)
- Java Collections
- Java Streams
- CSV data handling

## How It Works

1. The user enters their age.
2. The user selects a mood.
3. The user selects a preferred genre.
4. The user selects a language.
5. The user chooses the number of results.
6. The system filters movies based on the selected preferences.
7. Matching movies are sorted using IMDb ratings.
8. The results are displayed in the graphical interface.
9. Selecting a movie displays its description.

## Project Structure

```text
personalised-movie-recommendation-system/
│
├── MovieRecommender.java
├── README.md
│
└── data/
    └── movies.csv
