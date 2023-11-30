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
import static org.mockito.Mockito.*;

/**
 * Тесты для {@link ShoppingService}
 */
public class ShoppingServiceTest {

    /**
     * Мок для репозитория продуктов
     */
    private final ProductDao productDaoMock = Mockito.mock(ProductDao.class);

    /**
     * Тестируемый сервис для покупок
     */
    private final ShoppingService shoppingService = new ShoppingServiceImpl(productDaoMock);

    /**
     * Пользователь, который используется в тестах
     */
    private final Customer customer = new Customer(0, "11-11-11");


    /*
      Тестирование получения корзины
      Не нужен, т.к. будет протестирован с методом buy
     */
//    @Test
//    public void getCartTest() {
//    }

    /**
     * Тестирование получения несколько товаров
     */
    @Test
    public void getFewProducts() {
        List<Product> expectProducts = List.of(new Product(), new Product());
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
        Cart emptyCart = shoppingService.getCart(customer);
        Assertions.assertEquals(0 , emptyCart.getProducts().size());
        Assertions.assertFalse(shoppingService.buy(emptyCart));
    }

    /**
     * Проверяет успешность покупки товаров в наличии
     */
    @Test
    public void buySomeCart() throws BuyException {
        Cart cart = shoppingService.getCart(customer);
        Product productOne = createProduct(null, 2);
        Product productTwo = createProduct(null, 2);
        Product noCartProduct = createProduct(null, 2);
        cart.add(productOne, 1);
        cart.add(productTwo, 1);

        Assertions.assertTrue(shoppingService.buy(cart));
        Mockito.verify(productDaoMock).save(productOne);
        Mockito.verify(productDaoMock).save(productTwo);
        Mockito.verify(productDaoMock, times(0)).save(noCartProduct);
        Assertions.assertEquals(1, productOne.getCount());
        Assertions.assertEquals(1, productTwo.getCount());
        Assertions.assertEquals(2, noCartProduct.getCount());
    }

    /**
     * Проверяет успешность покупки последнего товара в наличии
     */
    @Test
    public void buyLastProduct() throws BuyException {
        Cart cart = shoppingService.getCart(customer);
        Product productOne = createProduct(null, 1);
        cart.add(productOne, 1);

        Assertions.assertTrue(shoppingService.buy(cart));
        Mockito.verify(productDaoMock).save(productOne);
        Assertions.assertEquals(0, productOne.getCount());
    }

    /**
     * Проверяет неуспешность покупки товаров, которых стало не хватать.
     */
    @Test
    public void buyUnavailable() {
        // Создаем корзину
        Cart cart = shoppingService.getCart(customer);
        Product productOne = createProduct("Some Product", 3);
        Product productTwo = createProduct(null, 2);
        cart.add(productOne, 2);
        cart.add(productTwo, 1);
        // А теперь первый продукт кто-то откупил и его не хватает
        productOne.subtractCount(2);

        // И смотрим, вылетит ли exception
        BuyException exception = Assertions.assertThrows(BuyException.class, () -> shoppingService.buy(cart));
        Assertions.assertEquals(exception.getMessage(), "В наличии нет необходимого количества товара Some Product");
        Mockito.verify(productDaoMock, times(0)).save(any(Product.class));
        Assertions.assertEquals(1, productOne.getCount());
        Assertions.assertEquals(2, productTwo.getCount());
    }

    /**
     * Создает продукт с заданными свойствами
     * @param name Название продукта (может быть null)
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
