package ru.netology.moneytransfer.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.netology.moneytransfer.entity.CardTransferOperation;

import java.util.UUID;

@Repository
public interface CardTransferOperationRepository extends CrudRepository<CardTransferOperation, UUID> {
}
