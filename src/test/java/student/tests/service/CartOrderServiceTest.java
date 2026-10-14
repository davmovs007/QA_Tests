package student.tests.service;

import am.gortsup.testing.exception.PaymentFailedException;
import am.gortsup.testing.model.Address;
import am.gortsup.testing.model.CartItem;
import am.gortsup.testing.model.Coupon;
import am.gortsup.testing.model.DiscountType;
import am.gortsup.testing.model.Order;
import am.gortsup.testing.model.OrderStatus;
import am.gortsup.testing.model.Product;
import am.gortsup.testing.model.ProductCategory;
import am.gortsup.testing.service.OrderService;
import am.gortsup.testing.service.ShoppingCartService;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.util.List;

public class CartOrderServiceTest {

    private ShoppingCartService cartService;
    private OrderService orderService;

    @BeforeMethod(groups = {"smoke", "regression", "negative"})
    public void setUp() {
        cartService = new ShoppingCartService();
        orderService = new OrderService();
    }

    @Test(groups = {"smoke", "regression"})
    // Проверяет расчёт subtotal и количества товаров в корзине перед оформлением заказа.
    public void shouldCalculateSubtotalAndItemCount() {
        Product laptop = new Product("L-1", "Laptop", ProductCategory.ELECTRONICS, 100.0);
        Product mouse = new Product("M-1", "Mouse", ProductCategory.ELECTRONICS, 50.0);

        cartService.addItem("cart-1", laptop, 1);
        cartService.addItem("cart-1", mouse, 2);

        Assert.assertEquals(cartService.calculateSubtotal("cart-1"), 200.0, 0.0001);
        Assert.assertEquals(cartService.getItemCount("cart-1"), 3);
    }

    @Test(groups = {"smoke", "regression"})
    // Проверяет применение скидочного купона и окончательную сумму корзины с налогом.
    public void shouldApplyCouponAndComputeTotal() {
        Product laptop = new Product("L-1", "Laptop", ProductCategory.ELECTRONICS, 100.0);
        Product mouse = new Product("M-1", "Mouse", ProductCategory.ELECTRONICS, 50.0);

        cartService.addItem("cart-1", laptop, 1);
        cartService.addItem("cart-1", mouse, 2);

        Coupon coupon = new Coupon("SAVE10", DiscountType.PERCENTAGE, 10.0, 0.0, LocalDate.now().plusDays(7), 5);
        cartService.applyCoupon("cart-1", coupon, LocalDate.now());

        Assert.assertEquals(cartService.calculateDiscountAmount("cart-1"), 20.0, 0.0001);
        Assert.assertEquals(cartService.calculateTotal("cart-1", 20.0), 216.0, 0.0001);
    }

    @Test(groups = {"smoke", "regression"})
    // Проверяет жизненный цикл заказа: создание в PENDING, оплату, отправку и доставку.
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
}
