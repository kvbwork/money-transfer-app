package ru.netology.moneytransfer.bean;

import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class ConfirmationCodeGenerator implements Supplier<String> {
    @Override
    public String get() {
        return "0000";
    }
}
