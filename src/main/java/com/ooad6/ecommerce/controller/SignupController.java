package com.ooad6.ecommerce.controller;

import com.ooad6.ecommerce.model.User;
import com.ooad6.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.servlet.http.HttpSession;
import java.util.Random;

@Controller
public class SignupController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @RequestMapping("signup")
    public String sign() {
        System.out.println("This is Signup");
        return "signup";
    }

    @RequestMapping("/registered")
    public String registered(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("address") String address,
            @RequestParam("checkpassw") String checkpassw,
            @RequestParam("phno") String phoneNumber,
            @RequestParam("country") String country,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (password.equals(checkpassw)) {
//            session.setAttribute("name", name);
            int userid = new Random().nextInt(900000) + 100000;
            session.setAttribute("userid", userid);
            model.addAttribute("name", name);
            String encodedPassword = passwordEncoder.encode(password);
            User user = new User( name, email, encodedPassword, address, phoneNumber, country, userid);
            userRepository.save(user);
//            return "redirect:/Homepage";
            return "register";
        }

        else {
            redirectAttributes.addAttribute("error", "Incorrect password. Try again!");
            return "redirect:/signup";
        }

    }
    @RequestMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}

