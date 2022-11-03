package kitchenpos.application;

import java.util.List;
import kitchenpos.dao.ProductDao;
import kitchenpos.domain.Product;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductDao productDao;

    public ProductService(ProductDao productDao) {
        this.productDao = productDao;
    }

    @Transactional
    public Product create(String name, Long price) {
        Product product = new Product(name, price);
        return productDao.save(product);
    }

    public List<Product> list() {
        return productDao.findAll();
    }

    public Product search(Long productId) {
        return productDao.findById(productId)
                .orElseThrow(IllegalArgumentException::new);
    }
}
