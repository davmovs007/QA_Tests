package student.tests.service;

import am.gortsup.testing.model.Product;
import am.gortsup.testing.model.ProductCategory;
import am.gortsup.testing.service.InventoryService;
import am.gortsup.testing.service.ProductCatalogService;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ProductInventoryServiceTest {

    private ProductCatalogService productCatalogService;
    private InventoryService inventoryService;

    @BeforeMethod(groups = {"smoke", "regression", "negative"})
    public void setUp() {
        productCatalogService = new ProductCatalogService();
        inventoryService = new InventoryService();
    }

    @Test(groups = {"smoke", "regression"})
    // Проверяет добавление товара в каталог, изменение цены и поиск по имени.
    public void shouldManageCatalogEntries() {
        Product keyboard = productCatalogService.addProduct(new Product("P-100", "Mechanical Keyboard", ProductCategory.ELECTRONICS, 120.0));

        Assert.assertEquals(productCatalogService.searchProductsByName("mechanical").size(), 1);
        productCatalogService.updateProductPrice("P-100", 110.0);
        Assert.assertEquals(productCatalogService.getProductById("P-100").getPrice(), 110.0, 0.0001);
        Assert.assertEquals(productCatalogService.getProductsByCategory(ProductCategory.ELECTRONICS).size(), 1);
        Assert.assertEquals(productCatalogService.getProductById("P-100").getName(), keyboard.getName());
    }

    @Test(groups = {"smoke", "regression"})
    // Проверяет управление остатками товара: резервирование, разрезервирование и проверку наличия.
    public void shouldReserveAndReleaseStock() {
        productCatalogService.addProduct(new Product("P-100", "Mechanical Keyboard", ProductCategory.ELECTRONICS, 120.0));

        inventoryService.setStock("P-100", 10);
        inventoryService.reserveStock("P-100", 4);
        Assert.assertEquals(inventoryService.getStock("P-100"), 6);
        Assert.assertEquals(inventoryService.getReservedStock("P-100"), 4);
        Assert.assertTrue(inventoryService.isInStock("P-100", 5));

        inventoryService.releaseStock("P-100", 2);
        Assert.assertEquals(inventoryService.getStock("P-100"), 8);
        Assert.assertEquals(inventoryService.getReservedStock("P-100"), 2);
    }
}
