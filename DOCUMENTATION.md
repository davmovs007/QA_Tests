# QA Automation TestNG Training API Specification & Assignment Guide

Welcome to the **QA Automation TestNG Training API**. This document defines how students import the provided JAR, architectural specifications, method contracts, error handling conditions, and recommended test cases for the library.

---

## Table of Contents
1. [Library Overview](#1-library-overview)
2. [How to Import the JAR in Maven](#2-how-to-import-the-jar-in-maven)
   - [Method A: Install to Local Maven Cache (Recommended)](#method-a-install-to-local-maven-cache-recommended)
   - [Method B: Project `libs` Directory with System Scope (No terminal command)](#method-b-project-libs-directory-with-system-scope-no-terminal-command)
   - [How to Use the Javadoc Bundle](#how-to-use-the-javadoc-bundle)
   - [Complete Student `pom.xml` Template](#complete-student-pomxml-template)
3. [Data Models & Enums](#3-data-models--enums)
4. [Domain Exceptions](#4-domain-exceptions)
5. [Utility Modules (Functions 1 – 31)](#5-utility-modules)
   - [ValidationUtils (9 functions)](#51-validationutils)
   - [StringUtils (7 functions)](#52-stringutils)
   - [MathAndFinanceUtils (8 functions)](#53-mathandfinanceutils)
   - [DateTimeUtils (7 functions)](#54-datetimeutils)
6. [Business Services (Functions 32 – 87)](#6-business-services)
   - [UserService (11 functions)](#61-userservice)
   - [ProductCatalogService (10 functions)](#62-productcatalogservice)
   - [InventoryService (8 functions)](#63-inventoryservice)
   - [ShoppingCartService (11 functions)](#64-shoppingcartservice)
   - [OrderService (9 functions)](#65-orderservice)
   - [LoyaltyService (8 functions)](#66-loyaltyservice)
7. [Suggested 85+ Test Scenarios Matrix](#7-suggested-85-test-scenarios-matrix)
8. [TestNG Implementation Guide for Students](#8-testng-implementation-guide-for-students)

---

## 1. Library Overview

The library represents an enterprise e-commerce backend engine, user identity provider, and quantitative finance calculator. It is built as a self-contained Java library (compiled with Java 17 bytecode for universal compatibility).

Students receive `testingTask-1.0.0.jar` and write tests using **TestNG 7.x**. All methods enforce strong validation and business rules.

---

## 2. How to Import the JAR in Maven

When students receive `testingTask-1.0.0.jar`, choose **Method A** or **Method B**:

### Method A: Install to Local Maven Cache (Recommended)

This is the standard approach in Java and QA automation. It installs the JAR once into the student's local `~/.m2` repository so Maven can resolve it like any official library.

1. The student opens a terminal/command prompt in the directory where `testingTask-1.0.0.jar` and `testingTask-1.0.0-javadoc.jar` were saved.
2. Run the following command:
   ```bash
   mvn install:install-file \
     -Dfile=testingTask-1.0.0.jar \
     -Djavadoc=testingTask-1.0.0-javadoc.jar \
     -DgroupId=am.gortsup \
     -DartifactId=testingTask \
     -Dversion=1.0.0 \
     -Dpackaging=jar \
     -DgeneratePom=true
   ```
   *(On Windows PowerShell, run the command on a single line without the `\` line breaks)*.

3. In the student's test project `pom.xml`, add the dependency:
   ```xml
   <dependency>
       <groupId>am.gortsup</groupId>
       <artifactId>testingTask</artifactId>
       <version>1.0.0</version>
   </dependency>
   ```

---

### Method B: Project `libs` Directory with System Scope (No terminal command)

If students do not want to run terminal commands, they can include the JAR directly inside their project folder:

1. Inside the student's project root directory, create a folder named `libs`.
2. Copy `testingTask-1.0.0.jar` into `<student-project>/libs/testingTask-1.0.0.jar`.
3. In the student's `pom.xml`, declare the dependency with `<scope>system</scope>`:
   ```xml
   <dependency>
       <groupId>am.gortsup</groupId>
       <artifactId>testingTask</artifactId>
       <version>1.0.0</version>
       <scope>system</scope>
       <systemPath>${project.basedir}/libs/testingTask-1.0.0.jar</systemPath>
   </dependency>
   ```
4. Reload/Sync Maven in IntelliJ IDEA or Eclipse.

---

### How to Use the Javadoc Bundle

The file `testingTask-1.0.0-javadoc.jar` contains full API documentation. Students can use it in two ways:

1. **In-IDE Quick Documentation (IntelliJ IDEA / Eclipse)**:
   - When installed using `-Djavadoc=testingTask-1.0.0-javadoc.jar` in Method A, IDEs automatically render full tooltips and method specifications on hover.
   - Shortcut in IntelliJ: `F1` (macOS) or `Ctrl + Q` (Windows) on any class/method name.
   - Shortcut in Eclipse: `F2` or `Shift + F2`.

2. **Offline HTML Website in Browser**:
   - Extract `testingTask-1.0.0-javadoc.jar` (e.g. `unzip testingTask-1.0.0-javadoc.jar -d javadoc` or rename to `.zip` and extract).
   - Double-click `javadoc/index.html` to open the full searchable Javadoc web portal in any browser.

---

### Complete Student `pom.xml` Template

Students can use this ready-to-run `pom.xml` template for their testing assignment:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>qa.course</groupId>
    <artifactId>student-testng-assignment</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <testng.version>7.10.2</testng.version>
    </properties>

    <dependencies>
        <!-- The QA Training API Library (imported via Method A or B) -->
        <dependency>
            <groupId>am.gortsup</groupId>
            <artifactId>testingTask</artifactId>
            <version>1.0.0</version>
            <!-- Uncomment line below if using Method B (libs folder) -->
            <!-- <scope>system</scope><systemPath>${project.basedir}/libs/testingTask-1.0.0.jar</systemPath> -->
        </dependency>

        <!-- TestNG Framework -->
        <dependency>
            <groupId>org.testng</groupId>
            <artifactId>testng</artifactId>
            <version>${testng.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- Maven Surefire Plugin to execute TestNG suites -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.2.5</version>
                <configuration>
                    <!-- Uncomment to run testng.xml suite if present -->
                    <!-- <suiteXmlFiles><suiteXmlFile>testng.xml</suiteXmlFile></suiteXmlFiles> -->
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## 3. Data Models & Enums

### Models (`am.gortsup.testing.model.*`)
- **`User`**: `id` (String), `username` (String), `email` (String), `password` (String), `role` (Role), `active` (boolean), `locked` (boolean), `failedLoginAttempts` (int), `createdAt` (LocalDateTime).
- **`Role`**: Enum with values `GUEST`, `CUSTOMER`, `MANAGER`, `ADMIN`.
- **`Product`**: `id` (String), `name` (String), `category` (ProductCategory), `price` (double), `active` (boolean).
- **`ProductCategory`**: Enum with values `ELECTRONICS`, `BOOKS`, `CLOTHING`, `HOME`, `BEAUTY`, `GROCERY`.
- **`CartItem`**: `product` (Product), `quantity` (int), `getSubtotal()` (double).
- **`Coupon`**: `code` (String), `discountType` (DiscountType), `discountValue` (double), `minOrderAmount` (double), `expiryDate` (LocalDate), `usageLimit` (int), `timesUsed` (int).
- **`DiscountType`**: Enum with values `PERCENTAGE`, `FIXED_AMOUNT`.
- **`Address`**: `street` (String), `city` (String), `state` (String), `postalCode` (String), `country` (String).
- **`Order`**: `id` (String), `userId` (String), `items` (List<CartItem>), `subtotal` (double), `discount` (double), `tax` (double), `total` (double), `status` (OrderStatus), `shippingAddress` (Address), `trackingNumber` (String).
- **`OrderStatus`**: Enum with values `PENDING`, `PAID`, `PROCESSING`, `SHIPPED`, `DELIVERED`, `CANCELLED`, `REFUNDED`.
- **`LoyaltyTier`**: Enum with values `BRONZE`, `SILVER`, `GOLD`, `PLATINUM`.

---

## 4. Domain Exceptions (`am.gortsup.testing.exception.*`)

All custom exceptions inherit from `RuntimeException`:
- `ValidationException`: Input parameters fail boundary, syntax, or uniqueness checks.
- `AuthenticationException`: Invalid credentials, inactive account, or password mismatch.
- `AccountLockedException`: Account has been locked (e.g. 3 consecutive failed login attempts).
- `ResourceNotFoundException`: Requested user, product, cart, or order not found.
- `InsufficientStockException`: Stock reservation or deduction exceeds available inventory.
- `InvalidCouponException`: Coupon is expired, usage limit reached, or cart value < minimum order amount.
- `IllegalOrderStateException`: State transitions violate lifecycle (e.g., shipping unpaid order, cancelling delivered order).
- `PaymentFailedException`: Luhn checksum invalid or payment amount mismatch.

---

## 5. Utility Modules

### 5.1. ValidationUtils (`am.gortsup.testing.util.ValidationUtils`)

| # | Method Signature | Specification & Rules | Exceptions |
|---|---|---|---|
| 1 | `boolean isValidEmail(String email)` | Validates standard email addresses. Must not be null or blank, max length 254 chars, valid username and domain parts, no consecutive dots. | None |
| 2 | `boolean isValidPassword(String password)` | Validates password complexity: length 8–64, at least 1 uppercase, 1 lowercase, 1 digit, 1 special character (`!@#$%^&*()-_+=[]{}|;:',.<>?/`), no whitespace. | None |
| 3 | `boolean isValidPhoneNumber(String phone)` | Validates phone numbers: digits count between 7 and 15 (E.164), optional leading `+`, allows spaces, dashes, dots, parentheses. | None |
| 4 | `boolean isValidPostalCode(String postalCode, String countryCode)` | Validates postal code by country: `"US"` (5 digits or 5+4), `"UK"`/`"GB"` (standard postcode), `"AM"` (4 digits), `"CA"` (A1A 1A1), other countries (3–10 alphanumeric). | None |
| 5 | `boolean isValidIban(String iban)` | Validates International Bank Account Number using ISO 7064 mod-97 check. | None |
| 6 | `boolean isValidIpAddress(String ip)` | Validates IPv4 address (4 octets 0–255, no leading zero unless single '0'). | None |
| 7 | `boolean isValidUrl(String url)` | Validates well-formed HTTP/HTTPS URL. | None |
| 8 | `String sanitizeInput(String input)` | XSS sanitizer: replaces `<`, `>`, `&`, `"`, `'` with HTML entities. Returns `""` if null. | None |
| 9 | `boolean isPositiveNumber(Number number)` | Returns `true` if number != null and > 0.0. | None |

### 5.2. StringUtils (`am.gortsup.testing.util.StringUtils`)

| # | Method Signature | Specification & Rules | Exceptions |
|---|---|---|---|
| 10 | `String toSlug(String text)` | Converts text to URL slug: lowercase, removes diacritics/accents, replaces spaces with hyphens, collapses multiple hyphens, strips non-alphanumerics. | None |
| 11 | `String capitalizeWords(String input)` | Capitalizes first letter of each word and lowers remaining letters. E.g. `"jOhN dOE"` -> `"John Doe"`. | None |
| 12 | `String truncate(String text, int maxLength, String suffix)` | Truncates text so that total length including suffix does not exceed `maxLength`. | `IllegalArgumentException` if `maxLength < suffix.length()` |
| 13 | `int countOccurrences(String text, String sub)` | Counts non-overlapping occurrences of `sub` in `text` (case-sensitive). Returns 0 if null/empty. | None |
| 14 | `boolean isPalindrome(String text)` | Checks if text is palindrome, ignoring case, spaces, and punctuation. E.g. `"A man, a plan, a canal: Panama"` -> `true`. | None |
| 15 | `String maskCreditCard(String cardNumber)` | Masks all digits except last 4 with `*`. Strips non-digits first. | None |
| 16 | `String reverseWords(String sentence)` | Reverses words in a sentence preserving single spaces. E.g. `"a b c"` -> `"c b a"`. | None |

### 5.3. MathAndFinanceUtils (`am.gortsup.testing.util.MathAndFinanceUtils`)

| # | Method Signature | Specification & Rules | Exceptions |
|---|---|---|---|
| 17 | `double calculateTax(double amount, double taxRatePercentage)` | Calculates tax: `amount * (taxRate / 100)` rounded to 2 decimals. | `IllegalArgumentException` if amount < 0 or taxRate not in [0, 100] |
| 18 | `double calculateDiscount(double amount, double discountPercentage)` | Calculates discounted price: `amount * (1 - discount / 100)` rounded to 2 decimals. | `IllegalArgumentException` if amount < 0 or discount not in [0, 100] |
| 19 | `double roundToDecimalPlaces(double value, int places)` | Rounds value using `RoundingMode.HALF_UP`. | `IllegalArgumentException` if places < 0 |
| 20 | `boolean isValidLuhn(String cardNumber)` | Validates credit card number digits (length 8 to 19) via Luhn Mod-10 algorithm. | None |
| 21 | `double convertCurrency(double amount, double exchangeRate)` | Converts currency: `amount * exchangeRate` rounded to 2 decimals. | `IllegalArgumentException` if amount < 0 or exchangeRate <= 0 |
| 22 | `double calculateCompoundInterest(double principal, double annualRate, int timesCompoundedPerYear, int years)` | Calculates future value: `P * (1 + r/n)^(n*t)` rounded to 2 decimals. | `IllegalArgumentException` if arguments invalid |
| 23 | `double calculateMonthlyInstallment(double principal, double annualInterestRate, int months)` | Standard fixed-rate loan amortization calculation. | `IllegalArgumentException` if principal <= 0 or months < 1 |
| 24 | `double calculatePercentageChange(double oldValue, double newValue)` | Calculates `((newValue - oldValue) / abs(oldValue)) * 100` rounded to 2 decimals. | `IllegalArgumentException` if oldValue == 0 |

### 5.4. DateTimeUtils (`am.gortsup.testing.util.DateTimeUtils`)

| # | Method Signature | Specification & Rules | Exceptions |
|---|---|---|---|
| 25 | `boolean isLeapYear(int year)` | Gregorian leap year check: divisible by 4, not 100 unless also divisible by 400. | None |
| 26 | `long calculateDaysBetween(LocalDate start, LocalDate end)` | Calculates days between dates. | `IllegalArgumentException` if either date is null |
| 27 | `boolean isWeekend(LocalDate date)` | Checks if date is Saturday or Sunday. | `IllegalArgumentException` if date is null |
| 28 | `int calculateBusinessDaysBetween(LocalDate start, LocalDate end)` | Counts Monday–Friday business days between dates [start, end). Returns 0 if start >= end. | `IllegalArgumentException` if either date is null |
| 29 | `LocalDate addBusinessDays(LocalDate start, int days)` | Adds business days skipping weekends. | `IllegalArgumentException` if start null or days < 0 |
| 30 | `boolean doIntervalsOverlap(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2)` | Checks if closed intervals [start1, end1] and [start2, end2] overlap. | `IllegalArgumentException` if null or start > end |
| 31 | `boolean isDateInRange(LocalDate date, LocalDate min, LocalDate max)` | Checks if target date is in range [min, max] inclusive. | `IllegalArgumentException` if null or min > max |

---

## 6. Business Services

### 6.1. UserService (`am.gortsup.testing.service.UserService`)

| # | Method Signature | Specification & Rules | Exceptions |
|---|---|---|---|
| 32 | `User registerUser(String username, String email, String password, Role role)` | Registers user. Enforces valid username (3-30 alphanumeric/_), valid email, strong password, unique username, and unique email. Defaults to `CUSTOMER` if role is null. | `ValidationException` |
| 33 | `User authenticate(String username, String password)` | Validates credentials. On 3rd failed attempt locks account. | `ValidationException`, `AuthenticationException`, `AccountLockedException` |
| 34 | `void changePassword(String userId, String oldPassword, String newPassword)` | Verifies old password, verifies new password strength, rejects identical passwords. | `ResourceNotFoundException`, `AuthenticationException`, `ValidationException` |
| 35 | `void lockUser(String userId)` | Sets user's `locked` flag to `true`. | `ResourceNotFoundException` |
| 36 | `void unlockUser(String userId)` | Sets `locked` to `false` and resets failed login attempts to 0. | `ResourceNotFoundException` |
| 37 | `User getUserById(String userId)` | Retrieves user by ID. | `ResourceNotFoundException` |
| 38 | `User getUserByUsername(String username)` | Retrieves user by username (case-insensitive). | `ResourceNotFoundException` |
| 39 | `void updateEmail(String userId, String newEmail)` | Validates format and uniqueness before updating email. | `ResourceNotFoundException`, `ValidationException` |
| 40 | `boolean hasPermission(String userId, String permission)` | Role check: ADMIN (all permissions), MANAGER (`READ`, `WRITE`, `MANAGE_INVENTORY`, `MANAGE_ORDERS`), CUSTOMER (`READ`, `CREATE_ORDER`, `VIEW_OWN_PROFILE`), GUEST (`READ`). | `ResourceNotFoundException` |
| 41 | `List<User> getAllUsers()` | Returns unmodifiable list of all registered users. | None |
| 42 | `void deactivateUser(String userId)` | Sets active = `false`. Inactive users cannot authenticate. | `ResourceNotFoundException` |

### 6.2. ProductCatalogService (`am.gortsup.testing.service.ProductCatalogService`)

| # | Method Signature | Specification & Rules | Exceptions |
|---|---|---|---|
| 43 | `Product addProduct(Product product)` | Adds product: non-empty ID and name, non-null category, non-negative price, unique ID. | `ValidationException` |
| 44 | `Product getProductById(String id)` | Retrieves product by ID. | `ResourceNotFoundException` |
| 45 | `void updateProductPrice(String id, double newPrice)` | Updates price rounded to 2 decimals. Must be non-negative. | `ResourceNotFoundException`, `ValidationException` |
| 46 | `List<Product> getProductsByCategory(ProductCategory category)` | Returns active products belonging to the category. | None |
| 47 | `List<Product> searchProductsByName(String query)` | Partial case-insensitive search across active products. | None |
| 48 | `void deactivateProduct(String id)` | Deactivates product (hidden from searches). | `ResourceNotFoundException` |
| 49 | `void activateProduct(String id)` | Re-activates product. | `ResourceNotFoundException` |
| 50 | `List<Product> getProductsInPriceRange(double minPrice, double maxPrice)` | Returns active products with price between `minPrice` and `maxPrice`. | `ValidationException` |
| 51 | `int applyCategoryDiscount(ProductCategory category, double discountPercentage)` | Applies discount percentage (0–100) to all products in category. Returns affected count. | `ValidationException` |
| 52 | `List<Product> getAllActiveProducts()` | Returns list of all active products. | None |

### 6.3. InventoryService (`am.gortsup.testing.service.InventoryService`)

| # | Method Signature | Specification & Rules | Exceptions |
|---|---|---|---|
| 53 | `void setStock(String productId, int quantity)` | Sets available stock count (must be >= 0). | `ValidationException` |
| 54 | `int getStock(String productId)` | Returns available stock (0 if unlisted). | None |
| 55 | `int getReservedStock(String productId)` | Returns currently reserved stock. | None |
| 56 | `boolean isInStock(String productId, int requestedQuantity)` | Returns `true` if available stock >= `requestedQuantity`. | None |
| 57 | `void reserveStock(String productId, int quantity)` | Moves quantity from available to reserved. | `ValidationException`, `InsufficientStockException` |
| 58 | `void releaseStock(String productId, int quantity)` | Releases reserved stock back to available stock. | `ValidationException` |
| 59 | `void deductStock(String productId, int quantity)` | Permanently removes quantity from reserved stock. | `ValidationException` |
| 60 | `void restock(String productId, int quantity)` | Adds units to available stock. | `ValidationException` |

### 6.4. ShoppingCartService (`am.gortsup.testing.service.ShoppingCartService`)

| # | Method Signature | Specification & Rules | Exceptions |
|---|---|---|---|
| 61 | `void addItem(String cartId, Product product, int quantity)` | Adds item to cart (qty 1–99). Increments if item exists. | `ValidationException` |
| 62 | `void removeItem(String cartId, String productId)` | Removes product item completely. | `ResourceNotFoundException`, `ValidationException` |
| 63 | `void updateQuantity(String cartId, String productId, int newQuantity)` | Updates item quantity (1–99). | `ResourceNotFoundException`, `ValidationException` |
| 64 | `void applyCoupon(String cartId, Coupon coupon, LocalDate currentDate)` | Validates expiry date, usage limit, and minimum order amount before attaching coupon. | `ValidationException`, `InvalidCouponException` |
| 65 | `void removeCoupon(String cartId)` | Removes currently attached coupon. | `ValidationException` |
| 66 | `double calculateSubtotal(String cartId)` | Computes sum of item prices * quantities. | `ValidationException` |
| 67 | `double calculateDiscountAmount(String cartId)` | Computes coupon discount amount (percentage or fixed). | `ValidationException` |
| 68 | `double calculateTotal(String cartId, double taxRatePercentage)` | Formula: `(subtotal - discount) + tax`. | `ValidationException` |
| 69 | `void clearCart(String cartId)` | Clears all items and applied coupon. | `ValidationException` |
| 70 | `List<CartItem> getCartItems(String cartId)` | Returns unmodifiable list of items in the cart. | `ValidationException` |
| 71 | `int getItemCount(String cartId)` | Returns sum of all item quantities in the cart. | `ValidationException` |

### 6.5. OrderService (`am.gortsup.testing.service.OrderService`)

| # | Method Signature | Specification & Rules | Exceptions |
|---|---|---|---|
| 72 | `Order createOrder(...)` | Creates order in `PENDING` state with items, address, subtotal, discount, and tax. | `ValidationException` |
| 73 | `void payOrder(String orderId, String cardNumber, double amount)` | Validates order is `PENDING`, verifies card via Luhn Mod-10 algorithm, checks exact amount match, updates status to `PAID`. | `ResourceNotFoundException`, `IllegalOrderStateException`, `PaymentFailedException` |
| 74 | `void shipOrder(String orderId, String trackingNumber)` | Validates order is `PAID`/`PROCESSING`, sets tracking number, transitions to `SHIPPED`. Unpaid orders cannot ship. | `ResourceNotFoundException`, `IllegalOrderStateException`, `ValidationException` |
| 75 | `void deliverOrder(String orderId)` | Transitions `SHIPPED` order to `DELIVERED`. | `ResourceNotFoundException`, `IllegalOrderStateException` |
| 76 | `cancelOrder(String orderId)` | Cancels order. Only orders in `PENDING` or `PAID` can be cancelled. Once shipped or delivered, cancellation is forbidden. | `ResourceNotFoundException`, `IllegalOrderStateException` |
| 77 | `refundOrder(String orderId, double refundAmount)` | Issues refund for paid order. `refundAmount` must be > 0 and <= total. Unpaid orders cannot be refunded. | `ResourceNotFoundException`, `IllegalOrderStateException`, `ValidationException` |
| 78 | `Order getOrder(String orderId)` | Retrieves order by ID. | `ResourceNotFoundException` |
| 79 | `List<Order> getOrdersByUser(String userId)` | Retrieves all orders placed by user. | None |
| 80 | `List<Order> getOrdersByStatus(OrderStatus status)` | Retrieves all orders filtered by status. | None |

### 6.6. LoyaltyService (`am.gortsup.testing.service.LoyaltyService`)

| # | Method Signature | Specification & Rules | Exceptions |
|---|---|---|---|
| 81 | `void addPoints(String userId, int pointsEarned)` | Adds points to current balance and lifetime total (must be > 0). | `ValidationException` |
| 82 | `void redeemPoints(String userId, int pointsToRedeem)` | Deducts points from current balance without altering lifetime points. | `ValidationException` if points <= 0 or balance insufficient |
| 83 | `int getPoints(String userId)` | Returns current spendable points balance. | None |
| 84 | `int getLifetimePoints(String userId)` | Returns cumulative lifetime points. | None |
| 85 | `LoyaltyTier calculateTier(int totalLifetimePoints)` | Tiers: 0–999 `BRONZE`, 1000–4999 `SILVER`, 5000–9999 `GOLD`, 10000+ `PLATINUM`. | `ValidationException` if negative |
| 86 | `LoyaltyTier getUserTier(String userId)` | Returns user's tier based on lifetime points. | None |
| 87 | `double getTierDiscountPercentage(LoyaltyTier tier)` | Discounts: `BRONZE` (0%), `SILVER` (5%), `GOLD` (10%), `PLATINUM` (15%). | None |
| 88 | `void transferPoints(String fromUserId, String toUserId, int points)` | Transfers points between distinct users. | `ValidationException` if self-transfer or insufficient balance |

---

## 7. Suggested 85+ Test Scenarios Matrix

Students are expected to implement a comprehensive test suite (at least 70+ test methods) covering the scenarios outlined below:

### Test Category Breakdown

| Test Area | Recommended Tests | TestNG Features Applied |
|---|---|---|
| **Input Validation** | 15 tests | `@DataProvider`, `@Test(groups = "validation")` |
| **String & Text Processing** | 10 tests | `@Test(groups = "string")`, `Assert.assertEquals` |
| **Financial & Math** | 10 tests | `@DataProvider`, `@Test(expectedExceptions = ...)` |
| **Date & Business Days** | 8 tests | `@DataProvider`, Interval boundaries |
| **User Identity & Security** | 12 tests | Lockout on 3 attempts, `@BeforeMethod`, Permissions |
| **Product Catalog** | 8 tests | Filter boundaries, category discount, activate/deactivate |
| **Inventory Management** | 8 tests | Concurrency/atomic reserve, release, `InsufficientStockException` |
| **Shopping Cart & Coupons** | 10 tests | Percentage & flat discounts, expiration dates, limits |
| **Order State Machine** | 12 tests | Lifecycle sequence (`dependsOnMethods`), Luhn payment validation |
| **Loyalty & Rewards** | 8 tests | Boundary value analysis (999 vs 1000, 4999 vs 5000), Point transfer |

---

## 8. TestNG Implementation Guide for Students

### Example Test Class Structure

```java
package student.tests;

import am.gortsup.testing.model.*;
import am.gortsup.testing.service.*;
import am.gortsup.testing.exception.*;
import org.testng.Assert;
import org.testng.annotations.*;

public class OrderLifecycleTest {

    private OrderService orderService;
    private Address address;
    private List<CartItem> items;

    @BeforeMethod
    public void setUp() {
        orderService = new OrderService();
        address = new Address("45 Abovyan St", "Yerevan", "Kentron", "0001", "AM");
        Product product = new Product("P-1", "Mechanical Keyboard", ProductCategory.ELECTRONICS, 120.0);
        items = List.of(new CartItem(product, 1));
    }

    @Test(groups = {"smoke", "order"})
    public void testCreatePendingOrder() {
        Order order = orderService.createOrder("user-100", items, address, 20.0, null);
        Assert.assertNotNull(order.getId());
        Assert.assertEquals(order.getStatus(), OrderStatus.PENDING);
        Assert.assertEquals(order.getTotal(), 144.0); // 120 + 20% tax
    }

    @Test(groups = {"regression", "order"})
    public void testOrderPaymentWithValidCard() {
        Order order = orderService.createOrder("user-101", items, address, 0.0, null);
        orderService.payOrder(order.getId(), "49927398716", 120.0);
        Assert.assertEquals(orderService.getOrder(order.getId()).getStatus(), OrderStatus.PAID);
    }

    @Test(expectedExceptions = PaymentFailedException.class, groups = {"negative", "order"})
    public void testOrderPaymentWithInvalidLuhnCard() {
        Order order = orderService.createOrder("user-102", items, address, 0.0, null);
        orderService.payOrder(order.getId(), "1234567812345678", 120.0);
    }

    @Test(expectedExceptions = IllegalOrderStateException.class, groups = {"negative", "order"})
    public void testCannotShipUnpaidOrder() {
        Order order = orderService.createOrder("user-103", items, address, 0.0, null);
        orderService.shipOrder(order.getId(), "TRACK-XYZ");
    }
}
```

### Running Tests with TestNG XML Suite

Create a `testng.xml` file in your test project:
```xml
<!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd" >
<suite name="QA Training TestNG Suite" verbose="1">
    <test name="All Tests">
        <packages>
            <package name="student.tests.*"/>
        </packages>
    </test>
</suite>
```
