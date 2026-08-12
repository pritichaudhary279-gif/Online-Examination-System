# Online Examination and Evaluation System

A Java-based web application designed to conduct and manage online examinations efficiently. The system provides role-based access for **Admin, Teacher, and Student**, along with timed examinations, automated MCQ evaluation, result generation, analytics, notifications, and examination management.

## 📌 Project Overview

The **Online Examination and Evaluation System** digitizes the examination process, from creating and managing examinations to conducting exams, evaluating answers, generating results, and analyzing student performance.

The system helps reduce manual work and provides an organized platform for managing online examinations.

### Key Features

* Role-based access for Admin, Teacher, and Student
* Online examination management
* Timed examinations
* Automated MCQ evaluation
* Automatic result generation
* Question-bank management
* Student performance analysis
* Teacher result management
* CSV result export
* Notification management
* Examination event/proctoring log management

---

## 👥 User Roles

### 👨‍💼 Admin

The Admin manages the overall examination system.

**Capabilities:**

* Admin dashboard
* Manage users
* Manage courses
* Manage subjects
* Manage notifications
* Monitor examination statistics
* View student performance
* View subject-wise performance
* View pass-rate information

### 👨‍🏫 Teacher

Teachers manage examinations and questions.

**Capabilities:**

* Teacher dashboard
* Create examinations
* Save examinations as drafts
* Publish examinations
* Close examinations
* Manage question banks
* Add MCQ questions
* Configure examination duration and marks
* View examination results
* Export results in CSV format
* Monitor student performance

### 👨‍🎓 Student

Students can participate in examinations assigned to them.

**Capabilities:**

* Student dashboard
* View available examinations
* Filter examinations
* Start timed examinations
* Answer questions
* Save answers
* Resume unfinished examinations
* Submit examinations
* View results
* View performance information
* Receive notifications

---

## 🚀 Key Features

### 🔐 Authentication and Authorization

* Login system
* Role-based access control
* Admin, Teacher, and Student roles
* Session management
* Protected role-specific pages

### 📝 Examination Management

* Create examinations
* Save examinations as drafts
* Publish examinations
* Close examinations
* Configure examination duration
* Configure marks
* Manage question banks
* Course and subject-based examination management

### ⏱️ Timed Examination

* Countdown timer
* Answer saving
* Resume unfinished examinations
* Controlled examination submission
* Attempt management

### ✅ Automated Evaluation

* Automatic evaluation of MCQ questions
* Automatic score calculation
* Result generation
* Performance analysis

### 📊 Results and Analytics

The system provides examination performance information such as:

* Student scores
* Subject-wise performance
* Pass-rate information
* Examination statistics
* Student performance reports

### 📄 CSV Result Export

Teachers can export examination results in **CSV format** for record keeping and further analysis.

### 🔔 Notification Management

The system provides notification functionality for communicating important examination-related information to users.

### 🛡️ Proctoring Event Tracking

The system includes a foundation for recording examination-related events. These logs can be used for future integration with advanced AI-based examination monitoring.

---

## ⚙️ Technology Stack

| Technology    | Usage                           |
| ------------- | ------------------------------- |
| Java          | Backend Development             |
| Java Servlets | Request Handling                |
| JSP           | Dynamic Web Pages               |
| JSTL          | JSP Tag Libraries               |
| JDBC          | Database Connectivity           |
| MySQL         | Database Management             |
| Maven         | Build and Dependency Management |
| Apache Tomcat | Application Server              |
| HTML          | Frontend Structure              |
| CSS           | Styling                         |
| JavaScript    | Client-side Functionality       |
| Bootstrap     | User Interface                  |

---

## 📂 Project Structure

```text
OnlineExaminationSystem/
│
├── src/
│   └── main/
│       ├── java/
│       │   └── com/oes/
│       │       ├── dao/
│       │       ├── models/
│       │       ├── servlets/
│       │       └── utilities/
│       │
│       ├── resources/
│       │   └── com/oes/config/
│       │
│       └── webapp/
│           └── views/
│               ├── admin/
│               ├── teacher/
│               ├── student/
│               └── manage/
│
└── README.md
```

---

## 🎯 Project Objective

The main objective of this project is to develop a centralized online examination platform that simplifies:

* Examination creation
* Question management
* Online examination delivery
* Automated MCQ evaluation
* Result generation
* Student performance analysis
* Examination administration

The system aims to reduce manual examination work and provide a structured digital examination environment.

---

## 🔄 Examination Workflow

```text
Admin
  │
  ├── Manage Users
  ├── Manage Courses
  ├── Manage Subjects
  └── Manage Notifications
          │
          ▼
Teacher
  │
  ├── Create Examination
  ├── Add Questions
  ├── Save as Draft
  ├── Publish Examination
  └── Close Examination
          │
          ▼
Student
  │
  ├── View Available Examinations
  ├── Start Examination
  ├── Answer Questions
  ├── Save Answers
  └── Submit Examination
          │
          ▼
Evaluation
  │
  ├── MCQ Auto-Evaluation
  ├── Score Calculation
  └── Result Generation
          │
          ▼
Reports & Analytics
  │
  ├── Student Results
  ├── Subject Performance
  ├── Pass Rate
  └── CSV Export
```

---

## 🔮 Future Enhancements

The following features can be added in future versions:

### 🤖 AI-Based Proctoring

Integration with computer vision technologies such as **OpenCV** for:

* Face detection
* Multiple-face detection
* Face absence detection
* Suspicious activity detection
* Camera-based examination monitoring

### 🧠 AI-Based Subjective Answer Evaluation

Future versions can include NLP-based evaluation for subjective answers using:

* Keyword matching
* Semantic similarity
* Answer relevance
* Text similarity

### 📧 Advanced Notification System

Future versions can support:

* Email notifications
* Examination reminders
* Result notifications
* SMS notifications

---

## 🔒 Security

This project is developed primarily for academic and demonstration purposes.

For production deployment, additional security measures should be implemented, including:

* Secure password hashing
* Input validation
* Secure session management
* Protection against SQL injection
* Proper authorization controls
* Secure database configuration

**Do not store actual database passwords, API keys, or other sensitive credentials in the GitHub repository.**

---

## 📌 Project Status

| Feature                   | Status                |
| ------------------------- | --------------------- |
| Authentication            | ✅ Implemented         |
| Role-Based Access         | ✅ Implemented         |
| Admin Dashboard           | ✅ Implemented         |
| Teacher Dashboard         | ✅ Implemented         |
| Student Dashboard         | ✅ Implemented         |
| User Management           | ✅ Implemented         |
| Course Management         | ✅ Implemented         |
| Subject Management        | ✅ Implemented         |
| Examination Creation      | ✅ Implemented         |
| Question Bank             | ✅ Implemented         |
| Timed Examination         | ✅ Implemented         |
| MCQ Auto-Evaluation       | ✅ Implemented         |
| Result Generation         | ✅ Implemented         |
| Performance Analytics     | ✅ Implemented         |
| CSV Result Export         | ✅ Implemented         |
| Notifications             | ✅ Implemented         |
| Proctoring Event Tracking | ✅ Implemented         |
| AI-Based Proctoring       | 🔄 Future Enhancement |
| NLP-Based Evaluation      | 🔄 Future Enhancement |

---

## 👩‍💻 Author

Priti Chaudhari

Java | Web Development | Database | Process Automation

---

## 📚 Project Type

**Academic Project**

The project demonstrates the use of **Java web development, database management, examination automation, and process automation** to build a centralized online examination platform.
