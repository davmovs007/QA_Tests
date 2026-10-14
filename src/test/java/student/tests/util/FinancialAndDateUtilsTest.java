package student.tests.util;

import am.gortsup.testing.util.DateTimeUtils;
import am.gortsup.testing.util.MathAndFinanceUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.LocalDate;

public class FinancialAndDateUtilsTest {

    @Test(groups = {"smoke", "regression"})
    // Проверяет расчёт налога, скидки, округления и Luhn-проверки карты.
    public void shouldCalculateFinancialValues() {
        Assert.assertEquals(MathAndFinanceUtils.calculateTax(100.0, 20.0), 20.0, 0.0001);
        Assert.assertEquals(MathAndFinanceUtils.calculateDiscount(100.0, 10.0), 90.0, 0.0001);
        Assert.assertEquals(MathAndFinanceUtils.roundToDecimalPlaces(12.345, 2), 12.35, 0.0001);
        Assert.assertTrue(MathAndFinanceUtils.isValidLuhn("4111111111111111"));
        Assert.assertFalse(MathAndFinanceUtils.isValidLuhn("1234567812345678"));
        Assert.assertEquals(MathAndFinanceUtils.calculateMonthlyInstallment(1200.0, 0.0, 12), 100.0, 0.0001);
    }

    @Test(groups = {"smoke", "regression"})
    // Проверяет расчёт рабочих дней, диапазоны дат и пересечение временных интервалов.
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

    @Test(groups = {"smoke", "regression"})
    // Проверяет корректную работу високосного года и добавления рабочих дней.
    public void shouldSupportLeapYearAndBusinessDayShift() {
        Assert.assertTrue(DateTimeUtils.isLeapYear(2024));
        Assert.assertFalse(DateTimeUtils.isLeapYear(1900));
        Assert.assertEquals(DateTimeUtils.addBusinessDays(LocalDate.of(2024, 6, 7), 2), LocalDate.of(2024, 6, 11));
    }
}
