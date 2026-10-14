package student.tests;

import am.gortsup.testing.exception.AccountLockedException;
import am.gortsup.testing.exception.AuthenticationException;
import am.gortsup.testing.exception.IllegalOrderStateException;
import am.gortsup.testing.exception.InsufficientStockException;
import am.gortsup.testing.exception.InvalidCouponException;
import am.gortsup.testing.exception.PaymentFailedException;
import am.gortsup.testing.exception.ResourceNotFoundException;
import am.gortsup.testing.exception.ValidationException;
import am.gortsup.testing.model.Address;
import am.gortsup.testing.model.CartItem;
import am.gortsup.testing.model.Coupon;
import am.gortsup.testing.model.DiscountType;
import am.gortsup.testing.model.LoyaltyTier;
import am.gortsup.testing.model.Order;
import am.gortsup.testing.model.OrderStatus;
import am.gortsup.testing.model.Product;
import am.gortsup.testing.model.ProductCategory;
import am.gortsup.testing.model.Role;
import am.gortsup.testing.model.User;
import am.gortsup.testing.service.InventoryService;
import am.gortsup.testing.service.LoyaltyService;
import am.gortsup.testing.service.OrderService;
import am.gortsup.testing.service.ProductCatalogService;
import am.gortsup.testing.service.ShoppingCartService;
import am.gortsup.testing.service.UserService;
import am.gortsup.testing.util.DateTimeUtils;
import am.gortsup.testing.util.MathAndFinanceUtils;
import am.gortsup.testing.util.StringUtils;
import am.gortsup.testing.util.ValidationUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.util.List;

public class QAApiTestSuite {

    private UserService userService;
    private ProductCatalogService productCatalogService;
    private InventoryService inventoryService;
    private ShoppingCartService cartService;
    private OrderService orderService;
    private LoyaltyService loyaltyService;

    @BeforeMethod(groups = {"smoke", "regression", "negative", "validation", "string", "finance", "date", "security", "catalog", "inventory", "cart", "order", "loyalty"})
    public void setUp() {
        userService = new UserService();
        productCatalogService = new ProductCatalogService();
        inventoryService = new InventoryService();
        cartService = new ShoppingCartService();
        orderService = new OrderService();
        loyaltyService = new LoyaltyService();
    }

    // =========================================================================
    // BASE SMOKE SUITE SCENARIOS (ORIGINAL 9 TESTS)
    // =========================================================================

    @Test(groups = {"smoke"})
    // Проверяет корректную валидацию email, password и phone number: валидные данные проходят, невалидные отбрасываются.
    public void shouldValidateEmailsPasswordsAndPhoneNumbers() {
        Assert.assertTrue(ValidationUtils.isValidEmail("john.doe@example.com"));
        Assert.assertFalse(ValidationUtils.isValidEmail("invalid-email"));
        Assert.assertTrue(ValidationUtils.isValidPassword("StrongPass1!"));
        Assert.assertFalse(ValidationUtils.isValidPassword("weakpass"));
        Assert.assertTrue(ValidationUtils.isValidPhoneNumber("+1 (415) 555-1234"));
        Assert.assertFalse(ValidationUtils.isValidPhoneNumber("abc"));
    }

    @Test(groups = {"smoke"})
    // Проверяет нормализацию текста, поиск повторяющихся подстрок, палиндром и маскирование платежной информации.
    public void shouldNormalizeTextAndMaskSensitiveValues() {
        Assert.assertEquals(StringUtils.toSlug("Java Automation Course!"), "java-automation-course");
        Assert.assertEquals(StringUtils.reverseWords("alpha beta gamma"), "gamma beta alpha");
        Assert.assertTrue(StringUtils.isPalindrome("A man, a plan, a canal: Panama"));
        Assert.assertEquals(StringUtils.maskCreditCard("4111 2222 3333 4444"), "************4444");
        Assert.assertEquals(StringUtils.countOccurrences("banana", "na"), 2);
    }

    @Test(groups = {"smoke"})
    // Проверяет финансовые расчеты: налог, скидка, округление, Luhn-проверка и платеж по кредиту.
    public void shouldCalculateFinancialValuesAndRoundCorrectly() {
        Assert.assertEquals(MathAndFinanceUtils.calculateTax(100.0, 20.0), 20.0, 0.0001);
        Assert.assertEquals(MathAndFinanceUtils.calculateDiscount(100.0, 10.0), 90.0, 0.0001);
        Assert.assertEquals(MathAndFinanceUtils.roundToDecimalPlaces(12.345, 2), 12.35, 0.0001);
        Assert.assertTrue(MathAndFinanceUtils.isValidLuhn("4111111111111111"));
        Assert.assertFalse(MathAndFinanceUtils.isValidLuhn("1234567812345678"));
        Assert.assertEquals(MathAndFinanceUtils.calculateMonthlyInstallment(1200.0, 0.0, 12), 100.0, 0.0001);
    }

    @Test(groups = {"smoke"})
    // Проверяет расчёт рабочих дней, диапазоны дат, пересечение интервалов и определение выходных дней.
    public void shouldHandleDateOperationsAndRanges() {
        LocalDate start = LocalDate.of(2024, 6, 3);
        LocalDate end = LocalDate.of(2024, 6, 10);

        Assert.assertEquals(DateTimeUtils.calculateBusinessDaysBetween(start, end), 5);
        Assert.assertTrue(DateTimeUtils.isDateInRange(LocalDate.of(2024, 6, 5), start, end));
        Assert.assertTrue(DateTimeUtils.doIntervalsOverlap(
                LocalDate.of(2024, 6, 1), LocalDate.of(2024, 6, 7),
                LocalDate.of(2024, 6, 5), LocalDate.of(2024, 6, 9)
        ));
        Assert.assertFalse(DateTimeUtils.isWeekend(LocalDate.of(2024, 6, 3)));
        Assert.assertTrue(DateTimeUtils.isWeekend(LocalDate.of(2024, 6, 8)));
    }

    @Test(groups = {"smoke"})
    // Проверяет регистрацию пользователя, поиск по username и корректность прав доступа по роли.
    public void shouldRegisterUserAndCheckPermissions() {
        User user = userService.registerUser("alice_user", "alice@example.com", "StrongPass1!", Role.CUSTOMER);

        Assert.assertEquals(user.getUsername(), "alice_user");
        Assert.assertEquals(userService.getUserByUsername("ALICE_USER").getId(), user.getId());
        Assert.assertTrue(userService.hasPermission(user.getId(), "READ"));
        Assert.assertTrue(userService.hasPermission(user.getId(), "CREATE_ORDER"));
        Assert.assertFalse(userService.hasPermission(user.getId(), "MANAGE_INVENTORY"));
    }

    @Test(groups = {"smoke"})
    // Проверяет блокировку аккаунта после трёх неудачных логинов и последующее снятие блокировки.
    public void shouldLockAccountAfterThreeFailedLoginAttempts() {
        User user = userService.registerUser("bob_user", "bob@example.com", "StrongPass1!", Role.CUSTOMER);

        Assert.expectThrows(AuthenticationException.class, () -> userService.authenticate("bob_user", "WrongPass1!"));
        Assert.expectThrows(AuthenticationException.class, () -> userService.authenticate("bob_user", "AnotherBad1!"));
        Assert.expectThrows(AccountLockedException.class, () -> userService.authenticate("bob_user", "BadPass2!"));

        Assert.expectThrows(AccountLockedException.class, () -> userService.authenticate("bob_user", "StrongPass1!"));
        userService.unlockUser(user.getId());
        Assert.assertEquals(userService.authenticate("bob_user", "StrongPass1!").getId(), user.getId());
    }

    @Test(groups = {"smoke"})
    // Проверяет добавление товара в каталог, изменение цены, поиск по имени и работу складских операций reserve/release.
    public void shouldManageCatalogAndInventory() {
        Product keyboard = productCatalogService.addProduct(new Product("P-100", "Mechanical Keyboard", ProductCategory.ELECTRONICS, 120.0));

        Assert.assertEquals(productCatalogService.searchProductsByName("mechanical").size(), 1);
        productCatalogService.updateProductPrice("P-100", 110.0);
        Assert.assertEquals(productCatalogService.getProductById("P-100").getPrice(), 110.0, 0.0001);
        Assert.assertEquals(productCatalogService.getProductsInPriceRange(90.0, 150.0).size(), 1);

        inventoryService.setStock("P-100", 10);
        inventoryService.reserveStock("P-100", 4);
        Assert.assertEquals(inventoryService.getStock("P-100"), 6);
        Assert.assertEquals(inventoryService.getReservedStock("P-100"), 4);

        inventoryService.releaseStock("P-100", 2);
        Assert.assertEquals(inventoryService.getStock("P-100"), 8);
        Assert.assertEquals(inventoryService.getReservedStock("P-100"), 2);

        Product catalogProduct = productCatalogService.getProductById("P-100");
        Assert.assertEquals(catalogProduct.getName(), keyboard.getName());
    }

    @Test(groups = {"smoke"})
    // Проверяет подсчёт subtotal, применение скидочного купона и итоговую сумму корзины с налогом.
    public void shouldApplyCouponAndComputeCartTotals() {
        Product laptop = new Product("L-1", "Laptop", ProductCategory.ELECTRONICS, 100.0);
        Product mouse = new Product("M-1", "Mouse", ProductCategory.ELECTRONICS, 50.0);

        cartService.addItem("cart-1", laptop, 1);
        cartService.addItem("cart-1", mouse, 2);

        Assert.assertEquals(cartService.calculateSubtotal("cart-1"), 200.0, 0.0001);
        Assert.assertEquals(cartService.getItemCount("cart-1"), 3);

        Coupon coupon = new Coupon("SAVE10", DiscountType.PERCENTAGE, 10.0, 0.0, LocalDate.now().plusDays(7), 5);
        cartService.applyCoupon("cart-1", coupon, LocalDate.now());

        Assert.assertEquals(cartService.calculateDiscountAmount("cart-1"), 20.0, 0.0001);
        Assert.assertEquals(cartService.calculateTotal("cart-1", 20.0), 216.0, 0.0001);
    }

    @Test(groups = {"smoke"})
    // Проверяет жизненный цикл заказа: создание в PENDING, оплату, отправку, доставку и смену статусов.
    public void shouldCreateAndProcessOrderLifecycle() {
        Product orderProduct = new Product("ORD-PROD", "Tablet", ProductCategory.ELECTRONICS, 150.0);
        List<CartItem> items = List.of(new CartItem(orderProduct, 1));
        Address shippingAddress = new Address("15 Main St", "Yerevan", "Center", "0010", "AM");

        Order order = orderService.createOrder("user-1", items, shippingAddress, 20.0, null);
        Assert.assertEquals(order.getStatus(), OrderStatus.PENDING);
        Assert.assertEquals(order.getTotal(), 180.0, 0.0001);

        Assert.expectThrows(PaymentFailedException.class,
                () -> orderService.payOrder(order.getId(), "1234567812345678", order.getTotal()));

        orderService.payOrder(order.getId(), "4111111111111111", order.getTotal());
        Assert.assertEquals(orderService.getOrder(order.getId()).getStatus(), OrderStatus.PAID);

        orderService.shipOrder(order.getId(), "TRACK-ABC-123");
        Assert.assertEquals(orderService.getOrder(order.getId()).getStatus(), OrderStatus.SHIPPED);

        orderService.deliverOrder(order.getId());
        Assert.assertEquals(orderService.getOrder(order.getId()).getStatus(), OrderStatus.DELIVERED);
    }

    @Test(groups = {"smoke"})
    // Проверяет начисление баллов, переход между уровнями лояльности и перевод баллов между пользователями.
    public void shouldManageLoyaltyPointsAndTierThresholds() {
        loyaltyService.addPoints("user-10", 999);
        Assert.assertEquals(loyaltyService.getUserTier("user-10"), LoyaltyTier.BRONZE);

        loyaltyService.addPoints("user-10", 1);
        Assert.assertEquals(loyaltyService.getUserTier("user-10"), LoyaltyTier.SILVER);

        loyaltyService.addPoints("user-20", 5000);
        Assert.assertEquals(loyaltyService.getUserTier("user-20"), LoyaltyTier.GOLD);
        Assert.assertEquals(loyaltyService.getTierDiscountPercentage(LoyaltyTier.GOLD), 10.0, 0.0001);

        loyaltyService.addPoints("user-30", 10000);
        Assert.assertEquals(loyaltyService.getUserTier("user-30"), LoyaltyTier.PLATINUM);

        loyaltyService.addPoints("user-40", 500);
        loyaltyService.transferPoints("user-40", "user-50", 200);
        Assert.assertEquals(loyaltyService.getPoints("user-40"), 300);
        Assert.assertEquals(loyaltyService.getPoints("user-50"), 200);

        Assert.expectThrows(ValidationException.class,
                () -> loyaltyService.transferPoints("user-40", "user-40", 10));
    }

    // =========================================================================
    // SECTION 1: INPUT VALIDATION TESTS (ValidationUtils)
    // =========================================================================

    @DataProvider(name = "validEmailProvider")
    public Object[][] provideValidEmails() {
        return new Object[][]{
                {"simple@example.com"},
                {"very.common@example.com"},
                {"disposable.style.email.with+symbol@example.com"},
                {"other.email-with-dash@example.com"},
                {"fully-qualified-domain@example.co.uk"},
                {"user.name+tag+sorting@example.com"}
        };
    }

    @Test(dataProvider = "validEmailProvider", groups = {"validation", "regression"})
    // Проверяет успешную валидацию различных корректных форматов email адресов.
    public void shouldAcceptValidEmailFormats(String email) {
        Assert.assertTrue(ValidationUtils.isValidEmail(email), "Expected email to be valid: " + email);
    }

    @DataProvider(name = "invalidEmailProvider")
    public Object[][] provideInvalidEmails() {
        return new Object[][]{
                {null},
                {""},
                {"   "},
                {"plainaddress"},
                {"#@%^%#$@#$@#.com"},
                {"@example.com"},
                {"Joe Smith <email@example.com>"},
                {"email.example.com"},
                {"email@example@example.com"},
                {"email@example..com"}
        };
    }

    @Test(dataProvider = "invalidEmailProvider", groups = {"validation", "negative", "regression"})
    // Проверяет отклонение некорректных форматов email (null, пустые, без домена, с двойными точками).
    public void shouldRejectInvalidEmailFormats(String email) {
        Assert.assertFalse(ValidationUtils.isValidEmail(email), "Expected email to be invalid: " + email);
    }

    @Test(groups = {"validation", "negative"})
    // Проверяет граничное ограничение максимальной длины email (RFC 254 символа): ровно 254 символа допустимо, от 255 отбрасывается.
    public void shouldRejectEmailExceedingMaxLength254() {
        String exactly254 = "a".repeat(245) + "@test.com";
        String exceeds254 = "a".repeat(246) + "@test.com";
        Assert.assertTrue(ValidationUtils.isValidEmail(exactly254));
        Assert.assertFalse(ValidationUtils.isValidEmail(exceeds254));
    }

    @Test(groups = {"validation", "negative"})
    // Проверяет проверку пароля на несоблюдение отдельных требований сложности: длина, регистр, спецсимволы, пробелы.
    public void shouldRejectPasswordsFailingIndividualComplexityRules() {
        Assert.assertFalse(ValidationUtils.isValidPassword("Short1!"), "Password < 8 chars must fail");
        Assert.assertFalse(ValidationUtils.isValidPassword("A".repeat(65) + "1!"), "Password > 64 chars must fail");
        Assert.assertFalse(ValidationUtils.isValidPassword("ALLCAPS123!@#"), "Missing lowercase must fail");
        Assert.assertFalse(ValidationUtils.isValidPassword("lowercase123!@#"), "Missing uppercase must fail");
        Assert.assertFalse(ValidationUtils.isValidPassword("NoDigitsAllowed!@#"), "Missing digit must fail");
        Assert.assertFalse(ValidationUtils.isValidPassword("NoSpecialChars123"), "Missing special char must fail");
        Assert.assertFalse(ValidationUtils.isValidPassword("With Space1!"), "Whitespace must fail");
        Assert.assertFalse(ValidationUtils.isValidPassword(null), "Null password must fail");
    }

    @Test(groups = {"validation", "regression"})
    // Проверяет валидацию телефонных номеров с международными префиксами, скобками, точками и дефисами.
    public void shouldValidatePhoneNumbersWithVariousSeparatorsAndLengths() {
        Assert.assertTrue(ValidationUtils.isValidPhoneNumber("+1-541-754-3010"));
        Assert.assertTrue(ValidationUtils.isValidPhoneNumber("1911234567"));
        Assert.assertTrue(ValidationUtils.isValidPhoneNumber("+374 (10) 55-55-55"));
        Assert.assertTrue(ValidationUtils.isValidPhoneNumber("1234567")); // exactly 7 digits
        Assert.assertFalse(ValidationUtils.isValidPhoneNumber("123456")); // 6 digits (below 7)
        Assert.assertFalse(ValidationUtils.isValidPhoneNumber("+1234567890123456")); // 16 digits (above 15)
        Assert.assertFalse(ValidationUtils.isValidPhoneNumber("phone-number"));
        Assert.assertFalse(ValidationUtils.isValidPhoneNumber(null));
    }

    @Test(groups = {"validation", "regression"})
    // Проверяет валидацию почтовых индексов различных стран (US, AM, UK/GB, CA и прочие).
    public void shouldValidatePostalCodesForVariousCountries() {
        Assert.assertTrue(ValidationUtils.isValidPostalCode("90210", "US"));
        Assert.assertTrue(ValidationUtils.isValidPostalCode("90210-1234", "US"));
        Assert.assertFalse(ValidationUtils.isValidPostalCode("9021", "US"));

        Assert.assertTrue(ValidationUtils.isValidPostalCode("0001", "AM"));
        Assert.assertFalse(ValidationUtils.isValidPostalCode("001", "AM"));
        Assert.assertFalse(ValidationUtils.isValidPostalCode("ABCD", "AM"));

        Assert.assertTrue(ValidationUtils.isValidPostalCode("SW1A 1AA", "UK"));
        Assert.assertTrue(ValidationUtils.isValidPostalCode("W1A 0AX", "GB"));

        Assert.assertTrue(ValidationUtils.isValidPostalCode("K1A 0B1", "CA"));
        Assert.assertFalse(ValidationUtils.isValidPostalCode("12345", "CA"));

        Assert.assertTrue(ValidationUtils.isValidPostalCode("10115", "DE")); // generic 3-10 alphanumeric
        Assert.assertFalse(ValidationUtils.isValidPostalCode("AB", "FR")); // too short (<3)
        Assert.assertFalse(ValidationUtils.isValidPostalCode(null, "US"));
        Assert.assertFalse(ValidationUtils.isValidPostalCode("90210", null));
    }

    @Test(groups = {"validation", "regression"})
    // Проверяет валидацию банковских счетов IBAN по алгоритму ISO 7064 Mod-97.
    public void shouldValidateIbanChecksumMod97() {
        Assert.assertTrue(ValidationUtils.isValidIban("GB82WEST12345698765432"));
        Assert.assertTrue(ValidationUtils.isValidIban("GB82 WEST 1234 5698 7654 32"));
        Assert.assertFalse(ValidationUtils.isValidIban("GB82WEST12345698765433")); // invalid check digit
        Assert.assertFalse(ValidationUtils.isValidIban("SHORT")); // too short (<15)
        Assert.assertFalse(ValidationUtils.isValidIban(null));
    }

    @Test(groups = {"validation", "regression"})
    // Проверяет валидацию IPv4 адресов, отклоняя ведущие нули и значения октетов выше 255.
    public void shouldValidateIpv4AddressesAndRejectLeadingZeros() {
        Assert.assertTrue(ValidationUtils.isValidIpAddress("192.168.1.1"));
        Assert.assertTrue(ValidationUtils.isValidIpAddress("0.0.0.0"));
        Assert.assertTrue(ValidationUtils.isValidIpAddress("255.255.255.255"));
        Assert.assertTrue(ValidationUtils.isValidIpAddress("127.0.0.1"));

        Assert.assertFalse(ValidationUtils.isValidIpAddress("192.168.01.1")); // leading zero in octet
        Assert.assertFalse(ValidationUtils.isValidIpAddress("256.0.0.1")); // octet > 255
        Assert.assertFalse(ValidationUtils.isValidIpAddress("192.168.1")); // missing octet
        Assert.assertFalse(ValidationUtils.isValidIpAddress("192.168.1.1.1")); // extra octet
        Assert.assertFalse(ValidationUtils.isValidIpAddress("a.b.c.d"));
        Assert.assertFalse(ValidationUtils.isValidIpAddress(null));
        Assert.assertFalse(ValidationUtils.isValidIpAddress(""));
    }

    @Test(groups = {"validation", "regression"})
    // Проверяет валидацию URL адресов с протоколами HTTP и HTTPS, портами и параметрами запроса.
    public void shouldValidateHttpAndHttpsUrls() {
        Assert.assertTrue(ValidationUtils.isValidUrl("https://example.com"));
        Assert.assertTrue(ValidationUtils.isValidUrl("http://sub.domain.org/path?param=value#anchor"));
        Assert.assertTrue(ValidationUtils.isValidUrl("https://qa-portal.company.am:8080/api/v1/test"));

        Assert.assertFalse(ValidationUtils.isValidUrl("ftp://example.com"));
        Assert.assertFalse(ValidationUtils.isValidUrl("just-a-plain-string"));
        Assert.assertFalse(ValidationUtils.isValidUrl("https://"));
        Assert.assertFalse(ValidationUtils.isValidUrl(null));
        Assert.assertFalse(ValidationUtils.isValidUrl(""));
    }

    @Test(groups = {"validation", "regression"})
    // Проверяет санитизацию потенциально опасных XSS-символов в HTML сущности.
    public void shouldSanitizeInputContainingHtmlAndSpecialChars() {
        String malicious = "<script>alert('xss');</script>&\"test\"";
        String sanitized = ValidationUtils.sanitizeInput(malicious);

        Assert.assertEquals(sanitized, "&lt;script&gt;alert(&#x27;xss&#x27;);&lt;/script&gt;&amp;&quot;test&quot;");
        Assert.assertEquals(ValidationUtils.sanitizeInput(null), "");
        Assert.assertEquals(ValidationUtils.sanitizeInput("Clean Text"), "Clean Text");
    }

    @Test(groups = {"validation", "regression"})
    // Проверяет метод isPositiveNumber для положительных, отрицательных, нулевых и null значений.
    public void shouldValidatePositiveNumbersAndRejectZeroOrNegative() {
        Assert.assertTrue(ValidationUtils.isPositiveNumber(10));
        Assert.assertTrue(ValidationUtils.isPositiveNumber(0.0001));
        Assert.assertTrue(ValidationUtils.isPositiveNumber(100.5f));

        Assert.assertFalse(ValidationUtils.isPositiveNumber(0));
        Assert.assertFalse(ValidationUtils.isPositiveNumber(0.0));
        Assert.assertFalse(ValidationUtils.isPositiveNumber(-5));
        Assert.assertFalse(ValidationUtils.isPositiveNumber(-0.01));
        Assert.assertFalse(ValidationUtils.isPositiveNumber(null));
    }

    // =========================================================================
    // SECTION 2: STRING UTILITY TESTS (StringUtils)
    // =========================================================================

    @Test(groups = {"string", "regression"})
    // Проверяет преобразование строки с диакритикой, пробелами и спецсимволами в безопасный URL slug.
    public void shouldGenerateUrlSlugsFromSpecialCharactersAndAccents() {
        Assert.assertEquals(StringUtils.toSlug("Café & Restaurant!"), "cafe-restaurant");
        Assert.assertEquals(StringUtils.toSlug("  ---Multiple---Dashes---  "), "multiple-dashes");
        Assert.assertEquals(StringUtils.toSlug("Product 100% Cotton"), "product-100-cotton");
        Assert.assertEquals(StringUtils.toSlug("   "), "");
        Assert.assertEquals(StringUtils.toSlug(null), "");
    }

    @Test(groups = {"string", "regression"})
    // Проверяет капитализацию каждого слова в строке с приведением остальных символов к нижнему регистру.
    public void shouldCapitalizeWordsWithIrregularCasing() {
        Assert.assertEquals(StringUtils.capitalizeWords("jOHn dOE"), "John Doe");
        Assert.assertEquals(StringUtils.capitalizeWords("DEVELOPER"), "Developer");
        Assert.assertEquals(StringUtils.capitalizeWords("test automation engineer"), "Test Automation Engineer");
        Assert.assertEquals(StringUtils.capitalizeWords(""), "");
        Assert.assertEquals(StringUtils.capitalizeWords(null), "");
    }

    @Test(groups = {"string", "regression"})
    // Проверяет усечение строки до максимальной длины с добавлением суффикса.
    public void shouldTruncateTextWithSuffixCorrectly() {
        Assert.assertEquals(StringUtils.truncate("Antigravity Framework", 15, "..."), "Antigravity ...");
        Assert.assertEquals(StringUtils.truncate("Short", 10, "..."), "Short");
        Assert.assertEquals(StringUtils.truncate("Exact", 5, "..."), "Exact");
        Assert.assertNull(StringUtils.truncate(null, 5, "..."));
        Assert.assertEquals(StringUtils.truncate("NoSuffix", 4, null), "NoSu");
    }

    @Test(expectedExceptions = IllegalArgumentException.class, groups = {"string", "negative"})
    // Проверяет выброс исключения IllegalArgumentException при maxLength меньше длины суффикса.
    public void shouldThrowExceptionWhenTruncateMaxLengthLessThanSuffix() {
        StringUtils.truncate("Text", 2, "...");
    }

    @Test(groups = {"string", "regression"})
    // Проверяет подсчёт непересекающихся вхождений подстроки с учётом регистра.
    public void shouldCountSubStringOccurrencesCaseSensitively() {
        Assert.assertEquals(StringUtils.countOccurrences("aaaaa", "aa"), 2);
        Assert.assertEquals(StringUtils.countOccurrences("AbcAbcabc", "Abc"), 2);
        Assert.assertEquals(StringUtils.countOccurrences("hello world", "xyz"), 0);
        Assert.assertEquals(StringUtils.countOccurrences(null, "sub"), 0);
        Assert.assertEquals(StringUtils.countOccurrences("text", null), 0);
        Assert.assertEquals(StringUtils.countOccurrences("text", ""), 0);
    }

    @Test(groups = {"string", "regression"})
    // Проверяет определение палиндромов с игнорированием регистра, пробелов и знаков препинания.
    public void shouldIdentifyPalindromesIgnoringPunctuationAndCase() {
        Assert.assertTrue(StringUtils.isPalindrome("Madam, I'm Adam"));
        Assert.assertTrue(StringUtils.isPalindrome("Was it a car or a cat I saw?"));
        Assert.assertTrue(StringUtils.isPalindrome("12321"));
        Assert.assertFalse(StringUtils.isPalindrome("Not a palindrome"));
        Assert.assertFalse(StringUtils.isPalindrome(""));
        Assert.assertFalse(StringUtils.isPalindrome(null));
    }

    @Test(groups = {"string", "regression"})
    // Проверяет маскирование номера банковской карты с сохранением только последних 4 цифр.
    public void shouldMaskCreditCardsWithShortOrFormattedInputs() {
        Assert.assertEquals(StringUtils.maskCreditCard("1234-5678-9012-3456"), "************3456");
        Assert.assertEquals(StringUtils.maskCreditCard("5555444433332222"), "************2222");
        Assert.assertEquals(StringUtils.maskCreditCard("123"), "***"); // less than 4 digits masked completely
        Assert.assertEquals(StringUtils.maskCreditCard(""), "");
        Assert.assertEquals(StringUtils.maskCreditCard(null), "");
    }

    @Test(groups = {"string", "regression"})
    // Проверяет переворот порядка слов в предложении с сохранением одиночных пробелов.
    public void shouldReverseWordsInSentencePreservingSingleSpaces() {
        Assert.assertEquals(StringUtils.reverseWords("The quick brown fox"), "fox brown quick The");
        Assert.assertEquals(StringUtils.reverseWords("single"), "single");
        Assert.assertEquals(StringUtils.reverseWords("  spaced   out   words  "), "words out spaced");
        Assert.assertEquals(StringUtils.reverseWords(""), "");
        Assert.assertEquals(StringUtils.reverseWords(null), "");
    }

    // =========================================================================
    // SECTION 3: MATH & FINANCIAL UTILITY TESTS (MathAndFinanceUtils)
    // =========================================================================

    @Test(groups = {"finance", "regression"})
    // Проверяет расчёт налога для граничных процентных ставок (0%, 100%) и стандартных значений.
    public void shouldCalculateTaxForStandardAndBoundaryRates() {
        Assert.assertEquals(MathAndFinanceUtils.calculateTax(250.0, 20.0), 50.0, 0.0001);
        Assert.assertEquals(MathAndFinanceUtils.calculateTax(100.0, 0.0), 0.0, 0.0001);
        Assert.assertEquals(MathAndFinanceUtils.calculateTax(75.5, 100.0), 75.5, 0.0001);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, groups = {"finance", "negative"})
    // Проверяет исключение при передаче отрицательной суммы в расчёт налога.
    public void shouldThrowExceptionWhenTaxAmountIsNegative() {
        MathAndFinanceUtils.calculateTax(-10.0, 20.0);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, groups = {"finance", "negative"})
    // Проверяет исключение при передаче ставки налога свыше 100%.
    public void shouldThrowExceptionWhenTaxRateExceedsHundred() {
        MathAndFinanceUtils.calculateTax(100.0, 100.1);
    }

    @Test(groups = {"finance", "regression"})
    // Проверяет расчёт скидки для 0%, 100% и стандартных значений.
    public void shouldCalculateDiscountForStandardAndBoundaryPercentages() {
        Assert.assertEquals(MathAndFinanceUtils.calculateDiscount(200.0, 15.0), 170.0, 0.0001);
        Assert.assertEquals(MathAndFinanceUtils.calculateDiscount(100.0, 0.0), 100.0, 0.0001);
        Assert.assertEquals(MathAndFinanceUtils.calculateDiscount(150.0, 100.0), 0.0, 0.0001);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, groups = {"finance", "negative"})
    // Проверяет исключение при расчёте скидки с отрицательным процентом.
    public void shouldThrowExceptionWhenDiscountPercentageIsNegative() {
        MathAndFinanceUtils.calculateDiscount(100.0, -5.0);
    }

    @Test(groups = {"finance", "regression"})
    // Проверяет округление HALF_UP до заданного числа десятичных знаков.
    public void shouldRoundToSpecifiedDecimalPlacesHalfUp() {
        Assert.assertEquals(MathAndFinanceUtils.roundToDecimalPlaces(2.345, 2), 2.35, 0.0001);
        Assert.assertEquals(MathAndFinanceUtils.roundToDecimalPlaces(2.344, 2), 2.34, 0.0001);
        Assert.assertEquals(MathAndFinanceUtils.roundToDecimalPlaces(5.0, 0), 5.0, 0.0001);
        Assert.assertTrue(Double.isNaN(MathAndFinanceUtils.roundToDecimalPlaces(Double.NaN, 2)));
    }

    @Test(expectedExceptions = IllegalArgumentException.class, groups = {"finance", "negative"})
    // Проверяет исключение при передаче отрицательного количества знаков округления.
    public void shouldThrowExceptionWhenRoundingPlacesNegative() {
        MathAndFinanceUtils.roundToDecimalPlaces(10.5, -1);
    }

    @Test(groups = {"finance", "regression"})
    // Проверяет конвертацию валюты по курсу и валидацию входных параметров.
    public void shouldConvertCurrencyAndEnforceInputConstraints() {
        Assert.assertEquals(MathAndFinanceUtils.convertCurrency(100.0, 1.25), 125.0, 0.0001);
        Assert.assertEquals(MathAndFinanceUtils.convertCurrency(0.0, 385.5), 0.0, 0.0001);

        Assert.expectThrows(IllegalArgumentException.class, () -> MathAndFinanceUtils.convertCurrency(-1.0, 1.25));
        Assert.expectThrows(IllegalArgumentException.class, () -> MathAndFinanceUtils.convertCurrency(100.0, 0.0));
        Assert.expectThrows(IllegalArgumentException.class, () -> MathAndFinanceUtils.convertCurrency(100.0, -1.5));
    }

    @Test(groups = {"finance", "regression"})
    // Проверяет формулу сложных процентов и валидацию ограничений аргументов.
    public void shouldCalculateCompoundInterestCorrectly() {
        // P=1000, r=5%, n=4, t=2 -> 1104.49
        Assert.assertEquals(MathAndFinanceUtils.calculateCompoundInterest(1000.0, 5.0, 4, 2), 1104.49, 0.0001);

        Assert.expectThrows(IllegalArgumentException.class,
                () -> MathAndFinanceUtils.calculateCompoundInterest(-100.0, 5.0, 4, 2));
        Assert.expectThrows(IllegalArgumentException.class,
                () -> MathAndFinanceUtils.calculateCompoundInterest(1000.0, -1.0, 4, 2));
        Assert.expectThrows(IllegalArgumentException.class,
                () -> MathAndFinanceUtils.calculateCompoundInterest(1000.0, 5.0, 0, 2));
        Assert.expectThrows(IllegalArgumentException.class,
                () -> MathAndFinanceUtils.calculateCompoundInterest(1000.0, 5.0, 4, -1));
    }

    @Test(groups = {"finance", "regression"})
    // Проверяет расчёт ежемесячного аннуитетного платежа по кредиту (с нулевой и положительной процентной ставкой).
    public void shouldCalculateMonthlyInstallmentForZeroAndPositiveRate() {
        Assert.assertEquals(MathAndFinanceUtils.calculateMonthlyInstallment(12000.0, 0.0, 12), 1000.0, 0.0001);
        Assert.assertEquals(MathAndFinanceUtils.calculateMonthlyInstallment(10000.0, 5.0, 24), 438.71, 0.0001);

        Assert.expectThrows(IllegalArgumentException.class,
                () -> MathAndFinanceUtils.calculateMonthlyInstallment(0.0, 5.0, 12));
        Assert.expectThrows(IllegalArgumentException.class,
                () -> MathAndFinanceUtils.calculateMonthlyInstallment(1000.0, -2.0, 12));
        Assert.expectThrows(IllegalArgumentException.class,
                () -> MathAndFinanceUtils.calculateMonthlyInstallment(1000.0, 5.0, 0));
    }

    @Test(groups = {"finance", "regression"})
    // Проверяет расчёт процентного изменения между старым и новым значением и обработку деления на ноль.
    public void shouldCalculatePercentageChangeAndThrowOnZeroOldValue() {
        Assert.assertEquals(MathAndFinanceUtils.calculatePercentageChange(100.0, 150.0), 50.0, 0.0001);
        Assert.assertEquals(MathAndFinanceUtils.calculatePercentageChange(200.0, 150.0), -25.0, 0.0001);

        Assert.expectThrows(IllegalArgumentException.class,
                () -> MathAndFinanceUtils.calculatePercentageChange(0.0, 100.0));
    }

    // =========================================================================
    // SECTION 4: DATE & BUSINESS DAY UTILITY TESTS (DateTimeUtils)
    // =========================================================================

    @DataProvider(name = "leapYearProvider")
    public Object[][] provideLeapYears() {
        return new Object[][]{
                {2024, true},
                {2023, false},
                {2000, true},
                {1900, false},
                {2400, true},
                {2100, false}
        };
    }

    @Test(dataProvider = "leapYearProvider", groups = {"date", "regression"})
    // Проверяет алгоритм определения високосного года по правилам григорианского календаря.
    public void shouldDetermineLeapYearsAccordingToGregorianRules(int year, boolean expected) {
        Assert.assertEquals(DateTimeUtils.isLeapYear(year), expected);
    }

    @Test(groups = {"date", "regression"})
    // Проверяет вычисление количества дней между датами в прямом и обратном направлениях.
    public void shouldCalculateDaysBetweenInclusiveAndNegativeWhenReversed() {
        LocalDate d1 = LocalDate.of(2024, 1, 1);
        LocalDate d2 = LocalDate.of(2024, 1, 10);

        Assert.assertEquals(DateTimeUtils.calculateDaysBetween(d1, d2), 9);
        Assert.assertEquals(DateTimeUtils.calculateDaysBetween(d2, d1), -9);

        Assert.expectThrows(IllegalArgumentException.class, () -> DateTimeUtils.calculateDaysBetween(null, d2));
        Assert.expectThrows(IllegalArgumentException.class, () -> DateTimeUtils.calculateDaysBetween(d1, null));
    }

    @Test(groups = {"date", "regression"})
    // Проверяет идентификацию субботы и воскресенья как выходных дней.
    public void shouldIdentifyWeekendsAndThrowOnNullDate() {
        Assert.assertTrue(DateTimeUtils.isWeekend(LocalDate.of(2024, 6, 8))); // Saturday
        Assert.assertTrue(DateTimeUtils.isWeekend(LocalDate.of(2024, 6, 9))); // Sunday
        Assert.assertFalse(DateTimeUtils.isWeekend(LocalDate.of(2024, 6, 7))); // Friday
        Assert.assertFalse(DateTimeUtils.isWeekend(LocalDate.of(2024, 6, 10))); // Monday

        Assert.expectThrows(IllegalArgumentException.class, () -> DateTimeUtils.isWeekend(null));
    }

    @Test(groups = {"date", "regression"})
    // Проверяет расчёт рабочих дней с пропуском выходных, а также случай start >= end.
    public void shouldCalculateBusinessDaysExcludingWeekends() {
        LocalDate monday = LocalDate.of(2024, 6, 3);
        LocalDate friday = LocalDate.of(2024, 6, 7);
        LocalDate nextMonday = LocalDate.of(2024, 6, 10);

        Assert.assertEquals(DateTimeUtils.calculateBusinessDaysBetween(monday, friday), 4);
        Assert.assertEquals(DateTimeUtils.calculateBusinessDaysBetween(monday, nextMonday), 5);
        Assert.assertEquals(DateTimeUtils.calculateBusinessDaysBetween(monday, monday), 0);
        Assert.assertEquals(DateTimeUtils.calculateBusinessDaysBetween(nextMonday, monday), 0);

        Assert.expectThrows(IllegalArgumentException.class, () -> DateTimeUtils.calculateBusinessDaysBetween(null, friday));
    }

    @Test(groups = {"date", "regression"})
    // Проверяет прибавление рабочих дней со сдвигом через выходные дни.
    public void shouldAddBusinessDaysSkippingWeekendsCorrectly() {
        LocalDate friday = LocalDate.of(2024, 6, 7);

        Assert.assertEquals(DateTimeUtils.addBusinessDays(friday, 1), LocalDate.of(2024, 6, 10)); // Next Monday
        Assert.assertEquals(DateTimeUtils.addBusinessDays(friday, 5), LocalDate.of(2024, 6, 14)); // Next Friday
        Assert.assertEquals(DateTimeUtils.addBusinessDays(friday, 0), friday);

        Assert.expectThrows(IllegalArgumentException.class, () -> DateTimeUtils.addBusinessDays(null, 2));
        Assert.expectThrows(IllegalArgumentException.class, () -> DateTimeUtils.addBusinessDays(friday, -1));
    }

    @Test(groups = {"date", "regression"})
    // Проверяет определение пересечения временных интервалов (частичное, граничное, отсутствие пересечения).
    public void shouldCheckIntervalOverlapBoundaryCases() {
        LocalDate a1 = LocalDate.of(2024, 6, 1);
        LocalDate a2 = LocalDate.of(2024, 6, 10);
        LocalDate b1 = LocalDate.of(2024, 6, 10);
        LocalDate b2 = LocalDate.of(2024, 6, 20);
        LocalDate c1 = LocalDate.of(2024, 6, 11);
        LocalDate c2 = LocalDate.of(2024, 6, 20);

        Assert.assertTrue(DateTimeUtils.doIntervalsOverlap(a1, a2, b1, b2), "Touching at boundary should overlap");
        Assert.assertFalse(DateTimeUtils.doIntervalsOverlap(a1, a2, c1, c2), "Disjoint intervals should not overlap");

        Assert.expectThrows(IllegalArgumentException.class,
                () -> DateTimeUtils.doIntervalsOverlap(a2, a1, b1, b2)); // start > end
    }

    @Test(groups = {"date", "regression"})
    // Проверяет нахождение даты в закрытом диапазоне [min, max] включая граничные точки.
    public void shouldValidateIfDateIsInRangeInclusive() {
        LocalDate min = LocalDate.of(2024, 1, 1);
        LocalDate max = LocalDate.of(2024, 12, 31);

        Assert.assertTrue(DateTimeUtils.isDateInRange(min, min, max));
        Assert.assertTrue(DateTimeUtils.isDateInRange(max, min, max));
        Assert.assertTrue(DateTimeUtils.isDateInRange(LocalDate.of(2024, 6, 15), min, max));
        Assert.assertFalse(DateTimeUtils.isDateInRange(LocalDate.of(2025, 1, 1), min, max));

        Assert.expectThrows(IllegalArgumentException.class, () -> DateTimeUtils.isDateInRange(null, min, max));
        Assert.expectThrows(IllegalArgumentException.class, () -> DateTimeUtils.isDateInRange(min, max, min));
    }

    // =========================================================================
    // SECTION 5: USER SERVICE & SECURITY TESTS (UserService)
    // =========================================================================

    @Test(groups = {"security", "regression"})
    // Проверяет назначение роли CUSTOMER по умолчанию при передаче null в registerUser.
    public void shouldRegisterUserWithDefaultRoleCustomerWhenRoleIsNull() {
        User user = userService.registerUser("default_role_user", "def@example.com", "StrongPass1!", null);
        Assert.assertEquals(user.getRole(), Role.CUSTOMER);
    }

    @Test(groups = {"security", "negative"})
    // Проверяет отказ в регистрации при некорректном формате имени пользователя (<3 или >30 символов, спецсимволы).
    public void shouldRejectRegistrationWithInvalidUsername() {
        Assert.expectThrows(ValidationException.class,
                () -> userService.registerUser("ab", "valid@example.com", "StrongPass1!", Role.CUSTOMER));
        Assert.expectThrows(ValidationException.class,
                () -> userService.registerUser("a".repeat(31), "valid@example.com", "StrongPass1!", Role.CUSTOMER));
        Assert.expectThrows(ValidationException.class,
                () -> userService.registerUser("user@name", "valid@example.com", "StrongPass1!", Role.CUSTOMER));
        Assert.expectThrows(ValidationException.class,
                () -> userService.registerUser(null, "valid@example.com", "StrongPass1!", Role.CUSTOMER));
    }

    @Test(groups = {"security", "negative"})
    // Проверяет отказ в регистрации пользователя с некорректным email или слабым паролем.
    public void shouldRejectRegistrationWithInvalidEmailOrWeakPassword() {
        Assert.expectThrows(ValidationException.class,
                () -> userService.registerUser("valid_user", "invalid-email", "StrongPass1!", Role.CUSTOMER));
        Assert.expectThrows(ValidationException.class,
                () -> userService.registerUser("valid_user", "valid@example.com", "weak", Role.CUSTOMER));
    }

    @Test(groups = {"security", "negative"})
    // Проверяет запрет регистрации дублирующегося username без учёта регистра.
    public void shouldRejectRegistrationWithDuplicateUsernameCaseInsensitive() {
        userService.registerUser("charlie_brown", "charlie@example.com", "StrongPass1!", Role.CUSTOMER);
        Assert.expectThrows(ValidationException.class,
                () -> userService.registerUser("CHARLIE_BROWN", "other@example.com", "StrongPass1!", Role.CUSTOMER));
    }

    @Test(groups = {"security", "negative"})
    // Проверяет запрет регистрации пользователя с уже занятым email адресом.
    public void shouldRejectRegistrationWithDuplicateEmailCaseInsensitive() {
        userService.registerUser("unique_user_1", "shared@example.com", "StrongPass1!", Role.CUSTOMER);
        Assert.expectThrows(ValidationException.class,
                () -> userService.registerUser("unique_user_2", "SHARED@example.com", "StrongPass1!", Role.CUSTOMER));
    }

    @Test(groups = {"security", "regression"})
    // Проверяет сброс счётчика неудачных попыток входа при успешной аутентификации.
    public void shouldAuthenticateUserWithValidCredentialsAndResetFailedAttempts() {
        userService.registerUser("david_user", "david@example.com", "StrongPass1!", Role.CUSTOMER);

        Assert.expectThrows(AuthenticationException.class,
                () -> userService.authenticate("david_user", "WrongPass1!"));
        User authUser = userService.authenticate("david_user", "StrongPass1!");

        Assert.assertEquals(authUser.getFailedLoginAttempts(), 0);
    }

    @Test(groups = {"security", "negative"})
    // Проверяет ошибки валидации при пустых данных аутентификации или несуществующем пользователе.
    public void shouldRejectAuthenticationWithEmptyDataOrNonExistentUser() {
        Assert.expectThrows(ValidationException.class, () -> userService.authenticate("", "pass"));
        Assert.expectThrows(ValidationException.class, () -> userService.authenticate("user", ""));
        Assert.expectThrows(ValidationException.class, () -> userService.authenticate(null, "pass"));
        Assert.expectThrows(AuthenticationException.class, () -> userService.authenticate("non_existent", "StrongPass1!"));
    }

    @Test(groups = {"security", "regression"})
    // Проверяет успешную смену пароля и валидацию совпадения со старым/слабым паролем.
    public void shouldChangePasswordSuccessfullyAndEnforcePasswordRules() {
        User user = userService.registerUser("eva_user", "eva@example.com", "OldStrongPass1!", Role.CUSTOMER);

        Assert.expectThrows(AuthenticationException.class,
                () -> userService.changePassword(user.getId(), "WrongOldPass1!", "NewStrongPass2!"));
        Assert.expectThrows(ValidationException.class,
                () -> userService.changePassword(user.getId(), "OldStrongPass1!", "OldStrongPass1!")); // same password
        Assert.expectThrows(ValidationException.class,
                () -> userService.changePassword(user.getId(), "OldStrongPass1!", "weak")); // weak password

        userService.changePassword(user.getId(), "OldStrongPass1!", "NewStrongPass2!");
        Assert.assertNotNull(userService.authenticate("eva_user", "NewStrongPass2!"));
    }

    @Test(groups = {"security", "regression"})
    // Проверяет ручную блокировку и разблокировку пользователя администратором.
    public void shouldLockAndUnlockUserManually() {
        User user = userService.registerUser("frank_user", "frank@example.com", "StrongPass1!", Role.CUSTOMER);

        userService.lockUser(user.getId());
        Assert.expectThrows(AccountLockedException.class, () -> userService.authenticate("frank_user", "StrongPass1!"));

        userService.unlockUser(user.getId());
        Assert.assertEquals(userService.authenticate("frank_user", "StrongPass1!").getId(), user.getId());
    }

    @Test(groups = {"security", "regression"})
    // Проверяет запрет аутентификации для деактивированного аккаунта пользователя.
    public void shouldPreventInactiveUserFromAuthenticating() {
        User user = userService.registerUser("grace_user", "grace@example.com", "StrongPass1!", Role.CUSTOMER);

        userService.deactivateUser(user.getId());
        Assert.expectThrows(AuthenticationException.class, () -> userService.authenticate("grace_user", "StrongPass1!"));
    }

    @Test(groups = {"security", "regression"})
    // Проверяет обновление email адреса и валидацию уникальности и формата.
    public void shouldUpdateUserEmailAndRejectDuplicates() {
        User u1 = userService.registerUser("helen_1", "helen1@example.com", "StrongPass1!", Role.CUSTOMER);
        userService.registerUser("helen_2", "helen2@example.com", "StrongPass1!", Role.CUSTOMER);

        Assert.expectThrows(ValidationException.class, () -> userService.updateEmail(u1.getId(), "invalid-email"));
        Assert.expectThrows(ValidationException.class, () -> userService.updateEmail(u1.getId(), "helen2@example.com"));

        userService.updateEmail(u1.getId(), "helen_new@example.com");
        Assert.assertEquals(userService.getUserById(u1.getId()).getEmail(), "helen_new@example.com");
    }

    @Test(groups = {"security", "regression"})
    // Проверяет матрицу полномочий ролей ADMIN, MANAGER, CUSTOMER и GUEST.
    public void shouldVerifyPermissionsHierarchyForAllRoles() {
        User admin = userService.registerUser("admin_u", "admin@example.com", "StrongPass1!", Role.ADMIN);
        User manager = userService.registerUser("mgr_u", "mgr@example.com", "StrongPass1!", Role.MANAGER);
        User guest = userService.registerUser("guest_u", "guest@example.com", "StrongPass1!", Role.GUEST);

        Assert.assertTrue(userService.hasPermission(admin.getId(), "ANY_CUSTOM_PERMISSION"));
        Assert.assertTrue(userService.hasPermission(manager.getId(), "MANAGE_INVENTORY"));
        Assert.assertFalse(userService.hasPermission(manager.getId(), "DELETE_DATABASE"));
        Assert.assertTrue(userService.hasPermission(guest.getId(), "READ"));
        Assert.assertFalse(userService.hasPermission(guest.getId(), "CREATE_ORDER"));
    }

    // =========================================================================
    // SECTION 6: PRODUCT CATALOG TESTS (ProductCatalogService)
    // =========================================================================

    @Test(groups = {"catalog", "negative"})
    // Проверяет валидацию полей товара при добавлении в каталог (null, пустые поля, отрицательная цена).
    public void shouldValidateProductFieldsOnAddition() {
        Assert.expectThrows(ValidationException.class, () -> productCatalogService.addProduct(null));
        Assert.expectThrows(ValidationException.class,
                () -> productCatalogService.addProduct(new Product("", "Name", ProductCategory.BOOKS, 10.0)));
        Assert.expectThrows(ValidationException.class,
                () -> productCatalogService.addProduct(new Product("ID-1", "", ProductCategory.BOOKS, 10.0)));
        Assert.expectThrows(ValidationException.class,
                () -> productCatalogService.addProduct(new Product("ID-1", "Name", null, 10.0)));
        Assert.expectThrows(ValidationException.class,
                () -> productCatalogService.addProduct(new Product("ID-1", "Name", ProductCategory.BOOKS, -5.0)));
    }

    @Test(groups = {"catalog", "negative"})
    // Проверяет запрет добавления товара с уже существующим идентификатором ID.
    public void shouldRejectDuplicateProductIdOnAddition() {
        productCatalogService.addProduct(new Product("DUP-1", "Product A", ProductCategory.HOME, 25.0));
        Assert.expectThrows(ValidationException.class,
                () -> productCatalogService.addProduct(new Product("DUP-1", "Product B", ProductCategory.HOME, 30.0)));
    }

    @Test(groups = {"catalog", "regression"})
    // Проверяет поиск товара по ID и выброс исключения ResourceNotFoundException при отсутствии.
    public void shouldRetrieveProductByIdAndHandleMissing() {
        Product p = productCatalogService.addProduct(new Product("P-SEARCH", "Desk Lamp", ProductCategory.HOME, 45.0));
        Assert.assertEquals(productCatalogService.getProductById("P-SEARCH").getName(), p.getName());

        Assert.expectThrows(ResourceNotFoundException.class, () -> productCatalogService.getProductById("UNKNOWN"));
        Assert.expectThrows(ResourceNotFoundException.class, () -> productCatalogService.getProductById(null));
    }

    @Test(groups = {"catalog", "regression"})
    // Проверяет обновление цены товара и отклонение отрицательной цены.
    public void shouldUpdateProductPriceAndRejectNegativeValue() {
        productCatalogService.addProduct(new Product("P-PRICE", "Monitor", ProductCategory.ELECTRONICS, 300.0));
        productCatalogService.updateProductPrice("P-PRICE", 279.99);

        Assert.assertEquals(productCatalogService.getProductById("P-PRICE").getPrice(), 279.99, 0.0001);
        Assert.expectThrows(ValidationException.class, () -> productCatalogService.updateProductPrice("P-PRICE", -1.0));
        Assert.expectThrows(ResourceNotFoundException.class, () -> productCatalogService.updateProductPrice("NO-ID", 100.0));
    }

    @Test(groups = {"catalog", "regression"})
    // Проверяет фильтрацию товаров по категории и возврат пустого списка при отсутствии совпадений.
    public void shouldFilterProductsByCategoryAndHandleEmpty() {
        productCatalogService.addProduct(new Product("B-1", "Java Handbook", ProductCategory.BOOKS, 50.0));
        productCatalogService.addProduct(new Product("B-2", "Clean Code", ProductCategory.BOOKS, 40.0));

        Assert.assertEquals(productCatalogService.getProductsByCategory(ProductCategory.BOOKS).size(), 2);
        Assert.assertEquals(productCatalogService.getProductsByCategory(ProductCategory.BEAUTY).size(), 0);
        Assert.assertEquals(productCatalogService.getProductsByCategory(null).size(), 0);
    }

    @Test(groups = {"catalog", "regression"})
    // Проверяет регистронезависимый частичный поиск товаров по названию.
    public void shouldSearchProductsByNameCaseInsensitive() {
        productCatalogService.addProduct(new Product("G-1", "Organic Green Tea", ProductCategory.GROCERY, 8.5));
        productCatalogService.addProduct(new Product("G-2", "Black Tea Bags", ProductCategory.GROCERY, 6.0));

        Assert.assertEquals(productCatalogService.searchProductsByName("tea").size(), 2);
        Assert.assertEquals(productCatalogService.searchProductsByName("GREEN").size(), 1);
        Assert.assertEquals(productCatalogService.searchProductsByName("Coffee").size(), 0);
        Assert.assertEquals(productCatalogService.searchProductsByName("").size(), 0);
        Assert.assertEquals(productCatalogService.searchProductsByName(null).size(), 0);
    }

    @Test(groups = {"catalog", "regression"})
    // Проверяет деактивацию и повторную активацию товаров в каталоге.
    public void shouldActivateAndDeactivateProductsInCatalog() {
        productCatalogService.addProduct(new Product("P-ACT", "Smart Watch", ProductCategory.ELECTRONICS, 200.0));

        productCatalogService.deactivateProduct("P-ACT");
        Assert.assertFalse(productCatalogService.getProductById("P-ACT").isActive());
        Assert.assertEquals(productCatalogService.searchProductsByName("watch").size(), 0);

        productCatalogService.activateProduct("P-ACT");
        Assert.assertTrue(productCatalogService.getProductById("P-ACT").isActive());
        Assert.assertEquals(productCatalogService.searchProductsByName("watch").size(), 1);
    }

    @Test(groups = {"catalog", "regression"})
    // Проверяет фильтрацию товаров по диапазону цен с сортировкой по возрастанию цены.
    public void shouldFilterProductsInPriceRangeAndSortByPrice() {
        productCatalogService.addProduct(new Product("R-1", "Item Low", ProductCategory.HOME, 15.0));
        productCatalogService.addProduct(new Product("R-2", "Item Mid", ProductCategory.HOME, 50.0));
        productCatalogService.addProduct(new Product("R-3", "Item High", ProductCategory.HOME, 100.0));

        List<Product> inRange = productCatalogService.getProductsInPriceRange(20.0, 90.0);
        Assert.assertEquals(inRange.size(), 1);
        Assert.assertEquals(inRange.get(0).getId(), "R-2");

        Assert.expectThrows(ValidationException.class, () -> productCatalogService.getProductsInPriceRange(-1.0, 50.0));
        Assert.expectThrows(ValidationException.class, () -> productCatalogService.getProductsInPriceRange(80.0, 20.0));
    }

    @Test(groups = {"catalog", "regression"})
    // Проверяет массовое применение скидки ко всей категории товаров и подсчёт обновлённых позиций.
    public void shouldApplyCategoryDiscountToAllProductsInCategory() {
        productCatalogService.addProduct(new Product("C-1", "Shirt", ProductCategory.CLOTHING, 100.0));
        productCatalogService.addProduct(new Product("C-2", "Pants", ProductCategory.CLOTHING, 80.0));

        int count = productCatalogService.applyCategoryDiscount(ProductCategory.CLOTHING, 20.0);
        Assert.assertEquals(count, 2);
        Assert.assertEquals(productCatalogService.getProductById("C-1").getPrice(), 80.0, 0.0001);
        Assert.assertEquals(productCatalogService.getProductById("C-2").getPrice(), 64.0, 0.0001);

        Assert.expectThrows(ValidationException.class, () -> productCatalogService.applyCategoryDiscount(null, 10.0));
        Assert.expectThrows(ValidationException.class, () -> productCatalogService.applyCategoryDiscount(ProductCategory.CLOTHING, -5.0));
    }

    // =========================================================================
    // SECTION 7: INVENTORY SERVICE TESTS (InventoryService)
    // =========================================================================

    @Test(groups = {"inventory", "regression"})
    // Проверяет установку и считывание доступного и зарезервированного остатка.
    public void shouldSetAndGetAvailableAndReservedStock() {
        inventoryService.setStock("INV-1", 50);
        Assert.assertEquals(inventoryService.getStock("INV-1"), 50);
        Assert.assertEquals(inventoryService.getReservedStock("INV-1"), 0);

        Assert.assertEquals(inventoryService.getStock("UNLISTED"), 0);
        Assert.assertEquals(inventoryService.getStock(null), 0);
        Assert.assertEquals(inventoryService.getReservedStock(null), 0);
    }

    @Test(groups = {"inventory", "negative"})
    // Проверяет запрет установки отрицательного остатка или пустого ID товара.
    public void shouldRejectNegativeStockOrNullProductId() {
        Assert.expectThrows(ValidationException.class, () -> inventoryService.setStock("INV-1", -5));
        Assert.expectThrows(ValidationException.class, () -> inventoryService.setStock("", 10));
        Assert.expectThrows(ValidationException.class, () -> inventoryService.setStock(null, 10));
    }

    @Test(groups = {"inventory", "regression"})
    // Проверяет метод isInStock для запрошенных количеств относительно остатка на складе.
    public void shouldCheckIsInStockForRequestedQuantities() {
        inventoryService.setStock("INV-STOCK", 10);

        Assert.assertTrue(inventoryService.isInStock("INV-STOCK", 5));
        Assert.assertTrue(inventoryService.isInStock("INV-STOCK", 10));
        Assert.assertFalse(inventoryService.isInStock("INV-STOCK", 11));
        Assert.assertFalse(inventoryService.isInStock("INV-STOCK", 0));
        Assert.assertFalse(inventoryService.isInStock("INV-STOCK", -1));
    }

    @Test(groups = {"inventory", "regression"})
    // Проверяет перемещение единиц из доступного остатка в зарезервированный при бронировании.
    public void shouldReserveAvailableStockAndReduceAvailableCount() {
        inventoryService.setStock("INV-RES", 20);
        inventoryService.reserveStock("INV-RES", 8);

        Assert.assertEquals(inventoryService.getStock("INV-RES"), 12);
        Assert.assertEquals(inventoryService.getReservedStock("INV-RES"), 8);
    }

    @Test(expectedExceptions = InsufficientStockException.class, groups = {"inventory", "negative"})
    // Проверяет выброс InsufficientStockException при попытке зарезервировать больше остатка.
    public void shouldThrowInsufficientStockExceptionWhenReservingMoreThanAvailable() {
        inventoryService.setStock("INV-FAIL", 5);
        inventoryService.reserveStock("INV-FAIL", 6);
    }

    @Test(groups = {"inventory", "negative"})
    // Проверяет отклонение резервирования неположительного количества товара.
    public void shouldThrowValidationExceptionWhenReservingZeroOrNegativeQuantity() {
        inventoryService.setStock("INV-ZERO", 10);
        Assert.expectThrows(ValidationException.class, () -> inventoryService.reserveStock("INV-ZERO", 0));
        Assert.expectThrows(ValidationException.class, () -> inventoryService.reserveStock("INV-ZERO", -3));
    }

    @Test(groups = {"inventory", "regression"})
    // Проверяет возврат зарезервированного товара в доступный остаток при отмене брони.
    public void shouldReleaseReservedStockBackToAvailable() {
        inventoryService.setStock("INV-REL", 15);
        inventoryService.reserveStock("INV-REL", 10);
        inventoryService.releaseStock("INV-REL", 4);

        Assert.assertEquals(inventoryService.getStock("INV-REL"), 9);
        Assert.assertEquals(inventoryService.getReservedStock("INV-REL"), 6);

        Assert.expectThrows(ValidationException.class, () -> inventoryService.releaseStock("INV-REL", 7));
        Assert.expectThrows(ValidationException.class, () -> inventoryService.releaseStock("INV-REL", 0));
    }

    @Test(groups = {"inventory", "regression"})
    // Проверяет окончательное списание единиц из зарезервированного остатка.
    public void shouldDeductStockPermanentlyFromReserved() {
        inventoryService.setStock("INV-DED", 10);
        inventoryService.reserveStock("INV-DED", 6);
        inventoryService.deductStock("INV-DED", 4);

        Assert.assertEquals(inventoryService.getStock("INV-DED"), 4);
        Assert.assertEquals(inventoryService.getReservedStock("INV-DED"), 2);

        Assert.expectThrows(ValidationException.class, () -> inventoryService.deductStock("INV-DED", 5));
        Assert.expectThrows(ValidationException.class, () -> inventoryService.deductStock("INV-DED", -1));
    }

    @Test(groups = {"inventory", "regression"})
    // Проверяет пополнение склада (restock) и валидацию добавляемого количества.
    public void shouldRestockInventoryByAddingToAvailable() {
        inventoryService.setStock("INV-RESTOCK", 5);
        inventoryService.restock("INV-RESTOCK", 25);

        Assert.assertEquals(inventoryService.getStock("INV-RESTOCK"), 30);
        Assert.expectThrows(ValidationException.class, () -> inventoryService.restock("INV-RESTOCK", 0));
        Assert.expectThrows(ValidationException.class, () -> inventoryService.restock("INV-RESTOCK", -10));
    }

    // =========================================================================
    // SECTION 8: SHOPPING CART & COUPONS (ShoppingCartService)
    // =========================================================================

    @Test(groups = {"cart", "regression"})
    // Проверяет добавление товаров в корзину и инкрементальное увеличение количества одинаковых товаров.
    public void shouldAddItemsToCartAndAccumulateQuantities() {
        Product book = new Product("BK-1", "Java 17 in Action", ProductCategory.BOOKS, 35.0);
        cartService.addItem("cart-acc", book, 2);
        cartService.addItem("cart-acc", book, 3);

        Assert.assertEquals(cartService.getItemCount("cart-acc"), 5);
        Assert.assertEquals(cartService.calculateSubtotal("cart-acc"), 175.0, 0.0001);
    }

    @Test(groups = {"cart", "negative"})
    // Проверяет запрет добавления товара с количеством вне диапазона 1..99 или превышением максимума 99.
    public void shouldRejectAddingProductWithInvalidQuantityOrExceeding99() {
        Product book = new Product("BK-2", "Algorithms", ProductCategory.BOOKS, 50.0);

        Assert.expectThrows(ValidationException.class, () -> cartService.addItem("cart-limit", book, 0));
        Assert.expectThrows(ValidationException.class, () -> cartService.addItem("cart-limit", book, 100));

        cartService.addItem("cart-limit", book, 95);
        Assert.expectThrows(ValidationException.class, () -> cartService.addItem("cart-limit", book, 10)); // 95 + 10 > 99
    }

    @Test(groups = {"cart", "negative"})
    // Проверяет отказ при добавлении в корзину деактивированного товара или null.
    public void shouldRejectAddingInactiveOrNullProductToCart() {
        Product inactiveProduct = new Product("INACT-1", "Old Phone", ProductCategory.ELECTRONICS, 50.0);
        inactiveProduct.setActive(false);

        Assert.expectThrows(ValidationException.class, () -> cartService.addItem("cart-bad", null, 1));
        Assert.expectThrows(ValidationException.class, () -> cartService.addItem("cart-bad", inactiveProduct, 1));
    }

    @Test(groups = {"cart", "regression"})
    // Проверяет удаление товара из корзины и обработку ResourceNotFoundException при попытке повторного удаления.
    public void shouldRemoveItemFromCartAndThrowIfNotFound() {
        Product mouse = new Product("M-10", "Mouse", ProductCategory.ELECTRONICS, 25.0);
        cartService.addItem("cart-rm", mouse, 1);
        cartService.removeItem("cart-rm", mouse.getId());

        Assert.assertEquals(cartService.getItemCount("cart-rm"), 0);
        Assert.expectThrows(ResourceNotFoundException.class, () -> cartService.removeItem("cart-rm", mouse.getId()));
    }

    @Test(groups = {"cart", "regression"})
    // Проверяет изменение количества позиции в корзине и валидацию допустимых пределов.
    public void shouldUpdateItemQuantityAndValidateBounds() {
        Product item = new Product("P-UPD", "Headphones", ProductCategory.ELECTRONICS, 60.0);
        cartService.addItem("cart-upd", item, 2);

        cartService.updateQuantity("cart-upd", item.getId(), 5);
        Assert.assertEquals(cartService.getItemCount("cart-upd"), 5);

        Assert.expectThrows(ValidationException.class, () -> cartService.updateQuantity("cart-upd", item.getId(), 0));
        Assert.expectThrows(ValidationException.class, () -> cartService.updateQuantity("cart-upd", item.getId(), 100));
        Assert.expectThrows(ResourceNotFoundException.class, () -> cartService.updateQuantity("cart-upd", "UNKNOWN", 3));
    }

    @Test(groups = {"cart", "regression"})
    // Проверяет применение купона с фиксированной скидкой и ограничение скидки суммой subtotal.
    public void shouldApplyFixedAmountCouponAndCapAtSubtotal() {
        Product item = new Product("P-FIX", "Backpack", ProductCategory.CLOTHING, 40.0);
        cartService.addItem("cart-fix", item, 1);

        Coupon bigCoupon = new Coupon("SAVE50", DiscountType.FIXED_AMOUNT, 50.0, 0.0, LocalDate.now().plusDays(5), 10);
        cartService.applyCoupon("cart-fix", bigCoupon, LocalDate.now());

        Assert.assertEquals(cartService.calculateDiscountAmount("cart-fix"), 40.0, 0.0001); // capped at subtotal
        Assert.assertEquals(cartService.calculateTotal("cart-fix", 0.0), 0.0, 0.0001);
    }

    @Test(groups = {"cart", "negative"})
    // Проверяет отказ в применении купона с истёкшим сроком действия или исчерпанным лимитом использований.
    public void shouldRejectExpiredOrDepletedCoupon() {
        Product item = new Product("P-EXP", "Coffee", ProductCategory.GROCERY, 30.0);
        cartService.addItem("cart-coupon-val", item, 1);

        Coupon expired = new Coupon("EXPIRED", DiscountType.PERCENTAGE, 10.0, 0.0, LocalDate.now().minusDays(1), 5);
        Assert.expectThrows(InvalidCouponException.class,
                () -> cartService.applyCoupon("cart-coupon-val", expired, LocalDate.now()));

        Coupon depleted = new Coupon("DEPLETED", DiscountType.PERCENTAGE, 10.0, 0.0, LocalDate.now().plusDays(5), 1);
        depleted.incrementTimesUsed();
        Assert.expectThrows(InvalidCouponException.class,
                () -> cartService.applyCoupon("cart-coupon-val", depleted, LocalDate.now()));
    }

    @Test(groups = {"cart", "negative"})
    // Проверяет отклонение купона, если сумма корзины ниже минимального порога minOrderAmount.
    public void shouldRejectCouponWhenCartSubtotalIsBelowMinimumOrderAmount() {
        Product item = new Product("P-MIN", "Candy", ProductCategory.GROCERY, 15.0);
        cartService.addItem("cart-min", item, 1);

        Coupon minCoupon = new Coupon("MIN50", DiscountType.PERCENTAGE, 10.0, 50.0, LocalDate.now().plusDays(3), 5);
        Assert.expectThrows(InvalidCouponException.class,
                () -> cartService.applyCoupon("cart-min", minCoupon, LocalDate.now()));
    }

    @Test(groups = {"cart", "regression"})
    // Проверяет удаление купона из корзины и пересчёт итоговой суммы без скидки.
    public void shouldRemoveCouponAndRecalculateTotals() {
        Product item = new Product("P-RMV", "Shoes", ProductCategory.CLOTHING, 100.0);
        cartService.addItem("cart-rc", item, 1);

        Coupon coupon = new Coupon("DISC10", DiscountType.PERCENTAGE, 10.0, 0.0, LocalDate.now().plusDays(5), 5);
        cartService.applyCoupon("cart-rc", coupon, LocalDate.now());
        Assert.assertEquals(cartService.calculateDiscountAmount("cart-rc"), 10.0, 0.0001);

        cartService.removeCoupon("cart-rc");
        Assert.assertEquals(cartService.calculateDiscountAmount("cart-rc"), 0.0, 0.0001);
        Assert.assertEquals(cartService.calculateTotal("cart-rc", 10.0), 110.0, 0.0001);
    }

    @Test(groups = {"cart", "regression"})
    // Проверяет очистку корзины (clearCart): удаление всех позиций и применённого купона.
    public void shouldClearCartAndResetAllItemsAndCoupons() {
        Product p = new Product("P-CLR", "Watch", ProductCategory.ELECTRONICS, 200.0);
        cartService.addItem("cart-clear", p, 2);
        cartService.clearCart("cart-clear");

        Assert.assertEquals(cartService.getItemCount("cart-clear"), 0);
        Assert.assertEquals(cartService.calculateSubtotal("cart-clear"), 0.0, 0.0001);
        Assert.assertTrue(cartService.getCartItems("cart-clear").isEmpty());
    }

    // =========================================================================
    // SECTION 9: ORDER SERVICE & STATE MACHINE (OrderService)
    // =========================================================================

    @Test(groups = {"order", "regression"})
    // Проверяет корректное создание заказа в статусе PENDING с расчётом скидки по купону и налога.
    public void shouldCreatePendingOrderWithCouponAndTax() {
        Product prod = new Product("ORD-C", "Monitor", ProductCategory.ELECTRONICS, 200.0);
        Address address = new Address("1 Bagramyan Ave", "Yerevan", "Center", "0019", "AM");
        Coupon coupon = new Coupon("SALE20", DiscountType.PERCENTAGE, 20.0, 100.0, LocalDate.now().plusDays(5), 2);

        Order order = orderService.createOrder("user-10", List.of(new CartItem(prod, 1)), address, 10.0, coupon);

        Assert.assertEquals(order.getStatus(), OrderStatus.PENDING);
        Assert.assertEquals(order.getSubtotal(), 200.0, 0.0001);
        Assert.assertEquals(order.getDiscount(), 40.0, 0.0001);
        Assert.assertEquals(order.getTax(), 16.0, 0.0001); // 10% of 160
        Assert.assertEquals(order.getTotal(), 176.0, 0.0001);
    }

    @Test(groups = {"order", "negative"})
    // Проверяет отказ в создании заказа при невалидных входных данных (пустые позиции, null адрес, невалидный налог).
    public void shouldRejectOrderCreationWithInvalidParameters() {
        Address addr = new Address("Street", "City", "State", "0001", "AM");
        List<CartItem> items = List.of(new CartItem(new Product("P", "N", ProductCategory.BOOKS, 10.0), 1));

        Assert.expectThrows(ValidationException.class, () -> orderService.createOrder("", items, addr, 20.0, null));
        Assert.expectThrows(ValidationException.class, () -> orderService.createOrder("user", null, addr, 20.0, null));
        Assert.expectThrows(ValidationException.class, () -> orderService.createOrder("user", List.of(), addr, 20.0, null));
        Assert.expectThrows(ValidationException.class, () -> orderService.createOrder("user", items, null, 20.0, null));
        Assert.expectThrows(ValidationException.class, () -> orderService.createOrder("user", items, addr, -5.0, null));
        Assert.expectThrows(ValidationException.class, () -> orderService.createOrder("user", items, addr, 105.0, null));
    }

    @Test(expectedExceptions = PaymentFailedException.class, groups = {"order", "negative"})
    // Проверяет выброс PaymentFailedException при несоответствии суммы платежа стоимости заказа.
    public void shouldRejectPaymentWhenAmountDoesNotMatchOrderTotal() {
        Product prod = new Product("ORD-M", "Case", ProductCategory.ELECTRONICS, 50.0);
        Order order = orderService.createOrder("u-mismatch", List.of(new CartItem(prod, 1)),
                new Address("S", "C", "ST", "0001", "AM"), 0.0, null);

        orderService.payOrder(order.getId(), "4111111111111111", 49.0); // total is 50.0
    }

    @Test(expectedExceptions = IllegalOrderStateException.class, groups = {"order", "negative"})
    // Проверяет запрет повторной оплаты уже оплаченного заказа.
    public void shouldRejectPaymentForNonPendingOrder() {
        Product prod = new Product("ORD-RP", "Cable", ProductCategory.ELECTRONICS, 20.0);
        Order order = orderService.createOrder("u-rep", List.of(new CartItem(prod, 1)),
                new Address("S", "C", "ST", "0001", "AM"), 0.0, null);

        orderService.payOrder(order.getId(), "4111111111111111", 20.0);
        orderService.payOrder(order.getId(), "4111111111111111", 20.0); // already PAID
    }

    @Test(groups = {"order", "regression"})
    // Проверяет отправку оплаченного заказа с присвоением номера отслеживания.
    public void shouldShipPaidOrderAndRejectEmptyTrackingNumber() {
        Product prod = new Product("ORD-SH", "Printer", ProductCategory.ELECTRONICS, 150.0);
        Order order = orderService.createOrder("u-ship", List.of(new CartItem(prod, 1)),
                new Address("S", "C", "ST", "0001", "AM"), 0.0, null);

        orderService.payOrder(order.getId(), "4111111111111111", 150.0);

        Assert.expectThrows(ValidationException.class, () -> orderService.shipOrder(order.getId(), ""));
        Assert.expectThrows(ValidationException.class, () -> orderService.shipOrder(order.getId(), null));

        orderService.shipOrder(order.getId(), "TRACK-9999");
        Assert.assertEquals(orderService.getOrder(order.getId()).getStatus(), OrderStatus.SHIPPED);
        Assert.assertEquals(orderService.getOrder(order.getId()).getTrackingNumber(), "TRACK-9999");
    }

    @Test(expectedExceptions = IllegalOrderStateException.class, groups = {"order", "negative"})
    // Проверяет запрет отправки неоплаченного (PENDING) заказа.
    public void shouldRejectShippingUnpaidOrder() {
        Product prod = new Product("ORD-UNP", "Scanner", ProductCategory.ELECTRONICS, 90.0);
        Order order = orderService.createOrder("u-unpaid", List.of(new CartItem(prod, 1)),
                new Address("S", "C", "ST", "0001", "AM"), 0.0, null);

        orderService.shipOrder(order.getId(), "TRACK-ILLEGAL");
    }

    @Test(groups = {"order", "regression"})
    // Проверяет переход доставленного заказа в DELIVERED и запрет доставки неоправленного заказа.
    public void shouldDeliverShippedOrderAndRejectDeliveringNonShipped() {
        Product prod = new Product("ORD-DEL", "Camera", ProductCategory.ELECTRONICS, 500.0);
        Order order = orderService.createOrder("u-del", List.of(new CartItem(prod, 1)),
                new Address("S", "C", "ST", "0001", "AM"), 0.0, null);

        Assert.expectThrows(IllegalOrderStateException.class, () -> orderService.deliverOrder(order.getId()));

        orderService.payOrder(order.getId(), "4111111111111111", 500.0);
        Assert.expectThrows(IllegalOrderStateException.class, () -> orderService.deliverOrder(order.getId()));

        orderService.shipOrder(order.getId(), "TRACK-DEL");
        orderService.deliverOrder(order.getId());
        Assert.assertEquals(orderService.getOrder(order.getId()).getStatus(), OrderStatus.DELIVERED);
    }

    @Test(groups = {"order", "regression"})
    // Проверяет отмену заказов в статусах PENDING и PAID.
    public void shouldCancelPendingAndPaidOrdersSuccessfully() {
        Product prod = new Product("ORD-CNC", "Watch", ProductCategory.ELECTRONICS, 80.0);
        Address addr = new Address("S", "C", "ST", "0001", "AM");

        Order order1 = orderService.createOrder("u-c1", List.of(new CartItem(prod, 1)), addr, 0.0, null);
        orderService.cancelOrder(order1.getId());
        Assert.assertEquals(orderService.getOrder(order1.getId()).getStatus(), OrderStatus.CANCELLED);

        Order order2 = orderService.createOrder("u-c2", List.of(new CartItem(prod, 1)), addr, 0.0, null);
        orderService.payOrder(order2.getId(), "4111111111111111", 80.0);
        orderService.cancelOrder(order2.getId());
        Assert.assertEquals(orderService.getOrder(order2.getId()).getStatus(), OrderStatus.CANCELLED);
    }

    @Test(groups = {"order", "negative"})
    // Проверяет запрет отмены уже отправленного, доставленного или повторной отмены аннулированного заказа.
    public void shouldRejectCancellationOfShippedOrDeliveredOrders() {
        Product prod = new Product("ORD-NOCNC", "Lens", ProductCategory.ELECTRONICS, 200.0);
        Order order = orderService.createOrder("u-nocnc", List.of(new CartItem(prod, 1)),
                new Address("S", "C", "ST", "0001", "AM"), 0.0, null);

        orderService.payOrder(order.getId(), "4111111111111111", 200.0);
        orderService.shipOrder(order.getId(), "TRACK-NC");

        Assert.expectThrows(IllegalOrderStateException.class, () -> orderService.cancelOrder(order.getId()));

        orderService.deliverOrder(order.getId());
        Assert.expectThrows(IllegalOrderStateException.class, () -> orderService.cancelOrder(order.getId()));
    }

    @Test(groups = {"order", "regression"})
    // Проверяет возврат средств (refund) по оплаченному заказу и валидацию суммы возврата.
    public void shouldRefundPaidOrderAndRejectInvalidRefundAmounts() {
        Product prod = new Product("ORD-REF", "Mic", ProductCategory.ELECTRONICS, 100.0);
        Order order = orderService.createOrder("u-ref", List.of(new CartItem(prod, 1)),
                new Address("S", "C", "ST", "0001", "AM"), 0.0, null);

        Assert.expectThrows(IllegalOrderStateException.class, () -> orderService.refundOrder(order.getId(), 50.0));

        orderService.payOrder(order.getId(), "4111111111111111", 100.0);

        Assert.expectThrows(ValidationException.class, () -> orderService.refundOrder(order.getId(), 0.0));
        Assert.expectThrows(ValidationException.class, () -> orderService.refundOrder(order.getId(), 150.0)); // > total

        orderService.refundOrder(order.getId(), 100.0);
        Assert.assertEquals(orderService.getOrder(order.getId()).getStatus(), OrderStatus.REFUNDED);
    }

    @Test(groups = {"order", "regression"})
    // Проверяет выборку заказов по идентификатору пользователя и по статусу заказа.
    public void shouldRetrieveOrdersByUserAndByStatus() {
        Product prod = new Product("ORD-FLT", "Paper", ProductCategory.HOME, 10.0);
        Address addr = new Address("S", "C", "ST", "0001", "AM");

        Order o1 = orderService.createOrder("cust-x", List.of(new CartItem(prod, 1)), addr, 0.0, null);
        Order o2 = orderService.createOrder("cust-x", List.of(new CartItem(prod, 2)), addr, 0.0, null);
        Order o3 = orderService.createOrder("cust-y", List.of(new CartItem(prod, 3)), addr, 0.0, null);

        Assert.assertEquals(orderService.getOrdersByUser("cust-x").size(), 2);
        Assert.assertEquals(orderService.getOrdersByUser("cust-y").size(), 1);
        Assert.assertEquals(orderService.getOrdersByUser(null).size(), 0);

        Assert.assertEquals(orderService.getOrdersByStatus(OrderStatus.PENDING).size(), 3);
        Assert.assertEquals(orderService.getOrdersByStatus(OrderStatus.SHIPPED).size(), 0);
        Assert.assertEquals(orderService.getOrdersByStatus(null).size(), 0);
    }

    // =========================================================================
    // SECTION 10: LOYALTY & REWARDS TESTS (LoyaltyService)
    // =========================================================================

    @Test(groups = {"loyalty", "regression"})
    // Проверяет начисление баллов лояльности и отклонение неположительных значений или пустого пользователя.
    public void shouldAddPointsAndRejectZeroOrNegativePointsOrEmptyUser() {
        loyaltyService.addPoints("loyalty-u1", 150);
        Assert.assertEquals(loyaltyService.getPoints("loyalty-u1"), 150);
        Assert.assertEquals(loyaltyService.getLifetimePoints("loyalty-u1"), 150);

        Assert.expectThrows(ValidationException.class, () -> loyaltyService.addPoints("loyalty-u1", 0));
        Assert.expectThrows(ValidationException.class, () -> loyaltyService.addPoints("loyalty-u1", -50));
        Assert.expectThrows(ValidationException.class, () -> loyaltyService.addPoints("", 100));
        Assert.expectThrows(ValidationException.class, () -> loyaltyService.addPoints(null, 100));
    }

    @Test(groups = {"loyalty", "regression"})
    // Проверяет списание баллов с баланса без уменьшения накопленных пожизненных баллов (lifetime).
    public void shouldRedeemPointsWithoutReducingLifetimePoints() {
        loyaltyService.addPoints("loyalty-u2", 500);
        loyaltyService.redeemPoints("loyalty-u2", 200);

        Assert.assertEquals(loyaltyService.getPoints("loyalty-u2"), 300);
        Assert.assertEquals(loyaltyService.getLifetimePoints("loyalty-u2"), 500);
    }

    @Test(groups = {"loyalty", "negative"})
    // Проверяет отказ при списании большего количества баллов, чем доступно на балансе.
    public void shouldRejectRedeemingMorePointsThanAvailableOrNonPositive() {
        loyaltyService.addPoints("loyalty-u3", 100);

        Assert.expectThrows(ValidationException.class, () -> loyaltyService.redeemPoints("loyalty-u3", 150));
        Assert.expectThrows(ValidationException.class, () -> loyaltyService.redeemPoints("loyalty-u3", 0));
        Assert.expectThrows(ValidationException.class, () -> loyaltyService.redeemPoints("loyalty-u3", -10));
    }

    @DataProvider(name = "loyaltyTierProvider")
    public Object[][] provideTierThresholds() {
        return new Object[][]{
                {0, LoyaltyTier.BRONZE},
                {999, LoyaltyTier.BRONZE},
                {1000, LoyaltyTier.SILVER},
                {4999, LoyaltyTier.SILVER},
                {5000, LoyaltyTier.GOLD},
                {9999, LoyaltyTier.GOLD},
                {10000, LoyaltyTier.PLATINUM},
                {50000, LoyaltyTier.PLATINUM}
        };
    }

    @Test(dataProvider = "loyaltyTierProvider", groups = {"loyalty", "regression"})
    // Проверяет определение уровня лояльности на граничных значениях баллов (0..999, 1000..4999, 5000..9999, 10000+).
    public void shouldCalculateLoyaltyTierAtBoundaryValues(int points, LoyaltyTier expectedTier) {
        Assert.assertEquals(loyaltyService.calculateTier(points), expectedTier);
    }

    @Test(expectedExceptions = ValidationException.class, groups = {"loyalty", "negative"})
    // Проверяет выброс ValidationException при передаче отрицательных баллов в расчёт уровня.
    public void shouldRejectNegativeLifetimePointsForTierCalculation() {
        loyaltyService.calculateTier(-1);
    }

    @Test(groups = {"loyalty", "regression"})
    // Проверяет сохранение уровня лояльности пользователя на основе пожизненных баллов даже после списания.
    public void shouldMaintainUserTierAfterPointsRedemption() {
        loyaltyService.addPoints("loyalty-u4", 5000);
        Assert.assertEquals(loyaltyService.getUserTier("loyalty-u4"), LoyaltyTier.GOLD);

        loyaltyService.redeemPoints("loyalty-u4", 4500);
        Assert.assertEquals(loyaltyService.getPoints("loyalty-u4"), 500);
        Assert.assertEquals(loyaltyService.getUserTier("loyalty-u4"), LoyaltyTier.GOLD);
    }

    @Test(groups = {"loyalty", "regression"})
    // Проверяет проценты персональной скидки для каждого уровня лояльности (BRONZE 0%, SILVER 5%, GOLD 10%, PLATINUM 15%).
    public void shouldReturnAccurateTierDiscountPercentages() {
        Assert.assertEquals(loyaltyService.getTierDiscountPercentage(LoyaltyTier.BRONZE), 0.0, 0.0001);
        Assert.assertEquals(loyaltyService.getTierDiscountPercentage(LoyaltyTier.SILVER), 5.0, 0.0001);
        Assert.assertEquals(loyaltyService.getTierDiscountPercentage(LoyaltyTier.GOLD), 10.0, 0.0001);
        Assert.assertEquals(loyaltyService.getTierDiscountPercentage(LoyaltyTier.PLATINUM), 15.0, 0.0001);
        Assert.assertEquals(loyaltyService.getTierDiscountPercentage(null), 0.0, 0.0001);
    }

    @Test(groups = {"loyalty", "regression"})
    // Проверяет перевод баллов между клиентами с обновлением текущего баланса и lifetime получателя.
    public void shouldTransferPointsBetweenUsersAndAdjustBalances() {
        loyaltyService.addPoints("sender-u", 300);
        loyaltyService.addPoints("receiver-u", 50);

        loyaltyService.transferPoints("sender-u", "receiver-u", 100);

        Assert.assertEquals(loyaltyService.getPoints("sender-u"), 200);
        Assert.assertEquals(loyaltyService.getPoints("receiver-u"), 150);
        Assert.assertEquals(loyaltyService.getLifetimePoints("receiver-u"), 150);
    }

    @Test(groups = {"loyalty", "negative"})
    // Проверяет отказ в переводе баллов самому себе или при превышении доступного баланса.
    public void shouldRejectTransferPointsToSameUserOrInsufficientBalanceOrNonPositive() {
        loyaltyService.addPoints("sender-bad", 100);

        Assert.expectThrows(ValidationException.class,
                () -> loyaltyService.transferPoints("sender-bad", "sender-bad", 50));
        Assert.expectThrows(ValidationException.class,
                () -> loyaltyService.transferPoints("sender-bad", "other-u", 150));
        Assert.expectThrows(ValidationException.class,
                () -> loyaltyService.transferPoints("sender-bad", "other-u", 0));
        Assert.expectThrows(ValidationException.class,
                () -> loyaltyService.transferPoints("sender-bad", "other-u", -20));
    }

    // =========================================================================
    // SECTION 11: ADVANCED SCENARIOS & FULL MATRIX VERIFICATION
    // =========================================================================

    private static Order sharedOrder;
    private static OrderService sharedOrderService;

    @Test(groups = {"order", "regression"})
    // Проверяет шаг 1 жизненного цикла заказа: создание заказа в статусе PENDING для цепочки dependsOnMethods.
    public void step1_shouldCreatePendingOrderForChain() {
        sharedOrderService = new OrderService();
        Product product = new Product("P-SEQ", "Sequential Product", ProductCategory.ELECTRONICS, 100.0);
        Address address = new Address("1 Sequential St", "Yerevan", "Center", "0001", "AM");
        sharedOrder = sharedOrderService.createOrder("user-seq", List.of(new CartItem(product, 1)), address, 20.0, null);
        Assert.assertEquals(sharedOrder.getStatus(), OrderStatus.PENDING);
        Assert.assertEquals(sharedOrder.getTotal(), 120.0, 0.0001);
    }

    @Test(dependsOnMethods = "step1_shouldCreatePendingOrderForChain", groups = {"order", "regression"})
    // Проверяет шаг 2 жизненного цикла заказа: оплата заказа (PENDING -> PAID) в цепочке dependsOnMethods.
    public void step2_shouldPayOrderInChain() {
        sharedOrderService.payOrder(sharedOrder.getId(), "4111111111111111", sharedOrder.getTotal());
        Assert.assertEquals(sharedOrderService.getOrder(sharedOrder.getId()).getStatus(), OrderStatus.PAID);
    }

    @Test(dependsOnMethods = "step2_shouldPayOrderInChain", groups = {"order", "regression"})
    // Проверяет шаг 3 жизненного цикла заказа: отправка заказа (PAID -> SHIPPED) в цепочке dependsOnMethods.
    public void step3_shouldShipOrderInChain() {
        sharedOrderService.shipOrder(sharedOrder.getId(), "TRACK-CHAIN-001");
        Assert.assertEquals(sharedOrderService.getOrder(sharedOrder.getId()).getStatus(), OrderStatus.SHIPPED);
    }

    @Test(dependsOnMethods = "step3_shouldShipOrderInChain", groups = {"order", "regression"})
    // Проверяет шаг 4 жизненного цикла заказа: доставка заказа (SHIPPED -> DELIVERED) в цепочке dependsOnMethods.
    public void step4_shouldDeliverOrderInChain() {
        sharedOrderService.deliverOrder(sharedOrder.getId());
        Assert.assertEquals(sharedOrderService.getOrder(sharedOrder.getId()).getStatus(), OrderStatus.DELIVERED);
    }

    @Test(groups = {"inventory", "regression"})
    // Проверяет потокобезопасность и атомарность резервирования склада при конкурентных запросах.
    public void shouldHandleConcurrentStockReservationsAtomically() throws InterruptedException {
        String prodId = "CONC-PROD";
        inventoryService.setStock(prodId, 5);

        int threadCount = 10;
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(threadCount);
        java.util.concurrent.atomic.AtomicInteger successCount = new java.util.concurrent.atomic.AtomicInteger(0);
        java.util.concurrent.atomic.AtomicInteger failureCount = new java.util.concurrent.atomic.AtomicInteger(0);
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    inventoryService.reserveStock(prodId, 1);
                    successCount.incrementAndGet();
                } catch (InsufficientStockException e) {
                    failureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, java.util.concurrent.TimeUnit.SECONDS);
        executor.shutdown();

        Assert.assertEquals(successCount.get(), 5, "Ровно 5 потоков должны успешно зарезервировать товар");
        Assert.assertEquals(failureCount.get(), 5, "Ровно 5 потоков должны получить InsufficientStockException");
        Assert.assertEquals(inventoryService.getStock(prodId), 0);
        Assert.assertEquals(inventoryService.getReservedStock(prodId), 5);
    }

    @Test(groups = {"security", "regression"})
    // Проверяет получение неизменяемого списка всех зарегистрированных пользователей и защиту от модификации.
    public void shouldReturnUnmodifiableListOfAllRegisteredUsers() {
        userService.registerUser("user_all_1", "all1@example.com", "StrongPass1!", Role.CUSTOMER);
        userService.registerUser("user_all_2", "all2@example.com", "StrongPass1!", Role.MANAGER);

        List<User> users = userService.getAllUsers();
        Assert.assertTrue(users.size() >= 2);
        Assert.expectThrows(UnsupportedOperationException.class, () -> users.add(null));
    }

    @Test(groups = {"catalog", "regression"})
    // Проверяет получение списка только активных товаров каталога с исключением деактивированных.
    public void shouldReturnOnlyActiveProductsFromCatalog() {
        productCatalogService.addProduct(new Product("ACT-1", "Active Item", ProductCategory.BOOKS, 20.0));
        Product p2 = productCatalogService.addProduct(new Product("ACT-2", "Inactive Item", ProductCategory.BOOKS, 30.0));
        productCatalogService.deactivateProduct(p2.getId());

        List<Product> activeProducts = productCatalogService.getAllActiveProducts();
        Assert.assertTrue(activeProducts.stream().anyMatch(p -> p.getId().equals("ACT-1")));
        Assert.assertFalse(activeProducts.stream().anyMatch(p -> p.getId().equals("ACT-2")));
    }

    @Test(groups = {"catalog", "negative"})
    // Проверяет выброс ResourceNotFoundException при активации или деактивации несуществующего товара.
    public void shouldThrowResourceNotFoundWhenActivatingOrDeactivatingMissingProduct() {
        Assert.expectThrows(ResourceNotFoundException.class, () -> productCatalogService.deactivateProduct("NON_EXISTENT"));
        Assert.expectThrows(ResourceNotFoundException.class, () -> productCatalogService.activateProduct("NON_EXISTENT"));
    }

    @Test(groups = {"security", "negative"})
    // Проверяет выброс ResourceNotFoundException при попытке поиска или изменения несуществующего пользователя.
    public void shouldThrowResourceNotFoundForMissingUserLookupsAndOperations() {
        Assert.expectThrows(ResourceNotFoundException.class, () -> userService.getUserById("MISSING-ID"));
        Assert.expectThrows(ResourceNotFoundException.class, () -> userService.getUserById(null));
        Assert.expectThrows(ResourceNotFoundException.class, () -> userService.getUserByUsername("MISSING_USER"));
        Assert.expectThrows(ResourceNotFoundException.class, () -> userService.getUserByUsername(null));
        Assert.expectThrows(ResourceNotFoundException.class, () -> userService.lockUser("MISSING-ID"));
        Assert.expectThrows(ResourceNotFoundException.class, () -> userService.unlockUser("MISSING-ID"));
        Assert.expectThrows(ResourceNotFoundException.class, () -> userService.deactivateUser("MISSING-ID"));
        Assert.expectThrows(ResourceNotFoundException.class, () -> userService.hasPermission("MISSING-ID", "READ"));
        Assert.expectThrows(ResourceNotFoundException.class, () -> userService.changePassword("MISSING-ID", "Old1!", "New1!"));
        Assert.expectThrows(ResourceNotFoundException.class, () -> userService.updateEmail("MISSING-ID", "new@example.com"));
    }

    @Test(groups = {"order", "negative"})
    // Проверяет выброс ResourceNotFoundException при операциях с несуществующим заказом.
    public void shouldThrowResourceNotFoundForMissingOrderOperations() {
        Assert.expectThrows(ResourceNotFoundException.class, () -> orderService.getOrder("MISSING-ORD"));
        Assert.expectThrows(ResourceNotFoundException.class, () -> orderService.getOrder(null));
        Assert.expectThrows(ResourceNotFoundException.class, () -> orderService.payOrder("MISSING-ORD", "4111111111111111", 100.0));
        Assert.expectThrows(ResourceNotFoundException.class, () -> orderService.shipOrder("MISSING-ORD", "TRACK-1"));
        Assert.expectThrows(ResourceNotFoundException.class, () -> orderService.deliverOrder("MISSING-ORD"));
        Assert.expectThrows(ResourceNotFoundException.class, () -> orderService.cancelOrder("MISSING-ORD"));
        Assert.expectThrows(ResourceNotFoundException.class, () -> orderService.refundOrder("MISSING-ORD", 10.0));
    }

    @Test(groups = {"cart", "negative"})
    // Проверяет валидацию cartId (запрет null и пустых значений) во всех операциях корзины.
    public void shouldValidateCartIdNotNullOrEmptyInCartOperations() {
        Product p = new Product("CART-V", "Item", ProductCategory.BOOKS, 15.0);
        Assert.expectThrows(ValidationException.class, () -> cartService.addItem("", p, 1));
        Assert.expectThrows(ValidationException.class, () -> cartService.addItem(null, p, 1));
        Assert.expectThrows(ValidationException.class, () -> cartService.removeItem("", "P"));
        Assert.expectThrows(ValidationException.class, () -> cartService.updateQuantity("", "P", 2));
        Assert.expectThrows(ValidationException.class, () -> cartService.calculateSubtotal(""));
        Assert.expectThrows(ValidationException.class, () -> cartService.calculateDiscountAmount(""));
        Assert.expectThrows(ValidationException.class, () -> cartService.calculateTotal("", 10.0));
        Assert.expectThrows(ValidationException.class, () -> cartService.clearCart(""));
        Assert.expectThrows(ValidationException.class, () -> cartService.getCartItems(""));
        Assert.expectThrows(ValidationException.class, () -> cartService.getItemCount(""));
    }

    @Test(groups = {"order", "regression"})
    // Проверяет частичный возврат средств (partial refund) по оплаченному заказу.
    public void shouldSupportPartialRefundOfPaidOrder() {
        Product prod = new Product("ORD-PART", "Keyboard", ProductCategory.ELECTRONICS, 100.0);
        Order order = orderService.createOrder("u-part", List.of(new CartItem(prod, 1)),
                new Address("S", "C", "ST", "0001", "AM"), 0.0, null);
        orderService.payOrder(order.getId(), "4111111111111111", 100.0);

        orderService.refundOrder(order.getId(), 40.0);
        Assert.assertEquals(orderService.getOrder(order.getId()).getStatus(), OrderStatus.REFUNDED);
    }

    @Test(groups = {"loyalty", "regression"})
    // Проверяет корректные значения по умолчанию для неизвестного или null пользователя в сервисе лояльности.
    public void shouldReturnZeroAndBronzeForNonExistentOrNullUser() {
        Assert.assertEquals(loyaltyService.getPoints(null), 0);
        Assert.assertEquals(loyaltyService.getPoints("UNREGISTERED"), 0);
        Assert.assertEquals(loyaltyService.getLifetimePoints(null), 0);
        Assert.assertEquals(loyaltyService.getLifetimePoints("UNREGISTERED"), 0);
        Assert.assertEquals(loyaltyService.getUserTier(null), LoyaltyTier.BRONZE);
        Assert.assertEquals(loyaltyService.getUserTier("UNREGISTERED"), LoyaltyTier.BRONZE);
    }
}
