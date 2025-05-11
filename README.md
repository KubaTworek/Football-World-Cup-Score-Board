# Football World Cup Score Board

This project is a clean and testable Java implementation of a Football Score Board, built as part of a recruitment exercise. It demonstrates a simple, in-memory system to manage live football scores with attention to code quality, thread safety, and maintainability.


---

## 🎯 Purpose

The goal was to implement a lightweight Java library (not a service or API) that satisfies a set of core requirements:

- ✅ Start a new game with an initial score of 0 - 0
- ✅ Update an ongoing game's score
- ✅ Finish a game and remove it from the board
- ✅ Display a summary of games ordered by:
  - Total score (descending)
  - Time of addition (most recent first if scores are equal)

---

## 💡 Design Principles
- **Simple & Focused**: Follows the problem specification strictly, avoiding unnecessary abstractions
- **In-memory data store**: Uses standard Java collections (`ConcurrentHashMap`, etc.)
- **Thread-safe**: All operations are safe for concurrent access, with optimistic locking via `ConcurrentHashMap#replace`
- **Clean Architecture**:
  - Separation of domain (`Match`, `MatchKey`, etc.) and application logic (`FootballScoreBoard`)
  - Testable components with minimal dependencies
- **TDD and SOLID**: Designed and implemented with unit and integration testing in mind

---

## 📌 Features

#### **Functional**

- Start new matches
- Update scores
- Finish games
- Get a real-time summary

#### **Non-Functional**

- Thread-safe operations using Java concurrency utilities
- Optimistic locking ensures consistent updates
- Cached summary for performance
- Fully unit- and integration-tested

---

## 📄 Assumptions

- A team cannot participate in more than one game at a time
- Team names are case-insensitive
- Duplicate or invalid game setups are rejected early
- No persistence mechanism was added to maintain focus on simplicity and testability

---

## 🔧 Technologies Used

- Java 17+ (tested on Java 23)
- JUnit 5
- Mockito

---

## 🧪 Tests

- ✅ Unit tests for core logic (`Match`, `MatchRepository`, `FootballScoreBoard`)
- ✅ Integration tests for end-to-end flows
- ✅ Concurrency scenarios (e.g. `OptimisticLockException`)
- ✅ Parametrized validation tests

---

## 🛡 Known Limitations

- Data is **volatile** and lost on application shutdown
- Uses `System.nanoTime()` for time-based ordering (not guaranteed to be stable across JVMs)
- **Optimistic locking** is based on in-memory reference equality, which won't work across multiple instances
- Not **scalable** in distributed environments — intended as a single-instance demo

---

## 👨‍💻 Author

Jakub Tworek  
GitHub: [@KubaTworek](https://github.com/KubaTworek)