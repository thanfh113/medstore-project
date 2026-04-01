-- MySQL dump 10.13  Distrib 8.0.19, for Win64 (x86_64)
--
-- Host: localhost    Database: medical_supplies_db
-- Database: Ứng dụng bán vật tư y tế tích hợp ChatbotAI và thanh toán điện tử
-- Medical Supplies E-Commerce with AI ChatBot and Electronic Payment
-- Server version	8.0.44

-- ===============================================
-- CREATE DATABASE
-- ===============================================
CREATE DATABASE IF NOT EXISTS `medical_supplies_db`
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `medical_supplies_db`;

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `banners`
--

DROP TABLE IF EXISTS `banners`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `banners` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `image_url` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `link_url` text COLLATE utf8mb4_unicode_ci COMMENT 'URL để navigate khi click banner',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `sort_order` int NOT NULL DEFAULT '0',
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `start_dt` datetime DEFAULT NULL COMMENT 'Thời điểm bắt đầu hiển thị',
  `end_dt` datetime DEFAULT NULL COMMENT 'Thời điểm kết thúc hiển thị',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_banners_active` (`is_active`),
  KEY `idx_banners_active_date` (`is_active`,`start_dt`,`end_dt`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `banners`
--

LOCK TABLES `banners` WRITE;
/*!40000 ALTER TABLE `banners` DISABLE KEYS */;
/*!40000 ALTER TABLE `banners` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cart_items`
--

DROP TABLE IF EXISTS `cart_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cart_items` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `product_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `quantity` int NOT NULL DEFAULT '1',
  `unit` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'Hộp',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `fk_cart_items_user_id__id` (`user_id`),
  KEY `fk_cart_items_product_id__id` (`product_id`),
  CONSTRAINT `fk_cart_items_product_id__id` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_cart_items_user_id__id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cart_items`
--

LOCK TABLES `cart_items` WRITE;
/*!40000 ALTER TABLE `cart_items` DISABLE KEYS */;
/*!40000 ALTER TABLE `cart_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `categories`
--

DROP TABLE IF EXISTS `categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categories` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `parent_id` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `slug` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `product_type_default` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `icon_url` text COLLATE utf8mb4_unicode_ci,
  `sort_order` int NOT NULL DEFAULT '0',
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `deleted_at` datetime DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_categories_slug` (`slug`),
  KEY `fk_cat_parent` (`parent_id`),
  CONSTRAINT `fk_cat_parent` FOREIGN KEY (`parent_id`) REFERENCES `categories` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categories`
--

LOCK TABLES `categories` WRITE;
/*!40000 ALTER TABLE `categories` DISABLE KEYS */;
/*!40000 ALTER TABLE `categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `category_attributes`
--

DROP TABLE IF EXISTS `category_attributes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `category_attributes` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `category_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `attr_key` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `label` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `data_type` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `unit` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_required` tinyint(1) NOT NULL DEFAULT '0',
  `is_searchable` tinyint(1) NOT NULL DEFAULT '0',
  `sort_order` int NOT NULL DEFAULT '0',
  `options_json` json DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_cat_attr_key` (`category_id`,`attr_key`),
  CONSTRAINT `fk_attr_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `category_attributes`
--

LOCK TABLES `category_attributes` WRITE;
/*!40000 ALTER TABLE `category_attributes` DISABLE KEYS */;
/*!40000 ALTER TABLE `category_attributes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chat_messages`
--

DROP TABLE IF EXISTS `chat_messages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_messages` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `session_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `sender_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `sender_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'USER' COMMENT 'USER|CONSULTANT|AI_BOT',
  `content` text COLLATE utf8mb4_unicode_ci,
  `type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'TEXT' COMMENT 'TEXT|IMAGE|PRODUCT_CARD|SUGGESTION',
  `metadata` json DEFAULT NULL COMMENT 'product info, suggestions, etc',
  `is_read` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `fk_chat_messages_session_id__id` (`session_id`),
  KEY `fk_chat_messages_sender_id__id` (`sender_id`),
  KEY `idx_messages_sender_type` (`sender_type`),
  CONSTRAINT `fk_chat_messages_sender_id__id` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_chat_messages_session_id__id` FOREIGN KEY (`session_id`) REFERENCES `chat_sessions` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chat_messages`
--

LOCK TABLES `chat_messages` WRITE;
/*!40000 ALTER TABLE `chat_messages` DISABLE KEYS */;
/*!40000 ALTER TABLE `chat_messages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chat_sessions`
--

DROP TABLE IF EXISTS `chat_sessions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_sessions` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `shop_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `product_id` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'context sản phẩm nếu có',
  `session_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'AI' COMMENT 'HUMAN|AI|HYBRID',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'OPEN' COMMENT 'OPEN|CLOSED|TRANSFERRED',
  `assigned_consultant_id` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Dược sĩ/Healthcare Consultant được gán',
  `escalated_to_human` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Từ AI được escalate lên human',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `closed_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_chat_sessions_user_id__id` (`user_id`),
  KEY `fk_chat_sessions_shop_id__id` (`shop_id`),
  KEY `fk_chat_sessions_product_id__id` (`product_id`),
  KEY `fk_chat_sessions_consultant__id` (`assigned_consultant_id`),
  KEY `idx_chat_sessions_type` (`session_type`),
  CONSTRAINT `fk_chat_sessions_product_id__id` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_chat_sessions_shop_id__id` FOREIGN KEY (`shop_id`) REFERENCES `shops` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_chat_sessions_user_id__id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_chat_sessions_consultant__id` FOREIGN KEY (`assigned_consultant_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chat_sessions`
--

LOCK TABLES `chat_sessions` WRITE;
/*!40000 ALTER TABLE `chat_sessions` DISABLE KEYS */;
/*!40000 ALTER TABLE `chat_sessions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_chatbot_settings`
--

DROP TABLE IF EXISTS `ai_chatbot_settings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_chatbot_settings` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `shop_id` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'NULL = system-wide config, shop-specific otherwise',
  `ai_provider` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'OPENAI|GEMINI|CLAUDE|LOCAL',
  `model_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'gpt-4, gemini-pro, claude-opus, etc',
  `temperature` decimal(3,2) DEFAULT '0.7' COMMENT 'Creativity level (0.0-1.0)',
  `max_tokens` int DEFAULT '1000' COMMENT 'Max response length',
  `system_prompt` text COLLATE utf8mb4_unicode_ci COMMENT 'System instruction for AI',
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `fk_ai_chatbot_settings_shop_id` (`shop_id`),
  CONSTRAINT `fk_ai_chatbot_settings_shop_id` FOREIGN KEY (`shop_id`) REFERENCES `shops` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_chatbot_settings`
--

LOCK TABLES `ai_chatbot_settings` WRITE;
/*!40000 ALTER TABLE `ai_chatbot_settings` DISABLE KEYS */;
/*!40000 ALTER TABLE `ai_chatbot_settings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_conversations`
--

DROP TABLE IF EXISTS `ai_conversations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_conversations` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `chat_session_id` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Link tới chat session',
  `user_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `shop_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `product_id` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Product context if any',
  `conversation_history` json DEFAULT NULL COMMENT 'Lịch sử cuộc trò chuyện dạng JSON',
  `intent` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Product inquiry, Price check, Health advice, etc',
  `sentiment` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'POSITIVE|NEUTRAL|NEGATIVE',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE|CLOSED|ESCALATED',
  `escalated_to_consultant` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Đã được escalate lên consultant',
  `consultant_id` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Consultant nhận escalation',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `closed_at` datetime DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `fk_ai_conversations_session` (`chat_session_id`),
  KEY `fk_ai_conversations_user` (`user_id`),
  KEY `fk_ai_conversations_shop` (`shop_id`),
  KEY `fk_ai_conversations_product` (`product_id`),
  KEY `fk_ai_conversations_consultant` (`consultant_id`),
  CONSTRAINT `fk_ai_conversations_session` FOREIGN KEY (`chat_session_id`) REFERENCES `chat_sessions` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_ai_conversations_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_ai_conversations_shop` FOREIGN KEY (`shop_id`) REFERENCES `shops` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_ai_conversations_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_ai_conversations_consultant` FOREIGN KEY (`consultant_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_conversations`
--

LOCK TABLES `ai_conversations` WRITE;
/*!40000 ALTER TABLE `ai_conversations` DISABLE KEYS */;
/*!40000 ALTER TABLE `ai_conversations` ENABLE KEYS */;
UNLOCK TABLES;

DROP TABLE IF EXISTS `disease_categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `disease_categories` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Tim mạch, Hô hấp, Dạ dày...',
  `icon_url` text COLLATE utf8mb4_unicode_ci,
  `description` text COLLATE utf8mb4_unicode_ci,
  `sort_order` int DEFAULT '0',
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_disease_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `disease_categories`
--

LOCK TABLES `disease_categories` WRITE;
/*!40000 ALTER TABLE `disease_categories` DISABLE KEYS */;
/*!40000 ALTER TABLE `disease_categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `health_articles`
--

DROP TABLE IF EXISTS `health_articles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `health_articles` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `title` varchar(300) COLLATE utf8mb4_unicode_ci NOT NULL,
  `content` text COLLATE utf8mb4_unicode_ci,
  `summary` varchar(500) COLLATE utf8mb4_unicode_ci COMMENT 'Tóm tắt ngắn gon',
  `thumbnail_url` text COLLATE utf8mb4_unicode_ci,
  `author` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Dược sĩ, Bác sĩ, người viết',
  `category` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Dinh dưỡng, Sức khỏe, Bệnh lý, v.v.',
  `is_published` tinyint(1) NOT NULL DEFAULT '0',
  `published_at` datetime DEFAULT NULL,
  `view_count` int DEFAULT '0',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_health_published` (`is_published`),
  KEY `idx_health_category` (`category`),
  KEY `idx_health_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `health_articles`
--

LOCK TABLES `health_articles` WRITE;
/*!40000 ALTER TABLE `health_articles` DISABLE KEYS */;
/*!40000 ALTER TABLE `health_articles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notifications`
--

DROP TABLE IF EXISTS `notifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notifications` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `body` text COLLATE utf8mb4_unicode_ci,
  `type` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ORDER_STATUS|PROMOTION|CHAT|REWARD|SYSTEM|PRODUCT',
  `ref_id` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'order_id, product_id, chat_session_id, etc',
  `is_read` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `fk_notifications_user_id__id` (`user_id`),
  KEY `idx_notifications_is_read` (`is_read`),
  KEY `idx_notifications_type` (`type`),
  CONSTRAINT `fk_notifications_user_id__id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notifications`
--

LOCK TABLES `notifications` WRITE;
/*!40000 ALTER TABLE `notifications` DISABLE KEYS */;
/*!40000 ALTER TABLE `notifications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_item_batches`
--

DROP TABLE IF EXISTS `order_item_batches`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_item_batches` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `order_item_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `batch_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `quantity` int NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_oib_item` (`order_item_id`),
  KEY `idx_oib_batch` (`batch_id`),
  CONSTRAINT `fk_oib_batch` FOREIGN KEY (`batch_id`) REFERENCES `product_batches` (`id`),
  CONSTRAINT `fk_oib_item` FOREIGN KEY (`order_item_id`) REFERENCES `order_items` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_item_batches`
--

LOCK TABLES `order_item_batches` WRITE;
/*!40000 ALTER TABLE `order_item_batches` DISABLE KEYS */;
/*!40000 ALTER TABLE `order_item_batches` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_items`
--

DROP TABLE IF EXISTS `order_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_items` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `order_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `product_id` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `name` varchar(300) COLLATE utf8mb4_unicode_ci NOT NULL,
  `price` decimal(15,0) NOT NULL,
  `quantity` int NOT NULL,
  `unit` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_order_items_order_id__id` (`order_id`),
  KEY `fk_order_items_product_id__id` (`product_id`),
  CONSTRAINT `fk_order_items_order_id__id` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_order_items_product_id__id` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_items`
--

LOCK TABLES `order_items` WRITE;
/*!40000 ALTER TABLE `order_items` DISABLE KEYS */;
/*!40000 ALTER TABLE `order_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `order_code` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `shop_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `address_id` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING|PROCESSING|SHIPPING|DELIVERED|CANCELLED|RETURNED',
  `pickup_type` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'DELIVERY' COMMENT 'DELIVERY|PICKUP',
  `branch_id` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Chi nhánh để pickup',
  `subtotal` decimal(15,0) DEFAULT NULL,
  `shipping_fee` decimal(15,0) DEFAULT '0',
  `discount` decimal(12,2) NOT NULL DEFAULT '0.00',
  `points_used` int NOT NULL DEFAULT '0' COMMENT 'Điểm thưởng được sử dụng',
  `points_earned` int NOT NULL DEFAULT '0' COMMENT 'Điểm thưởng được tích lũy',
  `total` decimal(15,0) DEFAULT NULL,
  `payment_method` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'COD|MOMO|VNPAY|BANK_TRANSFER|E_WALLET',
  `payment_status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'UNPAID' COMMENT 'UNPAID|PENDING|COMPLETED|FAILED',
  `payment_provider` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Nhà cung cấp thanh toán điện tử',
  `note` text COLLATE utf8mb4_unicode_ci,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_order_code` (`order_code`),
  KEY `fk_orders_user_id__id` (`user_id`),
  KEY `fk_orders_shop_id__id` (`shop_id`),
  KEY `fk_orders_address_id__id` (`address_id`),
  KEY `fk_orders_branch_id__id` (`branch_id`),
  KEY `idx_orders_status` (`status`),
  KEY `idx_orders_payment_status` (`payment_status`),
  CONSTRAINT `fk_orders_address_id__id` FOREIGN KEY (`address_id`) REFERENCES `user_addresses` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_orders_branch_id__id` FOREIGN KEY (`branch_id`) REFERENCES `pharmacy_branches` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_orders_shop_id__id` FOREIGN KEY (`shop_id`) REFERENCES `shops` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_orders_user_id__id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payment_methods`
--

DROP TABLE IF EXISTS `payment_methods`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payment_methods` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'MOMO|VNPAY|BANK_TRANSFER|CREDIT_CARD|E_WALLET',
  `provider` varchar(50) COLLATE utf8mb4_unicode_ci COMMENT 'MOMO|VNPAY|ZALOPAY|GPAY|etc',
  `label` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Tên hiển thị (VD: Momo Trương Anh)',
  `last4` varchar(4) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '4 số cuối thẻ/Tài khoản',
  `is_default` tinyint(1) NOT NULL DEFAULT '0',
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `fk_payment_methods_user_id__id` (`user_id`),
  KEY `idx_payment_methods_type` (`type`),
  CONSTRAINT `fk_payment_methods_user_id__id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payment_methods`
--

LOCK TABLES `payment_methods` WRITE;
/*!40000 ALTER TABLE `payment_methods` DISABLE KEYS */;
/*!40000 ALTER TABLE `payment_methods` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payments`
--

DROP TABLE IF EXISTS `payments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payments` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `order_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `payment_method_id` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `method` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'COD|MOMO|VNPAY|BANK_TRANSFER|E_WALLET',
  `provider` varchar(50) COLLATE utf8mb4_unicode_ci COMMENT 'Nhà cung cấp thanh toán',
  `amount` decimal(12,2) NOT NULL,
  `transaction_id` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Mã giao dịch từ cổng thanh toán',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING|COMPLETED|FAILED|CANCELLED',
  `payment_gateway_response` json DEFAULT NULL COMMENT 'Response từ payment gateway (JSON)',
  `paid_at` datetime DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `fk_payments_order_id__id` (`order_id`),
  KEY `fk_payments_payment_method_id__id` (`payment_method_id`),
  KEY `idx_payments_status` (`status`),
  KEY `idx_payments_method` (`method`),
  KEY `idx_payments_transaction_id` (`transaction_id`),
  CONSTRAINT `fk_payments_order_id__id` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_payments_payment_method_id__id` FOREIGN KEY (`payment_method_id`) REFERENCES `payment_methods` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payments`
--

LOCK TABLES `payments` WRITE;
/*!40000 ALTER TABLE `payments` DISABLE KEYS */;
/*!40000 ALTER TABLE `payments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pharmacy_branches`
--

DROP TABLE IF EXISTS `pharmacy_branches`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pharmacy_branches` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `shop_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `address` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `latitude` decimal(10,8) DEFAULT NULL,
  `longitude` decimal(11,8) DEFAULT NULL,
  `phone` varchar(15) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `open_time` varchar(8) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `close_time` varchar(8) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  KEY `fk_pharmacy_branches_shop_id__id` (`shop_id`),
  CONSTRAINT `fk_pharmacy_branches_shop_id__id` FOREIGN KEY (`shop_id`) REFERENCES `shops` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pharmacy_branches`
--

LOCK TABLES `pharmacy_branches` WRITE;
/*!40000 ALTER TABLE `pharmacy_branches` DISABLE KEYS */;
/*!40000 ALTER TABLE `pharmacy_branches` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `prescriptions`
--

DROP TABLE IF EXISTS `prescriptions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `prescriptions` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `order_id` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `image_url` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `note` text COLLATE utf8mb4_unicode_ci,
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `cloudinary_public_id` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_prescriptions_user_id__id` (`user_id`),
  KEY `fk_prescriptions_order_id__id` (`order_id`),
  CONSTRAINT `fk_prescriptions_order_id__id` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_prescriptions_user_id__id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `prescriptions`
--

LOCK TABLES `prescriptions` WRITE;
/*!40000 ALTER TABLE `prescriptions` DISABLE KEYS */;
/*!40000 ALTER TABLE `prescriptions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_attribute_values`
--

DROP TABLE IF EXISTS `product_attribute_values`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_attribute_values` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `product_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `attribute_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `value_text` text COLLATE utf8mb4_unicode_ci,
  `value_number` decimal(18,6) DEFAULT NULL,
  `value_bool` tinyint(1) DEFAULT NULL,
  `value_date` date DEFAULT NULL,
  `value_json` json DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_prod_attr` (`product_id`,`attribute_id`),
  KEY `fk_pav_attribute` (`attribute_id`),
  CONSTRAINT `fk_pav_attribute` FOREIGN KEY (`attribute_id`) REFERENCES `category_attributes` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_pav_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_attribute_values`
--

LOCK TABLES `product_attribute_values` WRITE;
/*!40000 ALTER TABLE `product_attribute_values` DISABLE KEYS */;
/*!40000 ALTER TABLE `product_attribute_values` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_batches`
--

DROP TABLE IF EXISTS `product_batches`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_batches` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `product_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `lot_number` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `mfg_date` date DEFAULT NULL,
  `exp_date` date DEFAULT NULL,
  `quantity_on_hand` int NOT NULL DEFAULT '0',
  `import_price` decimal(12,2) DEFAULT NULL,
  `note` text COLLATE utf8mb4_unicode_ci,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_batches_product` (`product_id`),
  KEY `idx_batches_expiry` (`exp_date`),
  CONSTRAINT `fk_batch_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_batches`
--

LOCK TABLES `product_batches` WRITE;
/*!40000 ALTER TABLE `product_batches` DISABLE KEYS */;
/*!40000 ALTER TABLE `product_batches` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_certificates`
--

DROP TABLE IF EXISTS `product_certificates`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_certificates` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `product_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `file_url` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `issue_date` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `expire_date` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `issuer` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `fk_product_certificates_product_id__id` (`product_id`),
  CONSTRAINT `fk_product_certificates_product_id__id` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_certificates`
--

LOCK TABLES `product_certificates` WRITE;
/*!40000 ALTER TABLE `product_certificates` DISABLE KEYS */;
/*!40000 ALTER TABLE `product_certificates` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_diseases`
--

DROP TABLE IF EXISTS `product_diseases`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_diseases` (
  `product_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `disease_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`product_id`,`disease_id`),
  KEY `fk_product_diseases_disease_id__id` (`disease_id`),
  CONSTRAINT `fk_product_diseases_disease_id__id` FOREIGN KEY (`disease_id`) REFERENCES `disease_categories` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_product_diseases_product_id__id` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_diseases`
--

LOCK TABLES `product_diseases` WRITE;
/*!40000 ALTER TABLE `product_diseases` DISABLE KEYS */;
/*!40000 ALTER TABLE `product_diseases` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_images`
--

DROP TABLE IF EXISTS `product_images`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_images` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `product_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `url` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `sort_order` int NOT NULL DEFAULT '0',
  `media_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'IMAGE' COMMENT 'IMAGE | VIDEO',
  `cloudinary_public_id` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Dùng để xóa file trên Cloudinary',
  `thumbnail_url` text COLLATE utf8mb4_unicode_ci COMMENT 'Ảnh bìa cho video',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `fk_product_images_product_id__id` (`product_id`),
  CONSTRAINT `fk_product_images_product_id__id` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_images`
--

LOCK TABLES `product_images` WRITE;
/*!40000 ALTER TABLE `product_images` DISABLE KEYS */;
/*!40000 ALTER TABLE `product_images` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `products`
--

DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `products` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `shop_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `category_id` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `name` varchar(300) COLLATE utf8mb4_unicode_ci NOT NULL,
  `slug` varchar(300) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `brand` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `origin` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Quốc gia xuất xứ: Việt Nam, Hàn Quốc, v.v.',
  `sku` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `unit` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'Hộp' COMMENT 'Hộp/Viên/Chai/Gói/Cái',
  `price` decimal(15,0) NOT NULL DEFAULT '0',
  `original_price` decimal(15,0) DEFAULT '0',
  `discount_pct` int NOT NULL DEFAULT '0',
  `reward_points` int NOT NULL DEFAULT '0',
  `stock` int NOT NULL DEFAULT '0',
  `product_type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'MEDICINE' COMMENT 'MEDICINE|SUPPLEMENT|MEDICAL_DEVICE|COSMETIC|FOOD|HERB|EQUIPMENT',
  `registration_number` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Số đăng ký lưu hành',
  `is_prescription` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Thuốc kê đơn hay không',
  `requires_consultation` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Cần tư vấn trước khi mua',
  `manufacturer` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Nhà sản xuất',
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `is_flash_sale` tinyint(1) NOT NULL DEFAULT '0',
  `is_best_seller` tinyint(1) NOT NULL DEFAULT '0',
  `flash_sale_end` datetime DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  `deleted_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_products_slug` (`slug`),
  UNIQUE KEY `uq_products_sku` (`sku`),
  KEY `fk_products_shop_id__id` (`shop_id`),
  KEY `fk_products_category_id__id` (`category_id`),
  KEY `idx_products_product_type` (`product_type`),
  KEY `idx_products_is_prescription` (`is_prescription`),
  FULLTEXT KEY `idx_products_search` (`name`,`description`,`brand`),
  CONSTRAINT `fk_products_category_id__id` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_products_shop_id__id` FOREIGN KEY (`shop_id`) REFERENCES `shops` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `products`
--

LOCK TABLES `products` WRITE;
/*!40000 ALTER TABLE `products` DISABLE KEYS */;
/*!40000 ALTER TABLE `products` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `refresh_tokens`
--

DROP TABLE IF EXISTS `refresh_tokens`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `refresh_tokens` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `token` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `expires_at` datetime NOT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `fk_refresh_tokens_user_id__id` (`user_id`),
  CONSTRAINT `fk_refresh_tokens_user_id__id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `refresh_tokens`
--

LOCK TABLES `refresh_tokens` WRITE;
/*!40000 ALTER TABLE `refresh_tokens` DISABLE KEYS */;
/*!40000 ALTER TABLE `refresh_tokens` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reviews`
--

DROP TABLE IF EXISTS `reviews`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reviews` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `product_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `order_id` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `rating` tinyint NOT NULL,
  `comment` text COLLATE utf8mb4_unicode_ci,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_review_order_prod` (`order_id`,`product_id`),
  KEY `fk_reviews_product_id__id` (`product_id`),
  KEY `fk_reviews_user_id__id` (`user_id`),
  CONSTRAINT `fk_reviews_order_id__id` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_reviews_product_id__id` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_reviews_user_id__id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `reviews_chk_1` CHECK ((`rating` between 1 and 5))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reviews`
--

LOCK TABLES `reviews` WRITE;
/*!40000 ALTER TABLE `reviews` DISABLE KEYS */;
/*!40000 ALTER TABLE `reviews` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reward_accounts`
--

DROP TABLE IF EXISTS `reward_accounts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reward_accounts` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `total_points` int NOT NULL DEFAULT '0',
  `used_points` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_reward_user` (`user_id`),
  CONSTRAINT `fk_reward_accounts_user_id__id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reward_accounts`
--

LOCK TABLES `reward_accounts` WRITE;
/*!40000 ALTER TABLE `reward_accounts` DISABLE KEYS */;
/*!40000 ALTER TABLE `reward_accounts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reward_products`
--

DROP TABLE IF EXISTS `reward_products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reward_products` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(300) COLLATE utf8mb4_unicode_ci NOT NULL,
  `image_url` text COLLATE utf8mb4_unicode_ci,
  `point_cost` int NOT NULL,
  `price_text` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `stock` int NOT NULL DEFAULT '0',
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reward_products`
--

LOCK TABLES `reward_products` WRITE;
/*!40000 ALTER TABLE `reward_products` DISABLE KEYS */;
/*!40000 ALTER TABLE `reward_products` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reward_redemptions`
--

DROP TABLE IF EXISTS `reward_redemptions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reward_redemptions` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `reward_product_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `quantity` int NOT NULL DEFAULT '1',
  `points_used` int NOT NULL,
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PROCESSING',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `fk_reward_redemptions_user_id__id` (`user_id`),
  KEY `fk_reward_redemptions_reward_product_id__id` (`reward_product_id`),
  CONSTRAINT `fk_reward_redemptions_reward_product_id__id` FOREIGN KEY (`reward_product_id`) REFERENCES `reward_products` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_reward_redemptions_user_id__id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reward_redemptions`
--

LOCK TABLES `reward_redemptions` WRITE;
/*!40000 ALTER TABLE `reward_redemptions` DISABLE KEYS */;
/*!40000 ALTER TABLE `reward_redemptions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reward_transactions`
--

DROP TABLE IF EXISTS `reward_transactions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reward_transactions` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `order_id` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `points` int NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `fk_reward_transactions_user_id__id` (`user_id`),
  KEY `fk_reward_transactions_order_id__id` (`order_id`),
  CONSTRAINT `fk_reward_transactions_order_id__id` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_reward_transactions_user_id__id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reward_transactions`
--

LOCK TABLES `reward_transactions` WRITE;
/*!40000 ALTER TABLE `reward_transactions` DISABLE KEYS */;
/*!40000 ALTER TABLE `reward_transactions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shops`
--

DROP TABLE IF EXISTS `shops`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shops` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `owner_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `logo_url` text COLLATE utf8mb4_unicode_ci,
  `license_number` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_approved` tinyint(1) NOT NULL DEFAULT '0',
  `expiry_alert_days` int NOT NULL DEFAULT '30',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `deleted_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_shops_owner` (`owner_id`),
  CONSTRAINT `fk_shops_owner_id__id` FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shops`
--

LOCK TABLES `shops` WRITE;
/*!40000 ALTER TABLE `shops` DISABLE KEYS */;
/*!40000 ALTER TABLE `shops` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_addresses`
--

DROP TABLE IF EXISTS `user_addresses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_addresses` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `label` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `recipient_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phone` varchar(15) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `address` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `ward` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `district` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `province` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_default` tinyint(1) NOT NULL DEFAULT '0',
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `fk_user_addresses_user_id__id` (`user_id`),
  CONSTRAINT `fk_user_addresses_user_id__id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_addresses`
--

LOCK TABLES `user_addresses` WRITE;
/*!40000 ALTER TABLE `user_addresses` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_addresses` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone` varchar(15) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `full_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `avatar_url` text COLLATE utf8mb4_unicode_ci,
  `gender` tinyint DEFAULT '1' COMMENT '1:Male, 2:Female, 3:Other',
  `date_of_birth` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `role` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'USER',
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `deleted_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_users_phone` (`phone`),
  UNIQUE KEY `uq_users_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;


--
-- Dumping routines for database 'medical_supplies_db'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- ===============================================
-- SCHEMA DOCUMENTATION
-- Đề tài: "Ứng dụng bán vật tư y tế tích hợp ChatbotAI và thanh toán điện tử"
-- Project: Medical Supplies E-Commerce with AI ChatBot & Electronic Payment
-- ===============================================
--
-- 1. USERS & AUTHENTICATION
--    - users: Lưu thông tin người dùng (3 role: ADMIN|SHOP|USER)
--    - refresh_tokens: Token refresh cho JWT authentication
--
-- 2. SHOPS & BRANCHES (Nhà Thuốc / Cơ Sở Y Tế)
--    - shops: Thông tin nhà thuốc/cơ sở y tế
--    - pharmacy_branches: Chi nhánh của các shop
--
-- 3. PRODUCTS & CATEGORIES (Vật Tư Y Tế)
--    - categories: Danh mục sản phẩm (phân cấp)
--    - category_attributes: Thuộc tính của từng danh mục
--    - products: Sản phẩm bán (MEDICINE, SUPPLEMENT, MEDICAL_DEVICE, COSMETIC, FOOD, HERB, EQUIPMENT)
--    - product_images: Ảnh sản phẩm
--    - product_certificates: Giấy phép, chứng nhận sản phẩm (Registration, Import License, GMP, etc)
--    - product_attribute_values: Giá trị thuộc tính của sản phẩm
--    - product_batches: Quản lý lô/batch sản phẩm (với ngày hết hạn)
--    - disease_categories: Phân loại bệnh lý / Công dụng
--    - product_diseases: Liên kết sản phẩm - bệnh lý
--
-- 4. CART & ORDERS
--    - cart_items: Giỏ hàng
--    - user_addresses: Địa chỉ giao hàng của user
--    - orders: Đơn hàng
--    - order_items: Chi tiết từng item trong đơn
--    - order_item_batches: Liên kết đơn - lô sản phẩm
--    - prescriptions: Đơn thuốc (kê đơn) khi mua thuốc
--
-- 5. PAYMENT & ELECTRONIC PAYMENT (Thanh Toán Điện Tử)
--    - payment_methods: Phương thức thanh toán (MOMO|VNPAY|BANK_TRANSFER|CREDIT_CARD|E_WALLET)
--    - payments: Lịch sử thanh toán với chi tiết từ payment gateway
--    Hỗ trợ các cổng thanh toán: MoMo, VNPay, Zalo Pay, Google Pay, Banking API
--
-- 6. CHATBOT AI & CONVERSATION (ChatBot AI & Tư Vấn)
--    - chat_sessions: Session chat (hỗ trợ HUMAN|AI|HYBRID)
--    - chat_messages: Tin nhắn chat (từ USER|CONSULTANT|AI_BOT)
--    - ai_chatbot_settings: Cấu hình AI ChatBot (provider, model, temperature, system prompt)
--    - ai_conversations: Lưu trữ cuộc trò chuyện AI (history, intent, sentiment, escalation)
--    Tính năng: AI tự động phản hồi, escalation lên Consultant khi cần, sentiment analysis
--
-- 7. REVIEWS & RATINGS
--    - reviews: Đánh giá sản phẩm (1-5 stars)
--
-- 8. REWARD & LOYALTY POINTS (Điểm Thưởng)
--    - reward_accounts: Tài khoản điểm thưởng
--    - reward_transactions: Lịch sử giao dịch điểm
--    - reward_products: Sản phẩm đổi quà
--    - reward_redemptions: Lịch sử đổi quà
--
-- 9. NOTIFICATIONS & BANNERS
--     - notifications: Thông báo hệ thống
--     - banners: Quảng cáo banner
--     - health_articles: Bài viết sức khỏe, tư vấn y tế
--
-- ===============================================
-- Key Features:
-- ✓ Support Medical Supplies (not just medicines)
-- ✓ AI ChatBot with escalation to consultants
-- ✓ Electronic Payment (MOMO, VNPay, Bank Transfer, E-Wallet)
-- ✓ Product batch/expiry tracking
-- ✓ Prescription verification
-- ✓ Reward points system
-- ✓ Multi-shop/branch support
-- ✓ Location-based services
-- ===============================================

-- ===============================================
-- SAMPLE DATA FOR MEDICAL SUPPLIES E-COMMERCE
-- ===============================================

-- Sample Users
INSERT INTO `users` (`id`, `email`, `full_name`, `phone`, `password`, `role`, `is_active`, `created_at`) VALUES
('u-admin-001', 'admin@medstore.vn', 'Admin MedStore', '0123456789', '$2b$10$example.hash.admin', 'ADMIN', 1, NOW()),
('u-shop-001', 'shop@medstore.vn', 'Shop MedStore HCM', '0987654321', '$2b$10$example.hash.shop', 'SHOP', 1, NOW()),
('u-user-001', 'user1@gmail.com', 'Nguyễn Văn A', '0329645776', '$2b$10$example.hash.user', 'USER', 1, NOW()),
('u-user-002', 'user2@gmail.com', 'Trần Thị B', '0329645777', '$2b$10$example.hash.user2', 'USER', 1, NOW());

-- Sample Shops
INSERT INTO `shops` (`id`, `owner_id`, `name`, `description`, `logo_url`, `license_number`, `is_approved`, `created_at`) VALUES
('shop-001', 'u-shop-001', 'MedStore - Vật Tư Y Tế Chuyên Nghiệp', 'Chuyên cung cấp thiết bị và vật tư y tế chất lượng cao cho bệnh viện, phòng khám và cá nhân', 'https://example.com/logo.png', 'SHOP-001-2024', 1, NOW());

-- Sample Pharmacy Branches
INSERT INTO `pharmacy_branches` (`id`, `shop_id`, `name`, `address`, `phone`, `latitude`, `longitude`, `is_active`) VALUES
('branch-001', 'shop-001', 'MedStore Chi nhánh Quận 1', '123 Nguyễn Thái Bình, Q.1, TP.HCM', '028-3822-0001', 10.77698900, 106.70090830, 1),
('branch-002', 'shop-001', 'MedStore Chi nhánh Quận 3', '456 Hai Bà Trưng, Q.3, TP.HCM', '028-3822-0002', 10.78593570, 106.69343650, 1);

-- Sample Categories for Medical Supplies
INSERT INTO `categories` (`id`, `parent_id`, `name`, `slug`, `description`, `product_type_default`, `icon_url`, `sort_order`, `is_active`, `created_at`, `updated_at`) VALUES
('cat-device', NULL, 'Thiết bị y tế', 'thiet-bi-y-te', 'Các loại thiết bị y tế chuyên dụng', 'DEVICE', 'https://example.com/icons/medical-device.png', 1, 1, NOW(), NOW()),
('cat-supplies', NULL, 'Vật tư tiêu hao', 'vat-tu-tieu-hao', 'Vật tư y tế sử dụng một lần', 'DEVICE', 'https://example.com/icons/medical-supplies.png', 2, 1, NOW(), NOW()),
('cat-protect', NULL, 'Đồ bảo hộ y tế', 'do-bao-ho-y-te', 'Thiết bị bảo hộ cá nhân cho ngành y tế', 'DEVICE', 'https://example.com/icons/protection.png', 3, 1, NOW(), NOW()),
('cat-instrument', NULL, 'Dụng cụ y tế', 'dung-cu-y-te', 'Các loại dụng cụ và công cụ y tế', 'DEVICE', 'https://example.com/icons/instrument.png', 4, 1, NOW(), NOW()),

-- Sub-categories for Medical Devices
('cat-monitor', 'cat-device', 'Máy theo dõi', 'may-theo-doi', 'Thiết bị theo dõi sinh hiệu bệnh nhân', 'DEVICE', 'https://example.com/icons/monitor.png', 11, 1, NOW(), NOW()),
('cat-diagnostic', 'cat-device', 'Thiết bị chẩn đoán', 'thiet-bi-chan-doan', 'Thiết bị hỗ trợ chẩn đoán bệnh', 'DEVICE', 'https://example.com/icons/diagnostic.png', 12, 1, NOW(), NOW()),
('cat-therapy', 'cat-device', 'Thiết bị điều trị', 'thiet-bi-dieu-tri', 'Thiết bị hỗ trợ điều trị', 'DEVICE', 'https://example.com/icons/therapy.png', 13, 1, NOW(), NOW()),

-- Sub-categories for Supplies
('cat-syringe', 'cat-supplies', 'Bơm tiêm', 'bom-tiem', 'Các loại bơm tiêm và kim', 'DEVICE', 'https://example.com/icons/syringe.png', 21, 1, NOW(), NOW()),
('cat-bandage', 'cat-supplies', 'Băng gạc', 'bang-gac', 'Băng gạc và vật liệu băng bó', 'DEVICE', 'https://example.com/icons/bandage.png', 22, 1, NOW(), NOW()),
('cat-tube', 'cat-supplies', 'Ống thông', 'ong-thong', 'Các loại ống thông y tế', 'DEVICE', 'https://example.com/icons/tube.png', 23, 1, NOW(), NOW());

-- Sample Products for Medical Supplies
INSERT INTO `products` (`id`, `shop_id`, `name`, `slug`, `brand`, `origin`, `price`, `original_price`, `discount_pct`, `unit`, `sku`, `description`, `product_type`, `registration_number`, `is_prescription`, `requires_consultation`, `is_flash_sale`, `is_best_seller`, `stock`, `is_active`, `created_at`, `updated_at`) VALUES
-- Medical Devices
('prod-001', 'shop-001', 'Máy đo huyết áp điện tử OMRON HEM-7120', 'may-do-huyet-ap-omron-hem-7120', 'OMRON', 'Nhật Bản', 850000, 950000, 11, 'Cái', 'OMR-HEM-7120', 'Máy đo huyết áp bắp tay tự động, màn hình LCD lớn, bộ nhớ 30 lần đo', 'DEVICE', 'MD-001-2024', 0, 1, 1, 1, 50, 1, NOW(), NOW()),
('prod-002', 'shop-001', 'Nhiệt kế hồng ngoại không tiếp xúc BRAUN BNT400', 'nhiet-ke-hong-ngoai-braun-bnt400', 'BRAUN', 'Đức', 1200000, 1350000, 11, 'Cái', 'BRA-BNT400', 'Nhiệt kế hồng ngoại đo trán, độ chính xác cao, màn hình màu', 'DEVICE', 'MD-002-2024', 0, 0, 0, 1, 30, 1, NOW(), NOW()),
('prod-003', 'shop-001', 'Máy xông mũi họng OMRON CompAIR C28P', 'may-xong-mui-hong-omron-compair-c28p', 'OMRON', 'Nhật Bản', 2500000, 2800000, 11, 'Cái', 'OMR-C28P', 'Máy nebulizer piston, tiếng ồn thấp, hiệu quả cao', 'DEVICE', 'MD-003-2024', 0, 1, 0, 0, 15, 1, NOW(), NOW()),

-- Medical Supplies
('prod-004', 'shop-001', 'Bơm tiêm 1ml Terumo', 'bom-tiem-1ml-terumo', 'TERUMO', 'Malaysia', 2500, 3000, 17, 'Cái', 'TER-1ML', 'Bơm tiêm insulin 1ml, vô trùng, cảm biến thấp', 'DEVICE', 'MS-001-2024', 0, 0, 0, 1, 1000, 1, NOW(), NOW()),
('prod-005', 'shop-001', 'Khẩu trang y tế 3 lớp KIMBERLY', 'khau-trang-y-te-3-lop-kimberly', 'KIMBERLY-CLARK', 'Thái Lan', 150000, 180000, 17, 'Hộp 50 cái', 'KIM-3LAY-50', 'Khẩu trang y tế 3 lớp, kháng khuẩn, thông thoáng', 'DEVICE', 'MS-002-2024', 0, 0, 1, 1, 500, 1, NOW(), NOW()),
('prod-006', 'shop-001', 'Găng tay y tế latex không bột ANSELL', 'gang-tay-y-te-latex-ansell', 'ANSELL', 'Malaysia', 220000, 250000, 12, 'Hộp 100 cái', 'ANS-LTX-100', 'Găng tay latex không bột, size M, chống thấm', 'DEVICE', 'MS-003-2024', 0, 0, 0, 1, 300, 1, NOW(), NOW()),

-- Protection Equipment
('prod-007', 'shop-001', 'Kính bảo hộ y tế 3M 2890', 'kinh-bao-ho-y-te-3m-2890', '3M', 'Mỹ', 450000, 500000, 10, 'Cái', '3M-2890', 'Kính bảo hộ chống giọt bắn, chống trầy xước', 'DEVICE', 'PE-001-2024', 0, 0, 0, 0, 100, 1, NOW(), NOW()),
('prod-008', 'shop-001', 'Áo choàng phẫu thuật SMS', 'ao-choang-phau-thuat-sms', 'MEDICOM', 'Canada', 85000, 100000, 15, 'Cái', 'MED-SMS-L', 'Áo choàng phẫu thuật SMS, không dệt, vô trùng, size L', 'DEVICE', 'PE-002-2024', 0, 0, 0, 0, 200, 1, NOW(), NOW());

-- Link Products to Categories
UPDATE `products` SET `category_id` = 'cat-monitor' WHERE `id` = 'prod-001';
UPDATE `products` SET `category_id` = 'cat-diagnostic' WHERE `id` = 'prod-002';
UPDATE `products` SET `category_id` = 'cat-therapy' WHERE `id` = 'prod-003';
UPDATE `products` SET `category_id` = 'cat-syringe' WHERE `id` = 'prod-004';
UPDATE `products` SET `category_id` = 'cat-protect' WHERE `id` = 'prod-005';
UPDATE `products` SET `category_id` = 'cat-protect' WHERE `id` = 'prod-006';
UPDATE `products` SET `category_id` = 'cat-protect' WHERE `id` = 'prod-007';
UPDATE `products` SET `category_id` = 'cat-protect' WHERE `id` = 'prod-008';

-- Sample Product Images
INSERT INTO `product_images` (`id`, `product_id`, `url`, `sort_order`, `created_at`) VALUES
('img-001', 'prod-001', 'https://example.com/products/omron-hem-7120-1.jpg', 1, NOW()),
('img-002', 'prod-001', 'https://example.com/products/omron-hem-7120-2.jpg', 2, NOW()),
('img-003', 'prod-002', 'https://example.com/products/braun-bnt400-1.jpg', 1, NOW()),
('img-004', 'prod-005', 'https://example.com/products/kimberly-mask-1.jpg', 1, NOW());

-- Sample Banners
INSERT INTO `banners` (`id`, `image_url`, `link_url`, `title`, `description`, `sort_order`, `is_active`, `start_dt`, `end_dt`, `created_at`) VALUES
('banner-001', 'https://example.com/banners/medical-supplies-sale.jpg', '/categories/thiet-bi-y-te', 'Khuyến mãi thiết bị y tế', 'Giảm giá đến 20% cho tất cả thiết bị y tế chính hãng', 1, 1, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), NOW()),
('banner-002', 'https://example.com/banners/chatbot-ai.jpg', '/chat', 'Tư vấn với AI ChatBot', 'Nhận tư vấn miễn phí từ AI ChatBot 24/7', 2, 1, NOW(), DATE_ADD(NOW(), INTERVAL 60 DAY), NOW()),
('banner-003', 'https://example.com/banners/electronic-payment.jpg', '/payment-methods', 'Thanh toán điện tử tiện lợi', 'Hỗ trợ MOMO, VNPay, ZaloPay và chuyển khoản ngân hàng', 3, 1, NOW(), DATE_ADD(NOW(), INTERVAL 90 DAY), NOW());

-- Sample Disease Categories for Health Content
INSERT INTO `disease_categories` (`id`, `name`, `description`, `icon_url`, `sort_order`, `is_active`, `created_at`, `updated_at`) VALUES
('disease-001', 'Tim mạch', 'Các bệnh lý về tim và mạch máu', 'https://example.com/icons/heart.png', 1, 1, NOW(), NOW()),
('disease-002', 'Hô hấp', 'Các bệnh lý về đường hô hấp', 'https://example.com/icons/lungs.png', 2, 1, NOW(), NOW()),
('disease-003', 'Tiểu đường', 'Quản lý và theo dõi bệnh tiểu đường', 'https://example.com/icons/diabetes.png', 3, 1, NOW(), NOW());

-- Sample Health Articles
INSERT INTO `health_articles` (`id`, `title`, `content`, `author`, `thumbnail_url`, `category`, `is_published`, `published_at`, `created_at`, `updated_at`) VALUES
('article-001', 'Hướng dẫn sử dụng máy đo huyết áp tại nhà', 'Chi tiết cách sử dụng máy đo huyết áp điện tử đúng cách để có kết quả chính xác...', 'BS. Nguyễn Văn C', 'https://example.com/articles/blood-pressure-guide.jpg', 'Tim mạch', 1, NOW(), NOW(), NOW()),
('article-002', 'Cách chọn khẩu trang y tế phù hợp', 'Hướng dẫn lựa chọn loại khẩu trang phù hợp cho từng mục đích sử dụng...', 'BS. Trần Thị D', 'https://example.com/articles/mask-guide.jpg', 'Hô hấp', 1, NOW(), NOW(), NOW());

-- Sample AI ChatBot Settings
INSERT INTO `ai_chatbot_settings` (`id`, `shop_id`, `ai_provider`, `model_name`, `temperature`, `max_tokens`, `system_prompt`, `is_active`, `created_at`, `updated_at`) VALUES
('ai-001', 'shop-001', 'openai', 'gpt-4', 0.7, 1000, 'Bạn là trợ lý AI chuyên về vật tư y tế. Hãy tư vấn cho khách hàng về các sản phẩm thiết bị y tế, cách sử dụng và lưu ý an toàn. Khi gặp câu hỏi phức tạp về y tế, hãy khuyên khách hàng nên tham khảo ý kiến bác sĩ.', 1, NOW(), NOW());

-- Sample Notifications
INSERT INTO `notifications` (`id`, `user_id`, `title`, `body`, `type`, `ref_id`, `is_read`, `created_at`) VALUES
('notif-001', 'u-user-001', 'Chào mừng đến MedStore', 'Cảm ơn bạn đã đăng ký tài khoản. Hãy khám phá các sản phẩm vật tư y tế chất lượng cao.', 'SYSTEM', NULL, 0, NOW()),
('notif-002', 'u-user-001', 'Khuyến mãi đặc biệt', 'Giảm giá 20% cho tất cả thiết bị y tế trong tháng này!', 'PROMOTION', 'banner-001', 0, NOW()),
('notif-003', 'u-user-002', 'Tích điểm thưởng', 'Bạn đã nhận được 100 điểm thưởng từ đơn hàng gần nhất.', 'REWARD', NULL, 0, NOW());

-- Sample Reward Accounts
INSERT INTO `reward_accounts` (`id`, `user_id`, `total_points`, `used_points`) VALUES
('reward-001', 'u-user-001', 500, 254),
('reward-002', 'u-user-002', 200, 50);

-- Sample Reward Products
INSERT INTO `reward_products` (`id`, `name`, `image_url`, `point_cost`, `price_text`, `stock`, `is_active`) VALUES
('rp-001', 'Voucher giảm giá 50K', 'https://example.com/rewards/voucher-50k.jpg', 200, '50.000₫', 100, 1),
('rp-002', 'Nhiệt kế miễn phí', 'https://example.com/rewards/thermometer.jpg', 500, 'Miễn phí', 20, 1),
('rp-003', 'Khẩu trang N95 (hộp 20 cái)', 'https://example.com/rewards/n95-mask.jpg', 800, 'Miễn phí', 50, 1);

-- Dump completed on 2026-04-01 - Medical Supplies E-Commerce Database with AI Chatbot & Electronic Payment
