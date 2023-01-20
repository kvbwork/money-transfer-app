package ru.netology.moneytransfer.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.netology.moneytransfer.entity.CardAccount;

import java.util.Optional;

@Repository
public interface CardAccountRepository extends CrudRepository<CardAccount, String> {

    Optional<CardAccount> findByCardNumber(String cardNumber);

}
