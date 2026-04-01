-- ===============================================
-- IMPORT SCRIPT FOR MEDICAL SUPPLIES DATABASE
-- Ứng dụng bán vật tư y tế tích hợp ChatbotAI và thanh toán điện tử
-- ===============================================

-- Bước 1: Tạo database mới (nếu chưa có)
CREATE DATABASE IF NOT EXISTS `medical_supplies_db`
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci
  COMMENT 'Medical Supplies E-Commerce with AI ChatBot & Electronic Payment';

-- Bước 2: Sử dụng database
USE `medical_supplies_db`;

-- Bước 3: Import dữ liệu từ file data.sql
-- Chạy lệnh sau trong terminal:
-- mysql -u root -p medical_supplies_db < data.sql

-- Hoặc trong MySQL Workbench/phpMyAdmin:
-- 1. Chọn database medical_supplies_db
-- 2. Import file data.sql

-- ===============================================
-- VERIFY INSTALLATION
-- ===============================================

-- Kiểm tra các tables đã được tạo
SELECT TABLE_NAME, TABLE_COMMENT
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'medical_supplies_db'
ORDER BY TABLE_NAME;

-- Kiểm tra sample data
SELECT 'Users' as TableName, COUNT(*) as RecordCount FROM users
UNION ALL
SELECT 'Shops', COUNT(*) FROM shops
UNION ALL
SELECT 'Products', COUNT(*) FROM products
UNION ALL
SELECT 'Categories', COUNT(*) FROM categories
UNION ALL
SELECT 'Banners', COUNT(*) FROM banners
UNION ALL
SELECT 'Notifications', COUNT(*) FROM notifications;

-- ===============================================
-- NEXT STEPS
-- ===============================================
-- 1. Cập nhật Backend connection string:
--    database: 'medical_supplies_db'
--
-- 2. Test API endpoints với sample data
--
-- 3. Cấu hình AI ChatBot settings trong bảng ai_chatbot_settings
--
-- 4. Setup payment gateway credentials trong payment_methods
-- ===============================================