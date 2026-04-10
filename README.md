# 🏗️ Equipment and Resource Booking Management System

![Java](https://img.shields.io/badge/Java-25-blue?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.5-6DB33F?logo=springboot&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.9-C71A36?logo=apachemaven&logoColor=white)
![JUnit](https://img.shields.io/badge/JUnit-5-25A162?logo=junit5&logoColor=white)
![Tests](https://img.shields.io/badge/Tests-64%20passed-brightgreen)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.1-005F0F?logo=thymeleaf&logoColor=white)
![H2](https://img.shields.io/badge/H2-In--Memory-1021FF)

**[Live Application](https://stan-equipment-rental-system.onrender.com)** · **[Project Documentation](https://stanstarishko.github.io/equipment-rental-system/)**

## The Story

This project started with a real problem. While volunteering at Glasgow Film Theatre and Shaw Trust, I watched staff juggle equipment bookings through spreadsheets, sticky notes and memory. A projector double-booked for two events. A laptop "somewhere in the building" with no record of who took it. Simple problems that a simple system could solve.

So I built one.

## What I Built

A full-stack web application where staff can register equipment, organise it into categories, create and cancel bookings, and see at a glance what is available and what is not. The dashboard shows live availability counts and active bookings. The booking form detects conflicts automatically, so double-bookings simply cannot happen. Eight business rules are enforced in the service layer and backed by 64 automated tests.

The system is live and you can try it right now: **[stan-equipment-rental-system.onrender.com](https://stan-equipment-rental-system.onrender.com)**

> It runs on Render's free tier, so if it has been idle the first load may take a minute or two while the JVM wakes up.

## What I Learned

None of the technologies in this project were covered in my course. Spring Boot, JPA, Thymeleaf, Mockito, Bean Validation: I learned all of them from official documentation, Baeldung tutorials and a fair amount of trial and error. The biggest challenge was getting the booking conflict detection right. A custom JPQL query that checks for overlapping date ranges across confirmed bookings, tested against a real H2 database with @DataJpaTest. It took me longer than I would like to admit, but it works, and I understand every line of it.

The testing phase taught me something I did not expect. I wrote 63 unit tests and they all passed. Then I opened the browser, cancelled a booking, and the equipment status stayed at BOOKED. A real bug that none of my tests caught, because I had tested the wrong thing. I added a regression test, fixed the code, ran everything again. 64 tests, all green. That experience taught me more about testing than any textbook could.

## How I Work

I planned before I coded. Requirements, UML diagrams, database design, wireframes: all documented before writing a single line of Java. When things changed during development (and they did), I made deliberate decisions about what to descope and why, rather than quietly dropping features and hoping nobody would notice.

The codebase reflects how I think about code quality. Meaningful variable names everywhere, even in lambdas and loops. Comments only where the intent is not obvious from the code itself. A layered architecture where each package has one job. Consistent formatting throughout because readability matters, especially when someone else has to maintain your code.

## Running It Locally

```bash
git clone https://github.com/StanStarishko/equipment-rental-system.git
cd equipment-rental-system
./mvnw spring-boot:run
```

Opens at `http://localhost:8080` with sample data loaded automatically.

## Documentation

The full project documentation, including planning reports, test results and Javadoc, is available at the **[documentation hub](https://stanstarishko.github.io/equipment-rental-system/)**.

---

**© April 2026 Stanislav Starishko**