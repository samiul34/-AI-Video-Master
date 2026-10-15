# AI Video Master — 3D Cinematic Android Starter

বাংলা ভয়েস ও টেক্সট সার্চ, সিনেমাটিক ডার্ক UI, ভিডিও কার্ড এবং GitHub Actions দিয়ে debug APK তৈরির starter project।

## ফিচার
- Neon cyan/purple accent, gradient hero ও raised card UI
- বাংলা ভয়েস ইনপুট (ডিভাইসে speech recognition থাকলে)
- লিখে ভিডিও সার্চ; সার্চ ফলাফল ব্রাউজারে খোলে
- অনুমোদিত সরাসরি ভিডিও ফাইল URL (.mp4/.m4v/.webm/.mov) Android DownloadManager দিয়ে ডাউনলোড
- GitHub Actions workflow: `.github/workflows/build-apk.yml`

## সীমাবদ্ধতা
এটি একটি starter app, পূর্ণ AI assistant নয়। সার্চ query ভিডিও সার্চ পেজে পাঠায়। YouTube/Facebook পেজ URL থেকে ভিডিও ফাইল বের করে না; প্ল্যাটফর্মের অনুমোদিত API ও নিয়ম মেনে আলাদা integration দরকার। এই source bundle এখানে compile/test করা হয়নি; GitHub Actions build সফল কি না workflow log-এ যাচাই করুন।

## GitHub দিয়ে মোবাইল থেকেই APK বানানো
1. ZIP ডাউনলোড করে Extract করুন।
2. GitHub.com-এ Sign in করে New repository বানান, যেমন `ai-video-master`।
3. Extract করা ফোল্ডারের ভেতরের সব ফাইল upload করুন। ZIP-কে একা repository-তে upload করবেন না। Hidden `.github` folder এবং workflow file-টি অবশ্যই upload হবে।
4. `main` branch-এ Commit changes করুন।
5. Repository-র Actions tab খুলুন। যদি workflow enable করার বোতাম থাকে, সেটি চাপুন।
6. `Build AI Video Master APK` নির্বাচন করে `Run workflow` চাপুন; অথবা main-এ push/commit হলে automatic run-এর জন্য অপেক্ষা করুন।
7. সফল run খুলে নিচের Artifacts অংশ থেকে `AI-Video-Master-debug-APK` ডাউনলোড করুন।
8. Artifact ZIP extract করে `app-debug.apk` ফোনে ইনস্টল করুন। কেবল নিজের তৈরি/বিশ্বস্ত APK ইনস্টল করুন।

## Build configuration
- Java 17
- Gradle 8.9
- Android SDK 35
- Kotlin 2.0.21
- Android Gradle Plugin 8.7.3

Debug APK পরীক্ষার জন্য। Release distribution-এর জন্য signing key দিয়ে signed release APK বানাতে হবে।
