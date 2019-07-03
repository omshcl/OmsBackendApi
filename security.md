# Fixes
Moved Session Cassandra object down into the [Api](src/main/java/oms/Api.java) class so that each servlet no longer has an indivual session associated with it. This should fix our unable to find host bug.

# Security changes
Moved database storage from plaintext to PBKDF2 hash algorithm in the [login](src/main/java/oms/login/LoginApi.java) class. This is an [industry best practice](https://security.stackexchange.com/questions/211/how-to-securely-hash-passwords?noredirect=1&lq=1). Also added a  random salt value to prevent Rainbow table attacks.



