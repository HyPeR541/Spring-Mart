package com.ooad6.ecommerce.controller;

import com.ooad6.ecommerce.exception.InsufficientStockException;
import com.ooad6.ecommerce.exception.OrderProcessingException;
import com.ooad6.ecommerce.model.Cart;
import com.ooad6.ecommerce.model.Orders;
import com.ooad6.ecommerce.model.User;
import com.ooad6.ecommerce.repository.UserRepository;
import com.ooad6.ecommerce.service.OrderProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

@Controller
public class OrdersController {

    @Autowired
    private OrderProcessingService orderProcessingService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/orders")
    public String showOrders(Model model, HttpSession session) {
        Object userIdObj = session.getAttribute("userid");
        
        if (userIdObj == null || !(userIdObj instanceof Integer)) {
            return "redirect:/login";
        }

        int userId = (Integer) userIdObj;

        List<Cart> cartItems = orderProcessingService.getUserCart(userId);
        session.setAttribute("cartItems", cartItems);
        model.addAttribute("cartItems", cartItems);
        
        int totalCost = cartItems.stream().mapToInt(item -> item.getCost() * item.getQty()).sum();
        model.addAttribute("totalCost", totalCost);
        
        return "orders";
    }

    @PostMapping("/confirmOrder")
    public String confirmOrder(
            @RequestParam("paymentMethod") String paymentMethod,
            HttpSession session,
            Model model) {
        
        Object userIdObj = session.getAttribute("userid");
        
        if (userIdObj == null || !(userIdObj instanceof Integer)) {
            session.setAttribute("orderMessage", "Session expired. Please login again.");
            return "redirect:/login";
        }

        int userId = (Integer) userIdObj;

        Optional<User> userOpt = userRepository.findByuserId(userId);
        
        if (!userOpt.isPresent()) {
            session.setAttribute("orderMessage", "User not found. Please login again.");
            return "redirect:/login";
        }

        User user = userOpt.get();
        model.addAttribute("username", user.getName());

        try {
            Orders newOrder = orderProcessingService.processOrder(userId, paymentMethod);

            session.removeAttribute("cartItems");
            session.setAttribute("orderMessage", 
                "Order placed successfully! Order ID: " + newOrder.getOrderId());
            
            return "thankyou";

        } catch (InsufficientStockException e) {
            System.err.println("Insufficient stock: " + e.getMessage());
            session.setAttribute("orderMessage", "Order failed: " + e.getMessage());
            return "redirect:/orders";

        } catch (OrderProcessingException e) {
            System.err.println("Order processing error: " + e.getMessage());
            session.setAttribute("orderMessage", 
                "Error processing your order. Please try again. " + e.getMessage());
            return "redirect:/orders";

        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            session.setAttribute("orderMessage", 
                "An unexpected error occurred. Please contact support.");
            return "redirect:/orders";
        }
    }
}