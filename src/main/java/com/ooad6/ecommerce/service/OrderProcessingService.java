package com.ooad6.ecommerce.service;

import com.ooad6.ecommerce.exception.InsufficientStockException;
import com.ooad6.ecommerce.exception.OrderProcessingException;
import com.ooad6.ecommerce.factory.OrderFactory;
import com.ooad6.ecommerce.model.Cart;
import com.ooad6.ecommerce.model.Items;
import com.ooad6.ecommerce.model.Orders;
import com.ooad6.ecommerce.repository.CartRepository;
import com.ooad6.ecommerce.repository.ItemsShow;
import com.ooad6.ecommerce.repository.OrdersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class OrderProcessingService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ItemsShow itemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private OrderFactory orderFactory;

    @Autowired
    private OrderService orderService; 

    @Transactional
    public Orders processOrder(int userId, String paymentMethod) 
            throws InsufficientStockException, OrderProcessingException {
        
        try {
            List<Cart> cartItems = cartRepository.findByUserid(userId);
            
            if (cartItems == null || cartItems.isEmpty()) {
                throw new OrderProcessingException("Cart is empty");
            }

            validateAndDecrementStock(cartItems);

            Orders newOrder = orderFactory.createOrder(userId, cartItems, paymentMethod);
            newOrder.setStatus("PENDING");
            ordersRepository.save(newOrder);

            orderService.placeOrder(newOrder); 

            newOrder.setStatus("CONFIRMED");
            ordersRepository.save(newOrder);

            cartRepository.deleteAll(cartItems);

            System.out.println("Order processed successfully! Order ID: " + newOrder.getOrderId());

            return newOrder;

        } catch (InsufficientStockException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Error processing order: " + e.getMessage());
            e.printStackTrace();
            throw new OrderProcessingException("Failed to process order: " + e.getMessage());
        }
    }

    private void validateAndDecrementStock(List<Cart> cartItems) 
            throws InsufficientStockException {
        
        for (Cart cartItem : cartItems) {
            Query query = new Query();
            query.addCriteria(
                Criteria.where("_id").is(cartItem.getId())
                        .and("Stock").gte(cartItem.getQty())
            );

            Update update = new Update();
            update.inc("Stock", -cartItem.getQty());

            Items updatedItem = mongoTemplate.findAndModify(
                query,
                update,
                Items.class
            );

            if (updatedItem == null) {
                Optional<Items> itemOpt = itemRepository.findById(cartItem.getId());
                
                if (!itemOpt.isPresent()) {
                    throw new InsufficientStockException(
                        "Item not found: " + cartItem.getName()
                    );
                }
                
                Items item = itemOpt.get();
                throw new InsufficientStockException(
                    "Insufficient stock for " + item.getName() + 
                    ". Available: " + item.getStock() + 
                    ", Requested: " + cartItem.getQty()
                );
            }

            System.out.println("Stock decremented atomically for: " + 
                             cartItem.getName() + 
                             " | Quantity: " + cartItem.getQty());
        }
    }

    public List<Cart> getUserCart(int userId) {
        return cartRepository.findByUserid(userId);
    }
}