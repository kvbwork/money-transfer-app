﻿## Сервис перевода денег

Приложение выполнено как пример реализации перевода средств с одной банковской карты
на другую. Состоит из frontend и backend частей в соответствии с openapi
спецификацией [https://github.com/netology-code/jd-homeworks/blob/master/diploma/MoneyTransferServiceSpecification.yaml](https://github.com/netology-code/jd-homeworks/blob/master/diploma/MoneyTransferServiceSpecification.yaml).

Приложение запускается как набор Docker контейнеров и использует каталог
системы `./appdata` для работы с данными.

## Сборка и запуск

```
docker-compose up
```

Будет установлено окружение и выполнена сборка frontend и backend.
частей. При успешном завершении произойдет запуск приложения.

## Работа с сервисом

После запуска frontend приложение будет доступно по адресу [http://localhost](http://localhost).
Если приложение запущено на отдельном сервере, то следует указать его имя.

Для отдельной работы с API используется путь `/api`

Указать в полях данные карт и сумму перевода. Сверить информацию о зарегистрированных
картах можно в файле `./appdata/accounts.json`.

Например:

|Ваша карта  |                |Карта получателя
|---         |---             |---
|Номер карты:|1111111111111111|2222222222222222
|ММ/ГГ:      |12/99           |
|CVC:        |111             |
|            |Сумма перевода: |5000

После нажатия кнопки **`Отправить`**.

- Приложение обратится к backend по пути `/api/transfer`. Произойдет проверка
  данных и регистрация операции. Во frontend вернется идентификатор операции
  или описание ошибки.

- Frontend подтверждает операцию кодом по пути `/api/confirmOperation`.
  Backend выполняет транзакцию и возвращает статусное сообщение.
  
- Frontend выводит в клиентском браузере окно с сообщением об успешном
  завершении или описание ошибки.

## Дополнительная информация

Подробности работы Backend части описаны в README.md файле в подкаталоге `backend`.