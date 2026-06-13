# Smart Jewel POS & CRM - Application Documentation

## Project Identity
- **Application Name**: স্বর্ণালি শিল্পালয় (Swarnali Shilpaloy)
- **Application ID**: `com.aistudio.smartjewel.krmnpb`
- **Main Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)

---

## 🏢 Proprietor & Business Details
The application is custom-branded for the following business identity:
- **Owner Name**: সুভাষ চন্দ্র পাল শান্ত (Subhash Chandra Paul Shanto)
- **Business Address**: তারাকান্দা মধ্যবাজার (Tarakanda Madhyabazar)
- **Contact Numbers**: 01712416731, 01824949920

---

## 🚀 Key Functional Modules

### 1. Jewelry POS (Point of Sale)
A high-performance billing interface optimized for both mobile and tablet devices.
- **Dual-Pane Layout**: Real-time ornament catalog on the left and dynamic invoice desk on the right (for wide screens).
- **Automated Billing**: Calculates total weight, subtotal, making charges, discounts, and GST taxes automatically.
- **Cart Management**: Real-time price updates for items in the cart if metal rates change during a session.
- **Checkout Flow**: Deducts stock from inventory and saves finalized invoices to the database.

### 2. CRM & Client Database
- **Customer Profiles**: Securely stores customer contact details and purchase histories locally.
- **Advanced Search**: Filter customers by name, phone, or address.
- **PDF Reporting**: Generates professional PDF reports of the customer database with business branding.

### 3. Inventory Management
- **Product Catalog**: Track ornaments by unique codes, purity (22K, 21K, etc.), and weight.
- **Stock Tracking**: Automated inventory updates upon sales.
- **Metal Rate Integration**: Prices auto-adjust based on live market rates for Gold and Silver.

### 4. AI Business Intelligence (Gemini AI)
- **Marketing Automation**: Generates personalized, persuasive promotional messages for WhatsApp/SMS using Gemini.
- **Business Analytics**: Analyzes daily sales data to provide smart insights and summaries for the owner.

### 5. Metal Rate Tracking
- **Live Rates**: Dynamic monitoring of Gold (24K, 22K, 21K, 18K) and Silver prices.
- **Currency Support**: Integrated support for BDT (Taka) and International reference currencies.

---

## 🛠 Technical Architecture
- **Architecture**: MVVM (Model-View-ViewModel) with a clean separation of data and UI.
- **Data Persistence**: **Room Database** for high-speed, offline-capable storage.
- **Networking**: **Retrofit** for API communication (Rates & AI).
- **Dependency Injection**: Simple constructor injection for modularity.
- **Internationalization**: Seamless English/Bengali localization engine.

---

## 🧪 Quality & Testing
- **Unit Testing**: Verified via Robolectric.
- **Visual Regression**: **Roborazzi** integrated for UI consistency checks.
- **Build Automation**: Custom Gradle tasks (`deployApk`) for automated APK exports.

---

## 📁 Build Artifacts
- **Debug APK**: Located at `.build-outputs/app-debug.apk` and `APK_DOWNLOAD/app-debug.apk`.
