package net.devstudy.resume.legacy.controller;

import net.devstudy.resume.legacy.util.DivisionCalculator;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

/**
 * @deprecated Verschoben in neuen Controller
 */
// @Controller
@Deprecated
public class DivisionController {

    @PostMapping("/divide")
    public String calculate(@RequestParam("dividend") String dividendStr,
            @RequestParam("divisor") String divisorStr,
            Model model, HttpSession session) {
        try {
            int dividend = Integer.parseInt(dividendStr);
            int divisor = Integer.parseInt(divisorStr);

            DivisionCalculator calculator = new DivisionCalculator();
            String result = calculator.calculateDivision(dividend, divisor);
            model.addAttribute("message", result);

            // Rechenhistorie aus der Session holen oder neu anlegen
            @SuppressWarnings("unchecked")
            List<String> history = (List<String>) session.getAttribute("history");
            if (history == null) {
                history = new ArrayList<>();
            }

            // Kurze Recheninfo zur Historie hinzufügen
            history.add(dividend + " ÷ " + divisor + " → Ergebnis: OK");
            if (history.size() > 5) {
                history.remove(0); // max. 5 Einträge
            }

            // Session und Model aktualisieren
            session.setAttribute("history", history);
            model.addAttribute("history", history);

        } catch (Exception e) {
            model.addAttribute("message", "Fehler ❌: " + e.getMessage());
        }

        return "index";
    }
}
