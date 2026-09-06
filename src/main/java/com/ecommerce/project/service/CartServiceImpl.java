package com.ecommerce.project.service;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.CartItem;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.repositories.CartItemRepository;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.repositories.ProductRepository;
import com.ecommerce.project.util.AuthUtil;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CartServiceImpl implements CartService{

    @Autowired
    CartRepository cartRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    CartItemRepository cartItemRepository;
    @Autowired
    AuthUtil authUtil;
    @Autowired
    ModelMapper modelMapper;
    @Override
    public CartDTO addProductToCart(Long productId, Integer quantity) {
        // find existing cart or create one
        Cart cart = createCart();

        // retrieve product detail
        Product product = productRepository.findById(productId).
                orElseThrow(() -> new ResourceNotFoundException("Product","ProductId",productId));


        //perform validation
       CartItem cartItem =  cartItemRepository.findCartItemByCartIdAndProductId(cart.getCartId(), productId);
       if(cartItem != null){
       throw new APIException("Product" + product.getProductName() + "Product already exists in the cart");
        }
       if(product.getQuantity() == 0) {
           throw new APIException( product.getProductName() + "is out of stock");
       }
       if(product.getQuantity() < quantity) {
           throw new APIException(product.getProductName() + "is out of stock. Only " + product.getQuantity() + " left in stock");
       }

        //create cart item
        CartItem cartItem1 = new CartItem();
       cartItem1.setProduct(product);
       cartItem1.setQuantity(quantity);
       cartItem1.setCart(cart);
       cartItem1.setDiscount(product.getDiscount());
       cartItem1.setProductPrice(product.getPrice());

        //save cart item to the cart
       cartItemRepository.save(cartItem1);
        product.setQuantity(product.getQuantity());
        cart.setTotalPrice(cart.getTotalPrice() + (product.getPrice() * quantity));
        cartRepository.save(cart);
        //return update cart

        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        List<CartItem> cartItems = cart.getCartItems();

        Stream<ProductDTO> productDTOStream = cartItems.stream()
                .map(item -> {
                    ProductDTO map = modelMapper.map(item.getProduct(), ProductDTO.class);
                    map.setQuantity(item.getQuantity());
                    return map;
                });
        cartDTO.setProducts(productDTOStream.toList());

        return cartDTO;
    }

    @Override
    public List<CartDTO> getAllCarts() {
        List<Cart> carts = cartRepository.findAll();
        if (carts.isEmpty()) {
            throw new APIException("No carts found");
        }else {
            List<CartDTO> cartDTOList = carts.stream()
                    .map(cart -> {
                        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
                        List<ProductDTO> productDTOS = cart.getCartItems()
                        .stream()
                                .map(item -> modelMapper.map(item.getProduct(), ProductDTO.class))
                                .collect(Collectors.toList());
                        cartDTO.setProducts(productDTOS);
                        return cartDTO;
                    })
                    .toList();
            return cartDTOList;
        }

    }

    @Override
    public CartDTO getCartByEmailCardId(String emailId, Long cartId) {
        Cart cart = cartRepository.findCartByEmailAndCartId(emailId, cartId);
        if(cart == null){
            throw new ResourceNotFoundException("Cart","EmailId and CartId", emailId + " and " + cartId);
        }
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        cart.getCartItems().forEach(c-> c.getProduct().setQuantity(c.getQuantity()));
        List<ProductDTO> productDTOS = cart.getCartItems()
                .stream()
                .map(item -> modelMapper.map(item.getProduct(), ProductDTO.class))
                .collect(Collectors.toList());
        cartDTO.setProducts(productDTOS);
        return cartDTO;

    }


    @Transactional
    @Override
    public CartDTO updateProductQuantityInCart(Long productId, Integer quantity) {
        String email = authUtil.loggedInEmail();
        Cart userCart = cartRepository.findCartByEmail(email);
        Long cartId = userCart.getCartId();
        Cart cart = cartRepository.findById(cartId).orElseThrow(
                () -> new ResourceNotFoundException("Cart", "cartId", cartId));

        Product product = productRepository.findById(productId).orElseThrow(
                () -> new ResourceNotFoundException("Product", "productId", productId));


        if(product.getQuantity() == 0) {
            throw new APIException( product.getProductName() + "is out of stock");
        }
        if(product.getQuantity() < quantity) {
            throw new APIException(product.getProductName() + "is out of stock. Only " + product.getQuantity() + " left in stock");
        }


        CartItem cartItem = cartItemRepository.findCartItemByCartIdAndProductId(cart.getCartId(), productId);

        if(cartItem == null){
            throw new APIException("product" +  product.getProductName() + "not available in cart");
        }
        int newQuantity = cartItem.getQuantity() + quantity;
        if(newQuantity < 0){
            throw new APIException("Quantity cannot be negative");
        }

        cartItem.setProductPrice(product.getSpecialPrice()) ;
        cartItem.setQuantity(cartItem.getQuantity() + quantity);
        cartItem.setDiscount(product.getDiscount());
        cart.setTotalPrice(cart.getTotalPrice() + (product.getSpecialPrice() * quantity));
        cartRepository.save(cart);
        CartItem updatedCartItem = cartItemRepository.save(cartItem);
        if(updatedCartItem.getQuantity() == 0){
            cartItemRepository.delete(updatedCartItem);
        }
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        List<CartItem> cartItems = cart.getCartItems();

        Stream<ProductDTO> productDTOStream = cartItems.stream()
                .map(item -> {
                    ProductDTO map = modelMapper.map(item.getProduct(), ProductDTO.class);
                    map.setQuantity(item.getQuantity());
                    return map;
                });
        cartDTO.setProducts(productDTOStream.toList());
        return cartDTO;
    }

    @Override
    public String deleteProductFromCart(Long cartId, Long productId) {

        Cart cart = cartRepository.findById(cartId).orElseThrow(
                () -> new ResourceNotFoundException("Cart", "cartId", cartId));
        CartItem cartItem = cartItemRepository.findCartItemByCartIdAndProductId(cart.getCartId(), productId);
        if(cartItem == null){
            throw new ResourceNotFoundException("product", "productId", productId);
        }

        cart.setTotalPrice(cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity()));
        cartItemRepository.deleteCartItemByCartIdAndProductId(cartId, productId);
        return "Product"+ cartItem.getProduct().getProductName() + "deleted from cart successfully";
    }

    @Override
    public void updateProductInCart(Long cartId, Long productId) {

            Cart cart = cartRepository.findById(cartId).orElseThrow(
                    () -> new ResourceNotFoundException("Cart", "cartId", cartId));
            CartItem cartItem = cartItemRepository.findCartItemByCartIdAndProductId(cart.getCartId(), productId);
            if(cartItem == null){
                throw new ResourceNotFoundException("product", "productId", productId);
            }
            Product product = productRepository.findById(productId).orElseThrow(
                    () -> new ResourceNotFoundException("Product", "productId", productId));

            double cartPrice = cart.getTotalPrice() -
                    (cartItem.getProductPrice() * cartItem.getQuantity());
            cartItem.setProductPrice(product.getSpecialPrice());
            cart.setTotalPrice(cartPrice + (cartItem.getProductPrice() * cartItem.getQuantity()));
            cartItem = cartItemRepository.save(cartItem);

    }

    private Cart createCart(){
        Cart userCart = cartRepository.findCartByEmail(authUtil.loggedInEmail());

        if(userCart != null){
            return userCart;
        }
        Cart cart = new Cart();
        cart.setUser(authUtil.loggedInUser());
        cart.setTotalPrice(0.0);
        Cart savedCart = cartRepository.save(cart);
        return savedCart;

    }

}
