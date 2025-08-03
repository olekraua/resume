package net.devstudy.resume.util;

public class DivisionCalculator {

    public String calculateDivision(int dividend, int divisor) {
        if (divisor == 0) {
            return "❌ Division durch 0 ist nicht erlaubt!";
        }

        dividend = Math.abs(dividend);
        divisor = Math.abs(divisor);

        if (dividend < divisor) {
            return dividend + " / " + divisor + " = 0";
        }

        StringBuilder result = new StringBuilder();
        StringBuilder quotient = new StringBuilder();
        StringBuilder remainder = new StringBuilder();

        String[] digits = String.valueOf(dividend).split("");
        int divisorLength = String.valueOf(divisor).length();

        for (int i = 0; i < digits.length; i++) {
            remainder.append(digits[i]);
            int number = Integer.parseInt(remainder.toString());

            if (number >= divisor) {
                int count = number / divisor;
                int mult = count * divisor;
                int newRemainder = number % divisor;

                String space = " ".repeat(i + 1 - String.valueOf(number).length());
                result.append(space + "_" + number + "\n");
                result.append(space + " " + mult + "\n");
                result.append(space + " " + "-".repeat(String.valueOf(number).length()) + "\n");

                quotient.append(count);
                remainder.setLength(0);
                remainder.append(newRemainder);
            } else {
                if (i >= divisorLength) {
                    quotient.append("0");
                }
            }
        }

        String finalResult = dividend + " |" + divisor + "\n"
                + "      |" + "-".repeat(quotient.length()) + "\n"
                + "      |" + quotient + "\n"
                + result.toString()
                + "Rest: " + remainder.toString();

        return finalResult;
    }
}
