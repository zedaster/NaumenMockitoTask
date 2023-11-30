import customer.Customer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import product.Product;
import shopping.Cart;

/**
 * Тесты для корзины
 */
public class CartTest {
    // Сюда вписал тесты, которые должны успешно проходить, но это не так

    /**
     * Не должен быть добавлен в корзину продукт без названия
     */
    @Test
    public void addNoNameProduct() {
        Customer customer = new Customer(0, "11-11-11");
        Cart cart = new Cart(customer);
        try {
            cart.add(new Product(), 1);
            Assertions.fail("Должен быть вызван IllegalArgumentException при отсутствии названия");
        } catch (IllegalArgumentException e) {
            Assertions.assertEquals("Невозможно добавить товар без названия в корзину", e.getMessage());
        }
    }
}
