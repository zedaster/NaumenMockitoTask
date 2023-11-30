import customer.Customer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import product.Product;
import product.ProductDao;
import shopping.BuyException;
import shopping.Cart;
import shopping.ShoppingService;
import shopping.ShoppingServiceImpl;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

public class ShoppingServiceTest {

    private final ProductDao productDaoMock = Mockito.mock(ProductDao.class);
    private final ShoppingService shoppingService = new ShoppingServiceImpl(productDaoMock);

    /**
     * Тестирование получения корзины
     * Не нужен, т.к. будет протестирован с методом buy
     */
//    @Test
//    public void getCartTest() {
//    }

    /**
     * Тестирование получения несколько товаров
     */
    @Test
    public void getFewProducts() {
        Product productOne = new Product();
        productOne.setName("One");
        Product productTwo = new Product();
        productTwo.setName("Two");
        List<Product> expectProducts = List.of(productOne, productTwo);
        when(productDaoMock.getAll()).thenReturn(expectProducts);

        List<Product> products = shoppingService.getAllProducts();
        Assertions.assertEquals(expectProducts, products);
    }

    /**
     * Тестирование получения пустого списка товаров
     */
    @Test
    public void getZeroProducts() {
        when(productDaoMock.getAll()).thenReturn(new ArrayList<>());
        List<Product> products = shoppingService.getAllProducts();
        Assertions.assertEquals(List.of(), products);
    }

    /**
     * Тестирование получения существующего продукта по имени
     */
    @Test
    public void getProductByName() {
        final String productName = "Expect Product";
        Product expectProduct = new Product();
        expectProduct.setName(productName);
        when(productDaoMock.getByName(productName)).thenReturn(expectProduct);

        Product resultProduct = shoppingService.getProductByName(productName);
        Assertions.assertEquals(expectProduct, resultProduct);
    }

    /**
     * Тестирование получения несуществующего продукта по имени
     */
    @Test
    public void getNonExistentProductByName() {
        final String productName = "Expect Product";
        Product expectProduct = new Product();
        expectProduct.setName(productName);
        when(productDaoMock.getByName(productName)).thenReturn(expectProduct);

        Product resultProduct = shoppingService.getProductByName("我就知道你有一双善于发现细节的眼睛。");
        Assertions.assertNull(resultProduct);
    }

    /**
     * Проверяет не успешность покупки пустой корзины.
     * @throws BuyException Если тест пошел нет так, как надо
     */
    @Test
    public void buyEmptyCart() throws BuyException {
        Customer customer = createCustomer();
        Cart emptyCart = shoppingService.getCart(customer);
        Assertions.assertEquals(0 , emptyCart.getProducts().size());
        Assertions.assertFalse(shoppingService.buy(emptyCart));
    }

    /**
     * Проверяет успешность покупки товаров в наличии
     */
    @Test
    public void buySomeCart() throws BuyException {
        // Здесь будет сохраненные продукты
        List<Product> savedProducts = new ArrayList<>();
        doAnswer((invocation) -> {
            savedProducts.add(invocation.getArgument(0));
            return null;
        }).when(productDaoMock).save(any(Product.class));

        Customer customer = createCustomer();
        Cart cart = shoppingService.getCart(customer);
        Product productOne = createProduct("Product 1", 1);
        Product productTwo = createProduct("Product 2", 1);
        cart.add(productOne, 1);
        cart.add(productTwo, 1);

        Assertions.assertTrue(shoppingService.buy(cart));
        Assertions.assertEquals(List.of(productOne, productTwo), savedProducts);
    }

    /**
     * Проверяет успешность покупки товаров, которых стало не хватать.
     */
    @Test
    public void buyUnavailable() {
        // Список сохраненных продуктов должен быть пустым
        List<Product> savedProducts = new ArrayList<>();
        doAnswer((invocation) -> {
            savedProducts.add(invocation.getArgument(0));
            return null;
        }).when(productDaoMock).save(any(Product.class));

        // Создаем корзину
        Customer customer = createCustomer();
        Cart cart = shoppingService.getCart(customer);
        Product productOne = createProduct("Product 1", 2);
        Product productTwo = createProduct("Product 2", 1);
        cart.add(productOne, 2);
        cart.add(productTwo, 1);
        // А теперь первый продукт кто-то откупил и его не хватает
        productOne.subtractCount(1);

        // И смотрим, вылетит ли exception
        Assertions.assertThrows(BuyException.class, () -> shoppingService.buy(cart));
        Assertions.assertEquals(List.of(), savedProducts);
    }

    /**
     * Создает тестового пользователя
     */
    private Customer createCustomer() {
        return new Customer(0, "11-11-11");
    }

    /**
     * Создает продукт с заданными свойствами
     * @param name Название продукта
     * @param count Количество в наличии
     * @return Экземпляр продукта
     */
    private Product createProduct(String name, int count) {
        Product product = new Product();
        product.setName(name);
        product.addCount(count);
        return product;
    }
}
