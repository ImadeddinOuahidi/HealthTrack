# HealthTrack - Android Health and Fitness App

Maintaining good health goes beyond just a good diet. Hydration, sleep, and balanced daily routines are critical to overall wellness. Many individuals forget to drink enough water, sleep poorly, or fail to track daily habits like step activity and rest breaks. Existing apps often focus heavily on workouts or calorie tracking, making them overwhelming and less appealing for users who just want simple, focused wellness tracking.
HealthTrack can provide a lightweight and intuitive mobile solution to help users stay on track with hydration, sleep, step counts, relaxation, and personal wellness goals. By combining reminders, habit tracking, and motivating challenges, HealthTrack supports healthier routines in a way that is easy to maintain.


## Team Information

* Imadeddin Ouahidi
* Sumash Chandra Bandaru
* Peyton Stahyl
* Lohitha Vodnala

## Application Information

### Test Credentials

To test the application with a pre-populated account, please use the following credentials:

*   **Email:** `test@gmail.com`
*   **Password:** `Test1234`

### APK 

`/app/build/outputs/apk/debug/`

### Supported Devices

The minimum supported version is **Android 8.0 (Oreo, API Level 26)**, and it is optimized for **Android 12 (S, API Level 31)** and above.

### Sequence Information (Application Flow)

The application follows a logical and intuitive user flow:

1.  **Login/Registration:** The user is first presented with a screen to either log in with their existing credentials or register for a new account. All user data is securely managed through Firebase Authentication.

2.  **Dashboard:** After logging in, the user lands on the main dashboard. This screen provides an overview of their current day's progress for hydration, sleep, and steps, compared to their set goals.

3.  **Core Features (Quick Navigation):** From the dashboard, the user can navigate to any of the core feature modules:
    *   **Hydration:** Log water intake and view a graph of the day's consumption.
    *   **Sleep:** Log sleep duration and quality, and view a graph of the week's sleep patterns.
    *   **Steps:** View the current step count and see a graph of the week's activity.
    *   **Goals:** Set or update personal goals for hydration, sleep, and steps.
    *   **Reports:** View long-term (30-day) trend graphs for all key metrics.
    *   **Rewards:** View a list of all available achievements and see which ones have been unlocked.
    *   **Notifications:** Configure and enable/disable reminders for hydration, sleep, and steps.

4.  **Firebase Integration:** All user data, including profile information, daily logs, goals, and settings, is stored in real-time in the Firebase Realtime Database, ensuring data is always synced across devices.

