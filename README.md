# 🍃 3 Season Restaurant — Smart Restaurant Ordering & Management System

ระบบสั่งอาหารออนไลน์ผ่าน QR Code ประจำโต๊ะ พร้อมระบบจัดการคิวครัว (KDS), เรียกพนักงาน, ชำระเงินสะสมแต้มสมาชิก และระบบรีวิวร้าน พัฒนาด้วย **Spring Boot 3.2** และ **Java 17**

---
## Group Members
6831503081 Acharayu Majan 

6831503083 Aisavares Seephut 

6831503094 Natthee Nimitchairoj


---
## 🛠️ Tech Stack
- **Backend:** Java 17, Spring Boot 3.2.0 (Spring Web, Spring Data JPA)
- **Frontend:** Thymeleaf, HTML5, CSS3, Bootstrap 5
- **Database:** H2 In-Memory Database (`jdbc:h2:mem:restaurantdb`)

---

## ✨ ฟีเจอร์หลักของระบบ (Core Features)
1. **Customer Ordering (`/?table={number}`)**
   - สั่งอาหารแยกตามหมายเลขโต๊ะ ค้นหาและกรองเมนูตามหมวดหมู่ (อาหารจานเดียว, กับข้าว, เครื่องดื่ม, ของหวาน)
   - ระบุหมายเหตุเพิ่มเติมในแต่ละรายการ (เช่น ไม่ใส่ผัก, เผ็ดน้อย)
2. **Staff Call System**
   - ปุ่มเรียกพนักงานจากหน้าโต๊ะอาหาร พร้อมระบบแจ้งเตือนไปยังหน้าจอพนักงานแบบเรียลไทม์
3. **Kitchen Display & Staff Queue (`/staff/queue`)**
   - แยกสถานะออเดอร์ชัดเจน: `Queued` (รอทำ) $\rightarrow$ `Cooking` (กำลังปรุง) $\rightarrow$ `Served` (เสิร์ฟแล้ว) $\rightarrow$ `Archived` (เช็กบิลแล้ว)
   - แสดงรายการโต๊ะที่กำลังเรียกพนักงานและรีวิวล่าสุด
4. **Payment & Loyalty Points (`/payment`)**
   - สรุปรายการอาหารและยอดชำระเงินของแต่ละโต๊ะ รองรับการสะสมแต้มสมาชิกด้วยเบอร์โทรศัพท์
5. **Customer Review System**
   - ลูกค้าสามารถให้คะแนน (1–5 ดาว) และเขียนข้อเสนอแนะหลังรับประทานอาหาร
6. **Admin Dashboard (`/admin`)**
   - แสดงยอดขายรวม จำนวนออเดอร์ คะแนนรีวิวเฉลี่ย และระบบเพิ่ม/ลบเมนูอาหาร
7. **Automated Data Seeder**
   - โหลดข้อมูลเมนูตัวอย่าง 7 รายการให้อัตโนมัติเมื่อเริ่มต้นรันเซิร์ฟเวอร์

---

## 🚀 วิธีการรันและทดสอบระบบ (How to Run)

1. เปิดโปรเจกต์ใน **VS Code** หรือ **IntelliJ IDEA** (ใช้ JDK 17 ขึ้นไป)
2. รันไฟล์หลัก `src/main/java/com/restaurant/ThreeSeasonApplication.java`
3. เปิดเบราว์เซอร์เข้าใช้งานตามลิงก์ด้านล่าง:
   - **หน้าสั่งอาหารลูกค้า (โต๊ะ 1):** `http://localhost:8080/?table=1`
   - **หน้าครัวและจัดการคิวพนักงาน:** `http://localhost:8080/staff/queue`
   - **หน้าผู้ดูแลระบบ (Admin Dashboard):** `http://localhost:8080/admin`
   - **หน้าฐานข้อมูล H2 Console:** `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:restaurantdb`, User: `sa`, Password: *(เว้นว่าง)*)
---
   ## Member Responsibilities
* **6831503081 Acharayu Majan:** พัฒนาระบบหน้าสั่งอาหารลูกค้า, ระบบตะกร้าสินค้า และหน้าติดตามสถานะคำสั่งซื้อ
* **6831503083 Aisavares Seephut:** แก้บั๊ก Redirect หลังสั่งอาหาร (`application.properties`) และอัปเดต `README.md`
* **6831503094 Natthee Nimitchairoj:** แก้ไขระบบจ่ายเงินให้ส่วนลดและราคาแสดงอย่างถูกต้อง
