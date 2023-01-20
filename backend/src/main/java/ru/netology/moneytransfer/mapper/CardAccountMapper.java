package ru.netology.moneytransfer.mapper;

import org.mapstruct.Mapper;
import ru.netology.moneytransfer.entity.CardAccount;

@Mapper(componentModel = "spring")
public interface CardAccountMapper {

    CardAccount makeCopy(CardAccount account);

}
