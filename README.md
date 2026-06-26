# TestMicroservicio


## Description
Proyecto para un microservicio web con webassembly y TeaVm.

## Endpoints
Un endpoint en WSO2 API Manager es la dirección URL específica del servicio backend real (API, servicio SOAP/REST) al que el gestor de APIs redirige las peticiones de los clientes. Actúa como el destino final del tráfico gestionado, permitiendo configurar balanceo de carga, reintentos, seguridad y políticas de tiempo de espera (timeouts)


http://wso2ei0.srv.corp.sgai-pro.sistemas.csic.es:8280/services/GEP_DSS/institutos

https://servicesws.csic.es/services/ProyectoDSS/proyectos/desde/1/hasta/20
https://servicesws.dev.sgai.csic.es/services/ISEC_DSS/obtenerAdjudicaciones?id_estado=15
https://servicesws.pre.sgai.csic.es/services/ISEC_DSS/obtenerAdjudicaciones?id_estado=15


https://saco.csic.es/apps/files/files/710272453?dir=/Portales%20Corporativos/Endpoints&openfile=true

## Error blocked by CORS policy
### Deshabilitar la seguridad en Chrome.

& "C:\Program Files (x86)\Google\Chrome\Application\chrome.exe" --disable-web-security --user-data-dir="C:/users/25707045Z"

### Crear un proxy inverso (CORS PROXY)
npx local-cors-proxy --proxyUrl https://servicesws.csic.es/services/ProyectoDSS --port 3000 --proxyPartial ""

Para tomcat usar nginx con la configuración indicada (nginx.conf) y cambiar API_ENDPOINT en consecuencia:

const API_ENDPOINT = 'http://localhost:8089/api-csic/services/ProyectoDSS/proyectos/desde/1/hasta/20';

## Insertar un wasm en drupal.

¿Como se configura nginx para usar php?

1. Arranca PHP
php-cgi.exe -b 127.0.0.1:9000

2. Configura Nginx 

server {
    listen       80;
    server_name  localhost;
    root         "C:/ruta/a/tu/drupal/web";
    index        index.php index.html;
    add_header Cross-Origin-Opener-Policy same-origin;
    add_header Cross-Origin-Embedder-Policy require-corp;


    # Esta es la parte clave para PHP
    location ~ \.php$ {
        fastcgi_pass   127.0.0.1:9000; # El puerto donde pusiste a PHP
        fastcgi_index  index.php;
        fastcgi_param  SCRIPT_FILENAME  $document_root$fastcgi_script_name;
        include        fastcgi_params;
    }

    # Regla básica para que Drupal funcione (Clean URLs)
    location / {
        try_files $uri $uri/ /index.php?$query_string;
    }
}

worker_processes  1;

#error_log  logs/error.log;
#error_log  logs/error.log  notice;
#error_log  logs/error.log  info;

#pid        logs/nginx.pid;


events {
    worker_connections  1024;
}

http {
    include       mime.types;
    default_type  application/octet-stream;
    sendfile        on;
    #tcp_nopush     on;
    keepalive_timeout  65;

        server {
            listen       8000;
            server_name  localhost;
            root "C:\Users\25707045Z\AppData\Roaming\drupal\web";
            index index.php index.html;
            add_header Cross-Origin-Opener-Policy same-origin;
            add_header Cross-Origin-Embedder-Policy require-corp;
            
            location ~ \.php$ {
                fastcgi_pass   127.0.0.1:9000; 
                fastcgi_index  index.php;
                fastcgi_param  SCRIPT_FILENAME  $document_root$fastcgi_script_name;
                include        fastcgi_params;
            }

            location / {
                try_files $uri $uri/ /index.php?$query_string;
            }
    }
}

3. Configura php

extension=pdo_sqlite
extension=sqlite3
max_input_time = 60
memory_limit = 512M
max_memory_limit = -1
extension=curl
extension=fileinfo
extension=gd
extension=mbstring
extension=openssl
extension=pdo_sqlite
extension=sqlite3

4. Drupal no deja insertar iframe

Ve a Configuración > Contenido > Formatos de texto y editores (/admin/config/content/formats).
Edita el formato que estés usando (normalmente Basic HTML o Full HTML).
Desmarca "Limitar las etiquetas HTML permitidas" (opción rápida pero menos segura).
O mejor, añade <iframe> a la lista de "Etiquetas HTML permitidas" si la casilla está marcada. Debes incluir los atributos que necesites: <iframe src width height frameborder allowfullscreen>
<iframe src="/wasm-plantilla-1.0-SNAPSHOT/index.html" width="100%" height="500px"></iframe>
Desactivar el filtro de CKEditor (La más efectiva)
Ve a Configuración > Formatos de texto y editores y edita el formato que usas (ej. Full HTML).
Busca la sección de Configuración del editor.
En la pestaña "Configuración de CKEditor", busca una casilla que dice "Limit allowed HTML tags and correct faulty HTML".
Desmárcala. Esto evita que el editor "piense" por ti y borre atributos.

1. Crea un formato exclusivo (Recomendado)
No modifiques el "Basic HTML" que usan los editores normales. En su lugar:
Ve a /admin/config/content/formats y crea uno nuevo llamado "HTML con Wasm".
Configura este formato con el <iframe> y los atributos que necesites.
En la sección de Roles, marca solo el de Administrador.
Así, un editor normal ni siquiera verá ese formato en su desplegable y no podrá "colar" iframes. Tú, como admin, simplemente cambias el selector a "HTML con Wasm" cuando necesites insertar el widget.
Si usas "Full HTML": Solo dáselo a gente de total confianza (o solo a ti). No filtra nada, es potencia pura y riesgo puro.
Si usas etiquetas permitidas: Limita los atributos al mínimo. Con src, width, height y frameborder tienes suficiente para tu widget.

Si quieres ser un ninja de la seguridad, añade el atributo sandbox a tu lista de permitidos y úsalo en tu etiqueta:
<iframe src="..." sandbox="allow-scripts allow-same-origin"></iframe>

Sí, es una estrategia excelente para cerrar el agujero de seguridad. Al estar el Wasm en el mismo servidor (mismo origen), puedes usar varias capas de protección sin romper la funcionalidad.
Aquí tienes las tres formas de configurar ese "filtro" de confianza:
1. Cabecera CSP (Content Security Policy) - La más robusta
Puedes configurar Nginx para que solo permita cargar iframes que provengan de tu propio dominio. Esto bloquea cualquier intento de un editor de insertar un <iframe> de un sitio externo (como una web maliciosa).
En tu bloque server de Nginx, añade:
nginx
add_header Content-Security-Policy "frame-src 'self';";
Como tu Wasm está en el mismo servidor, puedes usar el atributo sandbox en la etiqueta que permitiste en Drupal. Esto evita que el código Java/Wasm intente, por ejemplo, leer las cookies de sesión de tu Drupal:
html
<iframe src="/wasm-plantilla-1.0-SNAPSHOT/index.html" 
        sandbox="allow-scripts allow-same-origin">
</iframe>