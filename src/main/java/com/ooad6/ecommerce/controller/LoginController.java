package com.ooad6.ecommerce.controller;

import com.ooad6.ecommerce.model.User;
import com.ooad6.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.servlet.http.HttpSession;
import java.util.Optional;

@Controller
public class LoginController {
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @RequestMapping("/login")
    public String login() {
        return "login";
    }

    @RequestMapping("loggedin")
    public String loggedin(
            @RequestParam("userid") int userid,
            @RequestParam("password") String password,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        Optional<User> user = userRepository.findByuserId(userid);

        if (user.isPresent())
        {
            boolean match= passwordEncoder.matches(password, user.get().getPassword());
            if(match)
            {

                session.setAttribute("userid", userid);
                if (user.get().getName().toLowerCase().contains("admin"))
                {
                    return "redirect:/adminDashboard";
                }
                else
                {
                    return "redirect:/homepage";
                }
            }
            else
            {
                redirectAttributes.addAttribute("error", "Incorrect password or UserId. Try again!");
                return "redirect:/login";
            }
        }
        else {
            redirectAttributes.addAttribute("error", "Incorrect password or UserId. Try again!");
            return "redirect:/login";
        }
    }
}
