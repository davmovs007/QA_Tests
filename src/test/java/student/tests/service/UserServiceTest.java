package student.tests.service;

import am.gortsup.testing.exception.AccountLockedException;
import am.gortsup.testing.exception.AuthenticationException;
import am.gortsup.testing.model.Role;
import am.gortsup.testing.model.User;
import am.gortsup.testing.service.UserService;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class UserServiceTest {

    private UserService userService;

    @BeforeMethod(groups = {"smoke", "regression", "negative"})
    public void setUp() {
        userService = new UserService();
    }

    @Test(groups = {"smoke", "regression"})
    // Проверяет регистрацию пользователя, получение по username и права доступа по роли.
    public void shouldRegisterUserAndCheckPermissions() {
        User user = userService.registerUser("alice_user", "alice@example.com", "StrongPass1!", Role.CUSTOMER);

        Assert.assertEquals(user.getUsername(), "alice_user");
        Assert.assertEquals(userService.getUserByUsername("ALICE_USER").getId(), user.getId());
        Assert.assertTrue(userService.hasPermission(user.getId(), "READ"));
        Assert.assertTrue(userService.hasPermission(user.getId(), "CREATE_ORDER"));
        Assert.assertFalse(userService.hasPermission(user.getId(), "MANAGE_INVENTORY"));
    }

    @Test(groups = {"negative", "regression"})
    // Проверяет блокировку аккаунта после трёх неудачных входов и последующее снятие блокировки.
    public void shouldLockAccountAfterThreeFailedAttempts() {
        User user = userService.registerUser("bob_user", "bob@example.com", "StrongPass1!", Role.CUSTOMER);

        Assert.expectThrows(AuthenticationException.class, () -> userService.authenticate("bob_user", "WrongPass1!"));
        Assert.expectThrows(AuthenticationException.class, () -> userService.authenticate("bob_user", "AnotherBad1!"));
        Assert.expectThrows(AccountLockedException.class, () -> userService.authenticate("bob_user", "BadPass2!"));

        userService.unlockUser(user.getId());
        Assert.assertEquals(userService.authenticate("bob_user", "StrongPass1!").getId(), user.getId());
    }
}
