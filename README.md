<div align="center">

# 🕌 SalahGuard

<p align="center">
   <img width="120" height="120" alt="salahguardlogo" src="https://github.com/user-attachments/assets/babe4994-5407-4a0c-8533-7e3b7ee915ed" />

</p>

### *Hope over guilt. Consistency through compassion.*

A modern Android application designed to help Muslims stay consistent with prayer through mindful technology, beautiful design, and gentle encouragement.

![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?style=for-the-badge&logo=kotlin)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=for-the-badge)
![Architecture](https://img.shields.io/badge/Architecture-Clean%20Architecture-blue?style=for-the-badge)

</div>

---

# 🌙 About SalahGuard

SalahGuard is more than a prayer application.

It is a **digital spiritual companion** designed to help Muslims build a peaceful and consistent relationship with Salah through compassion rather than guilt.

Instead of overwhelming users with reminders, SalahGuard encourages mindful worship using beautiful design, reflection, learning, and progress tracking.

> **Technology should help us reconnect with our faith — not distract us from it.**

---

# ✨ Features

## 🏠 Home Dashboard
- Live prayer countdown
- Current & upcoming prayer overview
- Dynamic backgrounds based on prayer time
- Beautiful greeting experience

---

## 🕌 Prayer Experience
- Prayer details
- Prayer completion tracking
- Calm prayer interface
- Gentle reminders

---

## 🔔 Smart Notifications
- Prayer reminders
- Personalized notifications
- Calm and non-intrusive experience

---

## ⏰ Smart Prayer Alarm
- Dedicated Fajr alarm
- Prayer alarm scheduling
- Peaceful wake-up experience

---

## ✍ Reflection Journal
- Daily reflections
- Gratitude journaling
- Personal spiritual growth

---

## 📈 Journey
- Prayer streaks
- Monthly consistency
- Prayer calendar
- Progress analytics

---

## 📖 Learn
- Quran reading
- Arabic text
- English translation
- Audio recitation

---

## 🧭 Qibla Direction
- Accurate compass
- Elegant interface

---

## 🕌 Mosque Finder
- Locate nearby mosques
- Easy navigation

---

## 📿 Digital Tasbih
- Beautiful digital Tasbih
- Continue Dhikr after prayer

---


# 🛠 Tech Stack

| Category | Technology |
|----------|------------|
| Language | Kotlin |
| UI | Jetpack Compose |
| Design | Material Design 3 |
| Architecture | Clean Architecture |
| Pattern | MVVM |
| Dependency Injection | Hilt |
| Local Database | Room |
| Asynchronous | Coroutines + Flow |
| Navigation | Navigation Compose |
| IDE | Android Studio |

---

# 🛣️ RoadMap

<p align="center">
<img width="800" alt="image" src="https://github.com/user-attachments/assets/f2484118-1b11-437b-8022-c5fd6ca8e65e" />
</p>

---

# 📱 Screenshots

<p align="center">
   <img width="220"  alt="homepage" src="https://github.com/user-attachments/assets/367f3018-4875-456b-aece-1fb608c26106" />
   <img width="220"  alt="journey" src="https://github.com/user-attachments/assets/c1b63cbe-e932-4c29-9020-51dc9c171bda" />
   <img width="220"  alt="reflection" src="https://github.com/user-attachments/assets/afdb6444-57d5-4f27-b8ef-23703da198ed" />
</p>
<p align="center">
   <img width="220"  alt="qibla" src="https://github.com/user-attachments/assets/25368e75-7f60-4f10-bd41-18ec97b34348" />
   <img width="220"  alt="quran" src="https://github.com/user-attachments/assets/9213e10b-dc7c-4fb8-af19-a063c85e4fc0" />
   <img width="220"  alt="notification" src="https://github.com/user-attachments/assets/58a9ad82-91aa-46dc-861f-882658f15b9b" />
</p>
<p align="center">
   <img width="220"  alt="alarm" src="https://github.com/user-attachments/assets/56c5fdc2-4188-4bd8-b515-2f4880e662e7" />
   <img width="220"  alt="nearbymosque" src="https://github.com/user-attachments/assets/7f479055-604c-4dc9-b243-a6d6209e9ef6" />
   <img width="220"  alt="dhikr" src="https://github.com/user-attachments/assets/04b3ae82-306b-4388-b047-672b5cfab6b7" />
</p>

---

# 🎥 Demo

https://github.com/user-attachments/assets/03e8deec-67e7-487a-a325-c9434e93e1e5

---

# 🏗 Architecture

SalahGuard follows **Clean Architecture** with **MVVM** and the **Repository Pattern**.

```
Presentation Layer
        │
        ▼
Domain Layer
        ▲
        │
Data Layer
```

The **Domain Layer** contains pure Kotlin business logic and has **zero Android dependencies**, making the application scalable, maintainable, and easy to test.

---

# 📂 Project Structure

```
SalahGuard
│
├── data/
│   ├── local/
│   │   ├── dao/
│   │   ├── entity/
│   │
│   └── repository/
│
├── domain/
│   ├── model/
│   ├── repository/
│   └── usecase/
│
├── presentation/
│   ├── components/
│   ├── navigation/
│   ├── screens/
│   ├── theme/
│   └── viewmodel/
│
├── di/
│
└── app/
```

---

# 🚀 Current Progress

### ✅ Completed

- Home Dashboard
- Prayer Tracking
- Prayer Notifications
- Smart Alarm
- Reflection Journal
- Journey Tracking
- Quran Learning
- Protection Settings
- Modern UI/UX
- Dynamic Prayer Themes
- Qibla Direction
- Mosque Finder
- Digital Tasbih

### 💡 Planned

- AI Reflection Insights
- Widgets
- Wear OS Support
- Cloud Sync
- Multi-language Support

---

# 🚀 Getting Started

## Clone the repository

```bash
git clone https://github.com/YOUR_USERNAME/SalahGuard.git
```

## Open in Android Studio

```
File → Open → SalahGuard
```

## Sync Gradle

Allow Android Studio to download all dependencies.

---

## Run

- Android Studio Koala or newer
- Android 8.0+
- Min SDK 26

---

# 🎯 Design Philosophy

SalahGuard was built around one belief:

> **Prayer should feel peaceful, not pressured.**

Every screen, animation, reminder, and interaction is designed to reduce friction and help users reconnect with Allah through hope, reflection, and consistency.

---

# 🤝 Contributing

Contributions, suggestions, and feedback are always welcome.

Feel free to open an Issue or submit a Pull Request.

---

# 📄 License

This project is intended for educational and portfolio purposes.

---

<div align="center">

## 🌙 SalahGuard

### Hope over guilt.

### Consistency through compassion.

**Made with ❤️ to help Muslims build a peaceful relationship with Salah.**

</div>
