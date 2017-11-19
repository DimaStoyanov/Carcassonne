# Instructions for setting up server on Ubuntu

### Setting up Redis

[Do this steps](https://www.digitalocean.com/community/tutorials/how-to-install-and-configure-redis-on-ubuntu-16-04)

### Setting up PostgreSQL

```
    $ sudo apt-get update
    $ sudo apt-get install postgresql postgresql-contrib

```

Let's set password `password` and create database `users`
```
    $ sudo -i -u postgres
    $ createdb users
    $ psql
    postgres=# \password
    Enter new password: password
    Enter it again: password
    postgres=# \q
```



