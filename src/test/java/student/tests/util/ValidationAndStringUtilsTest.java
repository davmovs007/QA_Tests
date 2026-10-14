package student.tests.util;

import am.gortsup.testing.util.StringUtils;
import am.gortsup.testing.util.ValidationUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ValidationAndStringUtilsTest {

    @Test(groups = {"smoke", "regression"})
    // Проверяет корректную валидацию email, password и phone number.
    public void shouldValidateUserInputFormats() {
        Assert.assertTrue(ValidationUtils.isValidEmail("john.doe@example.com"));
        Assert.assertFalse(ValidationUtils.isValidEmail("invalid@email"));
        Assert.assertTrue(ValidationUtils.isValidPassword("StrongPass1!"));
        Assert.assertFalse(ValidationUtils.isValidPassword("weakpass"));
        Assert.assertTrue(ValidationUtils.isValidPhoneNumber("+1 (415) 555-1234"));
        Assert.assertFalse(ValidationUtils.isValidPhoneNumber("abc"));
    }

    @Test(groups = {"smoke", "regression"})
    // Проверяет нормализацию строки в slug, разворот слов и маскирование карты/карточки.
    public void shouldNormalizeTextAndMaskSensitiveValues() {
        Assert.assertEquals(StringUtils.toSlug("Java Automation Course!"), "java-automation-course");
        Assert.assertEquals(StringUtils.reverseWords("alpha beta gamma"), "gamma beta alpha");
        Assert.assertTrue(StringUtils.isPalindrome("A man, a plan, a canal: Panama"));
        Assert.assertEquals(StringUtils.maskCreditCard("4111 2222 3333 4444"), "************4444");
        Assert.assertEquals(StringUtils.countOccurrences("banana", "na"), 2);
    }

    @Test(groups = {"smoke", "regression"})
    // Проверяет безопасную очистку пользовательского ввода и обработку null/blank значений.
    public void shouldSanitizeInputAndHandleNulls() {
        Assert.assertEquals(ValidationUtils.sanitizeInput("<script>alert(1)</script>"), "&lt;script&gt;alert(1)&lt;/script&gt;");
        Assert.assertEquals(ValidationUtils.sanitizeInput(null), "");
        Assert.assertTrue(ValidationUtils.isPositiveNumber(5));
        Assert.assertFalse(ValidationUtils.isPositiveNumber(-1));
    }
}
