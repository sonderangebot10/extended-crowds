ARG PHP_VERSION=""
FROM php:${PHP_VERSION:+${PHP_VERSION}-}fpm
RUN apt-get update && apt-get install -y \
&& apt-get install -y at\
&& docker-php-ext-install mysqli && docker-php-ext-enable mysqli \
&& pecl install ds && docker-php-ext-enable ds
RUN chown -R www-data:www-data /var/www
