package student.tests.service;

import am.gortsup.testing.exception.ValidationException;
import am.gortsup.testing.model.LoyaltyTier;
import am.gortsup.testing.service.LoyaltyService;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class LoyaltyServiceTest {

    private LoyaltyService loyaltyService;

    @BeforeMethod(groups = {"smoke", "regression", "negative"})
    public void setUp() {
        loyaltyService = new LoyaltyService();
    }

    @Test(groups = {"smoke", "regression"})
    // Проверяет начисление баллов и корректный переход между уровнями лояльности.
    public void shouldEarnPointsAndReachTierThresholds() {
        loyaltyService.addPoints("user-10", 999);
        Assert.assertEquals(loyaltyService.getUserTier("user-10"), LoyaltyTier.BRONZE);

        loyaltyService.addPoints("user-10", 1);
        Assert.assertEquals(loyaltyService.getUserTier("user-10"), LoyaltyTier.SILVER);

        loyaltyService.addPoints("user-20", 5000);
        Assert.assertEquals(loyaltyService.getUserTier("user-20"), LoyaltyTier.GOLD);
        Assert.assertEquals(loyaltyService.getTierDiscountPercentage(LoyaltyTier.GOLD), 10.0, 0.0001);

        loyaltyService.addPoints("user-30", 10000);
        Assert.assertEquals(loyaltyService.getUserTier("user-30"), LoyaltyTier.PLATINUM);
    }

    @Test(groups = {"negative", "regression"})
    // Проверяет списание баллов и перевод между пользователями с валидацией ошибок.
    public void shouldRedeemAndTransferPoints() {
        loyaltyService.addPoints("user-40", 500);
        loyaltyService.redeemPoints("user-40", 200);
        Assert.assertEquals(loyaltyService.getPoints("user-40"), 300);

        loyaltyService.addPoints("user-50", 100);
        loyaltyService.transferPoints("user-40", "user-50", 100);
        Assert.assertEquals(loyaltyService.getPoints("user-40"), 200);
        Assert.assertEquals(loyaltyService.getPoints("user-50"), 200);

        Assert.expectThrows(ValidationException.class,
                () -> loyaltyService.transferPoints("user-40", "user-40", 10));
    }
}
