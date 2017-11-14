# Server API


Для обмена сообщениями между клиентом  и сервером
 используем get и post запросы, а также websocket.
 
Все ответы в формате  JSON.

Любой ответ содержит поля `code`, `type` и `description`. Ответ с типом `TOKEN` содержит также поле `token`.
Примеры ответов сервера:
```
  {"code":306, "type":"ERROR", "description":"Incorrect username/password"}
  {"token":"yfmqpSpWCVGIuYJY","code":200,"type":"TOKEN","description": "Save this token to local storage of browser. 
  It must be attached to the socket header and also as a parameter of a requests associated with account management"}
```


### Реализованные сервлеты
* /signin
* /signup
* /confirmation


1. /signup - сервлет для регистрации новых пользователей. При успехе отправляет на email пользователя письмо с потверждением аккаунта. 
Принимает обязательные параметры `username, password, email`. `username` и `email` должны быть уникальными. 
Возможные ответы сервера:

code | description                                   | token           | TYPE     | HTTP code
-----|-----------------------------------------------|-----------------|----------|-----------
100  | Need confirm email: sdima11101@gmail.com      |                 | OK       | 200
300  | Some parameters are missed                    |                 | ERROR    | 400
301  | Not unique username                           |                 | ERROR    | 400
302  | Not unique email address                      |                 | ERROR    | 400
303  | Invalid email address                         |                 | ERROR    | 400
304  | Server internal error                         |                 | ERROR    | 500

Не каждая ошибка с данным кодом выдает строго данное описание. 

2. /signin - сервлет для авторизации. Пользователь должен быть зарегестрирован и его аккаунт должен быть потвержден. 
В случае успеха возвращает токен, который необходимо сохранить в local storage браузера и использовать 
в дальнейших операциях управление аккаунтом и для открытия веб сокета (указать в заголовке). 
Принимает обязательные параметры `username`,  `password`. 
Возможные ответы сервера:


code | description                                   | token           | TYPE     | HTTP code
-----|-----------------------------------------------|-----------------|----------|-----------
200  | Save this token to local storage of browser...| yfmqpSpWCVGIuYJY| TOKEN    | 200
300  | Some parameters are missed                    |                 | ERROR    | 400
304  | Server internal error                         |                 | ERROR    | 304
306  | Incorrect username/password                   |                 | ERROR    | 401
307  | Email address does not confirmed              |                 | ERROR    | 401

3. /confirmation - сервлет для потверждения аккаунта. Пользователь получил письмо со ссылкой, перешел по ней, передав параметром уникальный ключ. Если ключ действительный в БД меняется состояние аккаунта на "потвержденный". Однако в данном случае нет смысла слать json сообщения, так как между пользователем и сервером уже не будет клиента. В случае ошибки я устанавливаю соотвествующий HTTP code. Если все ок просто пишу "Account successfully confirmed". Возможно что-то стоит допилить тут, пока хз.

P.S. наверня-ка я где-то допустил орфографические ляпы. Пинайте меня если что.
